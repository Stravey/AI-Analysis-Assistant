package org.example.airesumescoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DouBaoApiClientService implements AIService {
    @Value("${doubao.api.key}")
    private String apiKey;
    @Value("${doubao.api.url}")
    private String apiUrl;
    @Value("${doubao.api.model-id}")
    private String modelId; // 从配置文件获取模型ID
    @Value("${doubao.api.access-key:}")
    private String accessKey; // 可选的IAM访问密钥
    @Value("${doubao.api.secret-key:}")
    private String secretKey; // 可选的IAM密钥


    @Override
    public String analyzeResume(String resumeText) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        // 设置认证头
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            // 使用IAM认证
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneId.of("UTC"))
                    .format(Instant.now());
            connection.setRequestProperty("X-DT-Timestamp", timestamp);
            connection.setRequestProperty("Authorization", generateIamSignature(timestamp));
        } else {
            // 使用API Key认证
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        }

        connection.setDoOutput(true);

        Map<String, Object> body = new HashMap<>();
        // 使用配置的模型ID
        if (modelId != null && !modelId.isEmpty()) {
            body.put("model", modelId);
        } else {
            body.put("model", getModelName());
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是简历分析专家，请分析以下简历内容并以JSON格式返回姓名、联系方式、教育经历、工作经历、技能、反馈"
        ));
        messages.add(Map.of("role", "user", "content", resumeText));

        body.put("messages", messages);
        body.put("stream", false);

        String requestBody = new ObjectMapper().writeValueAsString(body);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // 尝试获取详细错误信息
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                throw new RuntimeException("API调用失败，响应码: " + connection.getResponseCode() + ", 错误信息: " + errorResponse.toString());
            } catch (Exception e) {
                throw new RuntimeException("API调用失败，响应码: " + connection.getResponseCode());
            }
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    @Override
    public String resumeDialogue(String context, String question) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        // 设置认证头
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            // 使用IAM认证
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneId.of("UTC"))
                    .format(Instant.now());
            connection.setRequestProperty("X-DT-Timestamp", timestamp);
            connection.setRequestProperty("Authorization", generateIamSignature(timestamp));
        } else {
            // 使用API Key认证
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        }

        connection.setDoOutput(true);

        Map<String, Object> body = new HashMap<>();
        // 使用配置的模型ID
        if (modelId != null && !modelId.isEmpty()) {
            body.put("model", modelId);
        } else {
            body.put("model", getModelName());
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是一个专业的职业顾问，基于用户提供的简历内容和对话历史，回答用户关于职业发展的问题。回答要专业、具体，并给出合理的建议。"
        ));
        messages.add(Map.of("role", "user", "content", context));
        messages.add(Map.of("role", "user", "content", question));

        body.put("messages", messages);
        body.put("temperature", 0.7); // 控制创造性
        body.put("max_tokens", 1000); // 限制响应长度

        String requestBody = new ObjectMapper().writeValueAsString(body);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            // 尝试获取详细错误信息
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                throw new RuntimeException("API调用失败，响应码: " + connection.getResponseCode() + ", 错误信息: " + errorResponse.toString());
            } catch (Exception e) {
                throw new RuntimeException("API调用失败，响应码: " + connection.getResponseCode());
            }
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private String getModelName() {
        // 使用火山引擎平台支持的豆包模型名称
        return "Doubao-lite-128k"; // 这是火山引擎平台上可用的豆包轻量模型
    }

    /**
     * 生成IAM认证签名
     */
    private String generateIamSignature(String timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String signStr = timestamp;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        return "HMAC-SHA256 Credential=" + accessKey + ", SignedHeaders=x-dt-timestamp, Signature=" + signature;
    }

}