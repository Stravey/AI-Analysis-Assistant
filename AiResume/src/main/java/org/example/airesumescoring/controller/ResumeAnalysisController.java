package org.example.airesumescoring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.airesumescoring.model.AIModel;
import org.example.airesumescoring.model.AIServiceFactory;
import org.example.airesumescoring.model.DialogueHistory;
import org.example.airesumescoring.repository.ResumeAnalysisRepository;
import org.example.airesumescoring.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resume")
public class ResumeAnalysisController {

    private final AIServiceFactory aiServiceFactory;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ResumeAnalysisController(AIServiceFactory aiServiceFactory,
                                    ResumeAnalysisRepository resumeAnalysisRepository) {
        this.aiServiceFactory = aiServiceFactory;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeText(
            @RequestBody Map<String, String> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                result.put("error", "请输入简历内容");
                return ResponseEntity.badRequest().body(result);
            }

            // 从请求体中获取模型参数
            String model = request.get("model");
            AIModel aiModel = model != null ? AIModel.fromString(model) : null;
            AIService aiService = aiServiceFactory.getService(aiModel);

            String aiResponse = aiService.analyzeResume(text);
            result.put("aiFeedback", formatAiResponse(aiResponse));
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            result.put("model", aiModel != null ? aiModel.getValue() : (AIModel.DEEPSEEK.getValue()));
            return ResponseEntity.ok(result);

        } catch (HttpMediaTypeNotSupportedException e) {
            result.put("error", "不支持的媒体类型，请使用application/json");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(result);
        } catch (Exception e) {
            result.put("error", "分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/dialogue")
    public ResponseEntity<Map<String, Object>> handleDialogue(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId) {

        Map<String, Object> result = new HashMap<>();
        String question = request.get("question");
        String resumeText = request.get("resumeText");

        if (question == null || question.trim().isEmpty()) {
            result.put("error", "请输入有效的问题");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 从请求体中获取模型参数
            String model = request.get("model");
            AIModel aiModel = model != null ? AIModel.fromString(model) : null;
            AIService aiService = aiServiceFactory.getService(aiModel);

            String context = buildDialogueContext(resumeText, sessionId);
            String aiResponse = aiService.resumeDialogue(context, question);
            saveDialogueHistory(sessionId, question, aiResponse);

            result.put("aiResponse", formatAiResponse(aiResponse));
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            result.put("model", aiModel != null ? aiModel.getValue() : (AIModel.DEEPSEEK.getValue()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", "对话处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private String buildDialogueContext(String resumeText, String sessionId) {
        if (sessionId != null) {
            List<DialogueHistory> history = resumeAnalysisRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            if (!history.isEmpty()) {
                return history.stream()
                        .map(h -> "Q: " + h.getQuestion() + "\nA: " + h.getAnswer())
                        .collect(Collectors.joining("\n"))
                        + "\n\n当前简历内容:\n" + resumeText;
            }
        }
        return "简历内容:\n" + resumeText;
    }

    private void saveDialogueHistory(String sessionId, String question, String answer) {
        if (sessionId != null) {
            DialogueHistory history = new DialogueHistory();
            history.setSessionId(sessionId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setCreatedAt(LocalDateTime.now());
            resumeAnalysisRepository.save(history);
        }
    }

    private String formatAiResponse(String aiResponse) throws Exception {
        JsonNode root = objectMapper.readTree(aiResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return content.replace("```json", "").replace("```", "").trim();
    }
}