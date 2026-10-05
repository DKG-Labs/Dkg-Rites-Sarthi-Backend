package com.sarthi;

import com.sarthi.service.PdfCompressionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPdfCompression {

    @Test
    public void testCompressionOnImagePdf() throws Exception {
        // 1. Create a synthetic uncompressed PDF with a 2400x3200 image (simulating a phone scan)
        byte[] originalPdf;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            BufferedImage img = new BufferedImage(2400, 3200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 2400, 3200);
            g.setColor(Color.BLACK);
            // Draw dummy table lines and text
            for (int i = 0; i < 3200; i += 50) {
                g.drawLine(0, i, 2400, i);
                g.drawString("Row " + i + " Some tabular sample data 12345.67", 100, i + 30);
            }
            g.dispose();

            PDImageXObject imgXObject = LosslessFactory.createFromImage(doc, img);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(imgXObject, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            originalPdf = baos.toByteArray();
        }

        System.out.println("Original synthetic PDF size: " + originalPdf.length + " bytes (" + (originalPdf.length / (1024 * 1024.0)) + " MB)");

        // 2. Compress using our service
        PdfCompressionService service = new PdfCompressionService();
        byte[] compressedPdf = service.compressPdf(originalPdf);

        System.out.println("Compressed PDF size: " + compressedPdf.length + " bytes (" + (compressedPdf.length / (1024 * 1024.0)) + " MB)");
        System.out.println("Reduction: " + String.format("%.2f", (1.0 - (double) compressedPdf.length / originalPdf.length) * 100) + "%");

        assertTrue(compressedPdf.length < originalPdf.length, "Compressed size should be smaller");
    }
}
