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
import java.util.HashMap;
import java.util.Map;

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

        int effectiveMaxWidth = targetMaxWidth > 0 ? targetMaxWidth : this.maxImageWidth;
        float effectiveQuality = (targetQuality > 0.0f && targetQuality <= 1.0f) ? targetQuality : this.jpegQuality;

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(originalBytes))) {
            Map<PDImageXObject, Boolean> processedCache = new HashMap<>();

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName name : resources.getXObjectNames()) {
                    if (!resources.isImageXObject(name)) continue;

                    try {
                        PDImageXObject originalImageXObject = (PDImageXObject) resources.getXObject(name);
                        if (originalImageXObject == null) continue;

                        if (processedCache.containsKey(originalImageXObject)) {
                            continue;
                        }
                        processedCache.put(originalImageXObject, true);

                        BufferedImage bImage = originalImageXObject.getImage();
                        if (bImage == null) continue;

                        int origWidth = bImage.getWidth();
                        int origHeight = bImage.getHeight();

                        // Skip small icons/logos under 350px
                        if (origWidth < 350 && origHeight < 350) {
                            continue;
                        }

                        org.apache.pdfbox.cos.COSStream oldStream = originalImageXObject.getCOSObject();
                        long oldStreamLen = oldStream != null ? oldStream.getLength() : 0;

                        // If the image is already lightweight (<= 700 KB) and within max width, keep it untouched
                        if (oldStreamLen > 0 && oldStreamLen <= targetImageSize && origWidth <= effectiveMaxWidth) {
                            continue;
                        }

                        BufferedImage processedImage = scaleAndConvertToRgb(bImage, effectiveMaxWidth);
                        PDImageXObject compressedImageXObject = JPEGFactory.createFromImage(document, processedImage, effectiveQuality);
                        org.apache.pdfbox.cos.COSStream newStream = compressedImageXObject.getCOSObject();

                        // Only replace if the new compressed stream is actually smaller than the old stream
                        long newStreamLen = newStream.getLength();

                        if (newStreamLen < oldStreamLen || origWidth > effectiveMaxWidth) {
                            oldStream.clear();
                            for (java.util.Map.Entry<COSName, org.apache.pdfbox.cos.COSBase> entry : newStream.entrySet()) {
                                oldStream.setItem(entry.getKey(), entry.getValue());
                            }
                            try (java.io.InputStream in = newStream.createRawInputStream();
                                 java.io.OutputStream outStream = oldStream.createRawOutputStream()) {
                                in.transferTo(outStream);
                            }
                        }
                    } catch (Exception imgEx) {
                        log.debug("Skipped compressing image {}: {}", name.getName(), imgEx.getMessage());
                    }
                }
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
