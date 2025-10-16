package com.ams.service;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * QR features have been removed. This stub remains to avoid breaking references.
 */
public class QRService {
    public static final class GeneratedQr {
        public final long id; public final String payload; public final Path file;
        public GeneratedQr(long id, String payload, Path file) { this.id = id; this.payload = payload; this.file = file; }
        public long id() { return id; } public String payload() { return payload; } public Path file() { return file; }
    }

    public GeneratedQr generateForDate(LocalDate date) {
        throw new UnsupportedOperationException("QR functionality has been removed");
    }
}
