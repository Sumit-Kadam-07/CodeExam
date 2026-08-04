package com.example.exam.util;

import java.io.InputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocxParser {

    private static final Logger logger = LoggerFactory.getLogger(DocxParser.class);

    public String extractFromDoc(InputStream inputStream) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(doc);
            String text = extractor.getText();
            extractor.close();
            logger.info("Extracted {} characters from DOC file", text.length());
            return text;
        }
    }

    public String extractFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            extractor.close();
            logger.info("Extracted {} characters from DOCX file", text.length());
            return text;
        }
    }
}
