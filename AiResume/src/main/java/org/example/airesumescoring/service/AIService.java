package org.example.airesumescoring.service;

public interface AIService {
    String analyzeResume(String resumeText) throws Exception;

    String resumeDialogue(String context, String question) throws Exception;
}
