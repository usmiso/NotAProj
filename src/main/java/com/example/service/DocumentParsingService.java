package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class DocumentParsingService {

    private static final String UPLOAD_DIR = "uploads/";

    public DocumentParsingService() {
        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            log.error("Failed to create upload directory", e);
        }
    }

    /**
     * Save uploaded file and extract text from PDF
     */
    public String parsePdfFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Save file to disk
        Files.write(filePath, file.getBytes());

        // Extract text from PDF
        return extractTextFromPdf(filePath.toFile());
    }

    /**
     * Extract text from a PDF file
     */
    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(new java.io.FileInputStream(pdfFile))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: " + pdfFile.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Save uploaded image file (for future OCR processing)
     */
    public String saveImageFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Save file to disk
        Files.write(filePath, file.getBytes());

        log.info("Image saved successfully: " + filePath);
        return filePath.toString();
    }

    /**
     * Extract VIN from text (simple pattern matching)
     */
    public String extractVin(String text) {
        // VIN is 17 characters, alphanumeric, no I, O, Q
        String vinPattern = "[A-HJ-NPR-Z0-9]{17}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(vinPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Extract mileage from text using regex patterns
     */
    public Integer extractMileage(String text) {
        // Look for patterns like "12345 km", "12,345 km", "Mileage: 12345"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?:mileage|km|odometer)\\s*:?\\s*(\\d+[,.]?\\d*)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String mileageStr = matcher.group(1).replaceAll("[,.]", "");
            try {
                return Integer.parseInt(mileageStr);
            } catch (NumberFormatException e) {
                log.warn("Could not parse mileage: " + mileageStr);
            }
        }
        return null;
    }

    /**
     * Extract accident indicators from text
     */
    public boolean hasAccidentIndicators(String text) {
        String[] accidentKeywords = {
                "accident", "damage", "collision", "impact", "crash",
                "salvage", "rebuild", "written off", "total loss"
        };

        String lowerText = text.toLowerCase();
        for (String keyword : accidentKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if report indicates roadworthy pass
     */
    public boolean isRoadworthyPass(String text) {
        String lowerText = text.toLowerCase();

        // Look for pass indicators
        if (lowerText.contains("roadworthy") && lowerText.contains("pass")) {
            return true;
        }
        if (lowerText.contains("road worthy") && lowerText.contains("pass")) {
            return true;
        }

        return false;
    }
}
