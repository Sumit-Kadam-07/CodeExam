package com.example.exam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ImageTextExtractor.class);

    /**
     * OCR is not yet implemented.
     * To enable OCR, integrate Tesseract via Tess4J or a cloud OCR API.
     *
     * Required for OCR:
     * - Tesseract OCR engine installed on the system
     * - Tess4J library added to pom.xml
     * - Language data files (e.g., eng.traineddata)
     */
    public String extractText(byte[] imageData, String imageType) {
        throw new UnsupportedOperationException(
            "OCR is not yet implemented. Please upload a text document (PDF, DOC, DOCX, TXT, MD) instead.");
    }

    public boolean isOcrAvailable() {
        return false;
    }
}
