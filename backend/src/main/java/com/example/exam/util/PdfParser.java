package com.example.exam.util;

import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PdfParser {

    private static final Logger logger = LoggerFactory.getLogger(PdfParser.class);

    public String extractText(InputStream inputStream) throws Exception {
        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            logger.info("Extracted {} characters from PDF", text.length());
            return text;
        }
    }
}
