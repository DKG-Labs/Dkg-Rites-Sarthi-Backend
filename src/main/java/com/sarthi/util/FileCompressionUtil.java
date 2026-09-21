package com.sarthi.util;

import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Slf4j
public class FileCompressionUtil {

    private static final int BUFFER_SIZE = 64 * 1024; // 64 KB buffer for fast streaming

    /**
     * Compress byte array using Deflater at BEST_COMPRESSION level.
     */
    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) return data;

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Math.max(data.length / 2, 1024));
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] compressed = outputStream.toByteArray();
            log.info("Compressed file from {} bytes to {} bytes (saved {}%)",
                    data.length, compressed.length,
                    String.format("%.1f", (1.0 - (double) compressed.length / data.length) * 100));
            return compressed;
        } catch (Exception e) {
            log.error("Error compressing file data, returning original: {}", e.getMessage(), e);
            return data;
        } finally {
            deflater.end();
        }
    }

    /**
     * Decompress byte array using Inflater.
     */
    public static byte[] decompress(byte[] data) {
        if (data == null || data.length == 0) return data;

        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Math.max(data.length * 2, 1024));
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) {
                    break;
                }
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] decompressed = outputStream.toByteArray();
            log.info("Decompressed file from {} bytes to {} bytes", data.length, decompressed.length);
            return decompressed;
        } catch (Exception e) {
            log.error("Error decompressing file data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to decompress file: " + e.getMessage(), e);
        } finally {
            inflater.end();
        }
    }

    /**
     * Check if bytes represent an uncompressed PDF (%PDF-)
     */
    public static boolean isRawPdf(byte[] data) {
        if (data == null || data.length < 4) return false;
        return data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F';
    }

    /**
     * Decompresses data if it is compressed, or returns raw bytes if it's already an uncompressed PDF.
     */
    public static byte[] decompressIfNeeded(byte[] data) {
        if (data == null || data.length < 4) return data;
        if (isRawPdf(data)) {
            return data;
        }
        try {
            return decompress(data);
        } catch (Exception e) {
            log.warn("Could not decompress data, assuming already raw: {}", e.getMessage());
            return data;
        }
    }
}
