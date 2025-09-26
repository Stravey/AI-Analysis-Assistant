package org.example.airesumescoring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.airesumescoring.model.AIModel;
import org.example.airesumescoring.model.AIServiceFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume/upload")
public class ResumeController {

    private final AIServiceFactory aiServiceFactory;
    private final ObjectMapper objectMapper;

    public ResumeController(AIServiceFactory aiServiceFactory, ObjectMapper objectMapper) {
        this.aiServiceFactory = aiServiceFactory;
        this.objectMapper = objectMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("resume") MultipartFile file,
            @RequestParam(value = "model", required = false) String model) {

        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "请选择有效的简历文件");
            return ResponseEntity.badRequest().body(result);
        }

        if (!isValidFileType(file.getContentType())) {
            result.put("error", "仅支持PDF/DOC/DOCX格式");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String text = extractTextFromFile(file);
            AIModel aiModel = model != null ? AIModel.fromString(model) : null;
            String aiResponse = aiServiceFactory.getService(aiModel).analyzeResume(text);

            result.put("fileName", file.getOriginalFilename());
            result.put("aiFeedback", formatAiResponse(aiResponse));
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            result.put("model", aiModel != null ? aiModel.getValue() : (AIModel.DEEPSEEK.getValue()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", "文件处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private boolean isValidFileType(String contentType) {
        return Arrays.asList(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ).contains(contentType);
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if ("application/pdf".equals(contentType)) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(document);
            }
        } else if ("application/msword".equals(contentType)) {
            try (HWPFDocument doc = new HWPFDocument(file.getInputStream())) {
                return new WordExtractor(doc).getText();
            }
        } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
            try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
                return new XWPFWordExtractor(docx).getText();
            }
        }
        return "";
    }

    private String formatAiResponse(String aiResponse) throws Exception {
        JsonNode root = objectMapper.readTree(aiResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return content.replace("```json", "").replace("```", "").trim();
    }
}