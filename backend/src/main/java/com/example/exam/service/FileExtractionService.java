package com.example.exam.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.util.DocxParser;
import com.example.exam.util.PdfParser;

@Service
public class FileExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FileExtractionService.class);
    private static final List<String> SUPPORTED_TEXT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "text/markdown"
    );

    private final PdfParser pdfParser;
    private final DocxParser docxParser;

    public FileExtractionService(PdfParser pdfParser, DocxParser docxParser) {
        this.pdfParser = pdfParser;
        this.docxParser = docxParser;
    }

    public String extractText(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String ext = filename != null ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";

        if ("pdf".equals(ext) || (contentType != null && contentType.contains("pdf"))) {
            return pdfParser.extractText(file.getInputStream());
        } else if ("doc".equals(ext) || (contentType != null && contentType.equals("application/msword"))) {
            return docxParser.extractFromDoc(file.getInputStream());
        } else if ("docx".equals(ext) || (contentType != null && contentType.contains("wordprocessingml"))) {
            return docxParser.extractFromDocx(file.getInputStream());
        } else if ("txt".equals(ext) || "md".equals(ext) || (contentType != null && (contentType.startsWith("text/")))) {
            return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + ext + ". Supported: PDF, DOC, DOCX, TXT, MD");
        }
    }

    public boolean isSupportedFileType(String contentType, String filename) {
        if (contentType == null && filename == null) return false;
        String ext = filename != null ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        return SUPPORTED_TEXT_TYPES.contains(contentType)
            || Arrays.asList("pdf", "doc", "docx", "txt", "md").contains(ext);
    }
}
