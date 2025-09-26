package org.example.airesumescoring.model;

import org.example.airesumescoring.config.AIConfig;
import org.example.airesumescoring.service.AIService;
import org.example.airesumescoring.service.DeepSeekApiClientService;
import org.example.airesumescoring.service.DouBaoApiClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIServiceFactory {

    @Autowired
    private DeepSeekApiClientService deepSeekService;

    @Autowired
    private DouBaoApiClientService douBaoService;

    @Autowired
    private AIConfig aiConfig;


    public AIService getService(AIModel model) {
        if (model == null) {
            // 使用配置的默认模型，如果没有配置则使用DeepSeek
            if (aiConfig != null && aiConfig.getModelDefault() != null) {
                model = AIModel.fromString(aiConfig.getModelDefault());
            } else {
                model = AIModel.DEEPSEEK;
            }
        }

        switch (model) {
            case DEEPSEEK:
                return deepSeekService;
            case DOUBAO:
                return douBaoService;
            default:
                throw new IllegalArgumentException("不支持的AI模型: " + model);
        }
    }
}