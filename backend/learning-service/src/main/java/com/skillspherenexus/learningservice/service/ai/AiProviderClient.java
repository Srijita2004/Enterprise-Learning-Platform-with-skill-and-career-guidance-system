package com.skillspherenexus.learningservice.service.ai;

import java.util.List;
import java.util.Map;

public interface AiProviderClient {

    record ProviderResult(String reply, String providerEngine, List<String> suggestedFollowUps) {}

    ProviderResult generateLearningResponse(
            String userMessage,
            String quickAction,
            String courseTitle,
            String moduleTitle,
            String lessonTitle,
            String lessonContent,
            List<Map<String, String>> conversationHistory
    );
}
