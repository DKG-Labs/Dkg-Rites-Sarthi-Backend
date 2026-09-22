package com.sarthi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfCompressionService {

    /**
     * Compresses a PDF byte array using Apache PDFBox stream and object compression.
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            // Save with full PDF stream compression
            document.save(out, CompressParameters.DEFAULT_COMPRESSION);
            byte[] compressedBytes = out.toByteArray();

            if (compressedBytes.length > 0 && compressedBytes.length < originalBytes.length) {
                log.info("PDF compressed successfully: original={} bytes, compressed={} bytes (saved {}%)",
                        originalBytes.length, compressedBytes.length,
                        String.format("%.1f", (1.0 - (double) compressedBytes.length / originalBytes.length) * 100));
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
}
