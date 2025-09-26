package org.example.airesumescoring.repository;

import org.example.airesumescoring.model.DialogueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<DialogueHistory, Long> {

    // 添加缺少的findBySessionId方法
    List<DialogueHistory> findBySessionId(String sessionId);

    List<DialogueHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    void deleteBySessionId(String sessionId);

    DialogueHistory findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
}