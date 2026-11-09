package com.sarthi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfCompressionService {

    /**
     * Whether to compress/downsample images inside PDFs.
     * Default: false to ensure images are NEVER compressed and retain 100% original quality.
     */
    @Value("${pdf.compression.compress-images:false}")
    private boolean compressImages = false;

    /**
     * Target max width in pixels for embedded page scans/photos if compression is enabled.
     * 2600px provides high clarity (full 300 DPI for A4) ensuring tabular text,
     * signatures, and small numbers remain sharp and legible without pixelation.
     */
    @Value("${pdf.compression.max-image-width:2600}")
    private int maxImageWidth = 2600;

    /**
     * JPEG quality setting if compression is enabled.
     */
    @Value("${pdf.compression.jpeg-quality:0.90}")
    private float jpegQuality = 0.90f;

    /**
     * Target size threshold in bytes (700 KB).
     * If an image stream is already <= 700 KB, it will not be recompressed further.
     */
    @Value("${pdf.compression.target-image-size:716800}")
    private long targetImageSize = 700L * 1024L;

    /**
     * Compresses a PDF byte array using default settings.
     * If compressImages is false, returns original uncompressed bytes directly.
     *
     * @param originalBytes The raw PDF bytes
     * @return PDF bytes (uncompressed original if compress-images=false)
     */
    public byte[] compressPdf(byte[] originalBytes) {
        return compressPdf(originalBytes, this.maxImageWidth, this.jpegQuality);
    }

    /**
     * Compresses a PDF byte array using specified maxWidth and quality settings.
     *
     * @param originalBytes The raw PDF bytes
     * @param targetMaxWidth Maximum pixel width for downsampling embedded images
     * @param targetQuality JPEG compression quality (0.0f - 1.0f)
     * @return PDF bytes
     */
    public byte[] compressPdf(byte[] originalBytes, int targetMaxWidth, float targetQuality) {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }

        // If image compression is disabled, return original bytes directly without altering images
        if (!this.compressImages) {
            log.info("PDF image compression is disabled (compress-images=false). Preserving original uncompressed images.");
            return originalBytes;
        }

        return compressImagesInPdf(originalBytes, targetMaxWidth, targetQuality, this.targetImageSize, false);
    }

    /**
     * Images scaled/encoded at once. Kept small on purpose: one decoded scan page can take 50-100 MB of heap,
     * so this also caps memory per request.
     */
    private static final int IMAGE_PARALLELISM =
            Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 1));

    /** Shared pool for image encoding; daemon threads so it never blocks shutdown. */
    private static final java.util.concurrent.ExecutorService IMAGE_POOL =
            java.util.concurrent.Executors.newFixedThreadPool(IMAGE_PARALLELISM, r -> {
                Thread t = new Thread(r, "pdf-image-encoder");
                t.setDaemon(true);
                return t;
            });

    private record PendingImage(String name, org.apache.pdfbox.cos.COSStream oldStream, int origWidth,
                                long oldStreamLen, java.util.concurrent.Future<byte[]> encoded) {}

    /** JPEG-encodes an RGB image at the given quality (same output PDFBox JPEGFactory would embed). */
    private static byte[] encodeJpeg(BufferedImage image, float quality) throws java.io.IOException {
        javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            ios.flush();
            return baos.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    /** Upper bound on preview image re-encoding, kept well under the gateway timeout. */
    private static final long PREVIEW_TIME_BUDGET_MS = 20_000L;

    /**
     * Smaller copy of a PDF for on-screen preview only (never for stored documents): page images are scaled to
     * at most 1600px and re-encoded as JPEG 0.70. Runs regardless of pdf.compression.compress-images, which
     * protects stored documents only. Digital signatures are not a concern because previews are never saved.
     */
    public byte[] compressForPreview(byte[] originalBytes) {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }
        return compressImagesInPdf(originalBytes, 1600, 0.70f, 150L * 1024L, true);
    }

    /**
     * Downsamples and re-encodes large page images; returns the original bytes if nothing gets smaller.
     * previewMode: replace an image only when the JPEG is smaller, and leave 1-bit scans (CCITT/JBIG2) alone,
     * since JPEG makes those bigger and blurrier.
     */
    private byte[] compressImagesInPdf(byte[] originalBytes, int targetMaxWidth, float targetQuality, long sizeThreshold,
                                       boolean previewMode) {
        int effectiveMaxWidth = targetMaxWidth > 0 ? targetMaxWidth : this.maxImageWidth;
        float effectiveQuality = (targetQuality > 0.0f && targetQuality <= 1.0f) ? targetQuality : this.jpegQuality;

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(originalBytes))) {
            // Preview only: cap the image work so large (~50 MB) uploads return before the gateway timeout;
            // images not reached in time are left as they are (bigger preview, same content)
            final long deadline = previewMode ? System.currentTimeMillis() + PREVIEW_TIME_BUDGET_MS : Long.MAX_VALUE;
            int skippedForTime = 0;
            // Keyed by the underlying stream so an image shared by many pages (e.g. a letterhead) is handled once
            java.util.Set<org.apache.pdfbox.cos.COSStream> processedStreams =
                    java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            java.util.List<PendingImage> pending = new java.util.ArrayList<>();
            java.util.concurrent.Semaphore inFlight = new java.util.concurrent.Semaphore(IMAGE_PARALLELISM);

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName name : resources.getXObjectNames()) {
                    if (!resources.isImageXObject(name)) continue;
                    if (System.currentTimeMillis() > deadline) {
                        skippedForTime++;
                        continue;
                    }

                    try {
                        PDImageXObject originalImageXObject = (PDImageXObject) resources.getXObject(name);
                        if (originalImageXObject == null) continue;

                        org.apache.pdfbox.cos.COSStream oldStream = originalImageXObject.getCOSObject();
                        if (oldStream == null || !processedStreams.add(oldStream)) {
                            continue;
                        }

                        // Size checks use the image dictionary, so images that are skipped are never decoded
                        int origWidth = originalImageXObject.getWidth();
                        int origHeight = originalImageXObject.getHeight();

                        // Skip small icons/logos under 350px
                        if (origWidth < 350 && origHeight < 350) {
                            continue;
                        }

                        long oldStreamLen = oldStream.getLength();

                        // If the image is already lightweight (<= 700 KB) and within max width, keep it untouched
                        if (oldStreamLen > 0 && oldStreamLen <= sizeThreshold && origWidth <= effectiveMaxWidth) {
                            continue;
                        }

                        // 1-bit scans and stencil masks are already tiny; JPEG would enlarge and blur them
                        if (previewMode && (originalImageXObject.isStencil() || originalImageXObject.getBitsPerComponent() == 1)) {
                            continue;
                        }

                        // Decoding reads the shared document, so it stays on this thread (PDDocument is not thread-safe).
                        // Wait for a free slot first: caps how many decoded images sit in memory at once.
                        inFlight.acquire();
                        BufferedImage bImage;
                        try {
                            bImage = originalImageXObject.getImage();
                        } catch (Exception decodeEx) {
                            inFlight.release();
                            throw decodeEx;
                        }
                        if (bImage == null) {
                            inFlight.release();
                            continue;
                        }

                        // Scale + JPEG encode touch only the image, so they run in parallel
                        final BufferedImage src = bImage;
                        java.util.concurrent.Future<byte[]> encoded = IMAGE_POOL.submit(() -> {
                            try {
                                return encodeJpeg(scaleAndConvertToRgb(src, effectiveMaxWidth), effectiveQuality);
                            } finally {
                                inFlight.release();
                            }
                        });
                        pending.add(new PendingImage(name.getName(), oldStream, origWidth, oldStreamLen, encoded));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception imgEx) {
                        log.debug("Skipped compressing image {}: {}", name.getName(), imgEx.getMessage());
                    }
                }
            }

            // Put results back one at a time (document writes stay on this thread), same replace rules as before
            for (PendingImage p : pending) {
                try {
                    long waitMs = previewMode ? Math.max(0L, deadline - System.currentTimeMillis()) : Long.MAX_VALUE;
                    byte[] jpegBytes = previewMode
                            ? p.encoded.get(waitMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                            : p.encoded.get();
                    PDImageXObject compressedImageXObject = JPEGFactory.createFromByteArray(document, jpegBytes);
                    org.apache.pdfbox.cos.COSStream newStream = compressedImageXObject.getCOSObject();
                    long newStreamLen = newStream.getLength();

                    boolean replace = previewMode
                            ? newStreamLen < p.oldStreamLen
                            : (newStreamLen < p.oldStreamLen || p.origWidth > effectiveMaxWidth);
                    if (replace) {
                        p.oldStream.clear();
                        for (java.util.Map.Entry<COSName, org.apache.pdfbox.cos.COSBase> entry : newStream.entrySet()) {
                            p.oldStream.setItem(entry.getKey(), entry.getValue());
                        }
                        try (java.io.InputStream in = newStream.createRawInputStream();
                             java.io.OutputStream outStream = p.oldStream.createRawOutputStream()) {
                            in.transferTo(outStream);
                        }
                    }
                } catch (java.util.concurrent.TimeoutException te) {
                    p.encoded.cancel(true);
                    skippedForTime++;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    p.encoded.cancel(true);
                } catch (Exception imgEx) {
                    log.debug("Skipped compressing image {}: {}", p.name, imgEx.getMessage());
                }
            }

            if (skippedForTime > 0) {
                log.info("Preview compression hit its {} ms time limit; {} images left uncompressed", PREVIEW_TIME_BUDGET_MS, skippedForTime);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out, CompressParameters.DEFAULT_COMPRESSION);
            byte[] compressedBytes = out.toByteArray();

            if (compressedBytes.length > 0 && compressedBytes.length < originalBytes.length) {
                double reduction = (1.0 - (double) compressedBytes.length / originalBytes.length) * 100.0;
                log.info("PDF compressed successfully (quality={}, maxWidth={}): original={} bytes ({} MB), compressed={} bytes ({} MB), saved {}%",
                        effectiveQuality, effectiveMaxWidth,
                        originalBytes.length, String.format("%.2f", originalBytes.length / (1024.0 * 1024.0)),
                        compressedBytes.length, String.format("%.2f", compressedBytes.length / (1024.0 * 1024.0)),
                        String.format("%.1f", reduction));
                return compressedBytes;
            } else {
                log.info("PDF already optimal or compressed size ({}) >= original ({}). Using original bytes.",
                        compressedBytes.length, originalBytes.length);
                return originalBytes;
            }
        } catch (Exception e) {
            log.warn("Could not compress PDF (using original uncompressed stream): {}", e.getMessage());
            return originalBytes;
        }
    }

    /**
     * Converts the image to RGB (white background for any transparency) and scales
     * it proportionally if it exceeds maxWidth using high-quality rendering hints.
     */
    private BufferedImage scaleAndConvertToRgb(BufferedImage src, int maxWidth) {
        int width = src.getWidth();
        int height = src.getHeight();

        int targetWidth = width;
        int targetHeight = height;

        if (width > maxWidth) {
            targetWidth = maxWidth;
            targetHeight = (int) Math.round(((double) height / width) * maxWidth);
        }

        BufferedImage rgbImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgbImage.createGraphics();

        // High quality bicubic rendering hints for sharp tabular borders and crisp text
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill white background (in case of PNG alpha transparency)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return rgbImage;
    }
}
