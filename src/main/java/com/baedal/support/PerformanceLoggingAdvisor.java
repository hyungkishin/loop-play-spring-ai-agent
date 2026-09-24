package com.baedal.support;

import com.baedal.support.observability.AgentMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceLoggingAdvisor implements CallAdvisor {

    private final AgentMetrics metrics;

    @Override
    public String getName() {
        return "PerformanceLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startNanos = System.nanoTime();
        try {
            ChatClientResponse response = chain.nextCall(request);
            logSuccess(elapsedMs(startNanos), response);
            return response;
        } catch (RuntimeException e) {
            logFailure(elapsedMs(startNanos), e);
            throw e;
        }
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private void logSuccess(long elapsedMs, ChatClientResponse response) {
        Integer promptTokens = null;
        Integer completionTokens = null;
        Integer totalTokens = null;

        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse != null) {
            ChatResponseMetadata metadata = chatResponse.getMetadata();
            if (metadata != null) {
                Usage usage = metadata.getUsage();
                if (usage != null) {
                    promptTokens = usage.getPromptTokens();
                    completionTokens = usage.getCompletionTokens();
                    totalTokens = usage.getTotalTokens();
                }
            }
        }

        log.info("LLM call elapsedMs={} promptTokens={} completionTokens={} totalTokens={}",
                elapsedMs, promptTokens, completionTokens, totalTokens);
        metrics.llmLatency(java.time.Duration.ofMillis(elapsedMs));
        // total은 기록하지 않는다. 같은 이름에 prompt/completion/total을 다 올리면
        // 태그 없이 조회했을 때 실제 사용량의 2배가 나온다(docs/6주차/01). total = sum by(type).
        metrics.tokens("prompt", promptTokens);
        metrics.tokens("completion", completionTokens);
    }

    private void logFailure(long elapsedMs, RuntimeException e) {
        log.warn("LLM call failed elapsedMs={} type={} message={}",
                elapsedMs, e.getClass().getSimpleName(), e.getMessage());
    }
}
