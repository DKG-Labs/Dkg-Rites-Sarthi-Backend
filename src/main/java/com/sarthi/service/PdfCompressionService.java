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
     * Target max width in pixels for embedded page scans/photos.
     * 1200px provides high clarity for tabular text and numbers while ensuring
     * even 200+ page documents compress down to manageable sizes.
     */
    private static final int MAX_IMAGE_WIDTH = 1200;

    /**
     * JPEG quality setting. 0.65f provides crisp text and table borders while
     * delivering substantial compression even on pre-encoded JPEGs.
     */
    private static final float JPEG_QUALITY = 0.65f;

    /**
     * Compresses a PDF byte array using image downsampling/JPEG re-encoding
     * and Apache PDFBox stream and object compression.
     * If compression fails or the compressed size is larger, returns original bytes.
     *
     * @param originalBytes The raw PDF bytes
     * @return Compressed PDF bytes
     */
    public byte[] compressPdf(byte[] originalBytes) {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }

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

                        BufferedImage processedImage = scaleAndConvertToRgb(bImage, MAX_IMAGE_WIDTH);
                        PDImageXObject compressedImageXObject = JPEGFactory.createFromImage(document, processedImage, JPEG_QUALITY);

                        org.apache.pdfbox.cos.COSStream oldStream = originalImageXObject.getCOSObject();
                        org.apache.pdfbox.cos.COSStream newStream = compressedImageXObject.getCOSObject();

                        // Only replace if the new compressed stream is actually smaller than the old stream
                        long oldStreamLen = oldStream.getLength();
                        long newStreamLen = newStream.getLength();

                        if (newStreamLen < oldStreamLen || origWidth > MAX_IMAGE_WIDTH) {
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
                log.info("PDF compressed successfully: original={} bytes ({} MB), compressed={} bytes ({} MB), saved {}%",
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

        // High quality rendering hints for sharp tabular borders and text
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
