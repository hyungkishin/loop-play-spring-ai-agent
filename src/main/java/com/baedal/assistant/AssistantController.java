package com.baedal.assistant;

import com.baedal.assistant.tool.OrderTools;
import com.baedal.support.ChatRequest;
import com.baedal.support.PerformanceLoggingAdvisor;
import com.baedal.support.guardrail.HandoffDetector;
import com.baedal.support.guardrail.HandoffDetector.HandoffResult;
import com.baedal.support.guardrail.InputGuardrailAdvisor;
import com.baedal.support.guardrail.OutputGuardrailAdvisor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final ChatClient chatClient;
    private final HandoffDetector handoffDetector;

    public AssistantController(ChatClient.Builder builder,
                               InputGuardrailAdvisor inputGuardrail,
                               MessageChatMemoryAdvisor memoryAdvisor,
                               QuestionAnswerAdvisor ragAdvisor,
                               OutputGuardrailAdvisor outputGuardrail,
                               PerformanceLoggingAdvisor performanceAdvisor,
                               HandoffDetector handoffDetector,
                               OrderTools orderTools) {
        this.handoffDetector = handoffDetector;
        this.chatClient = builder
                .defaultSystem(AssistantPrompt.SYSTEM_PROMPT)
                // Round 5: inputGuardrail(5) → memory(10) → rag(20) → outputGuardrail(50) → performance(100).
                // 입력 Guardrail은 가장 바깥(5)에서 차단 발화가 Memory에 저장되기 전에 잘라내고,
                // 출력 Guardrail(50)은 모델 응답을 받은 뒤 마스킹하되 performance(100)보다는 바깥에 둬
                // performance가 마스킹 전 날 응답의 토큰을 재게 한다.
                .defaultAdvisors(inputGuardrail, memoryAdvisor, ragAdvisor, outputGuardrail, performanceAdvisor)
                .defaultTools(orderTools)
                .build();
    }

    @PostMapping
    public String ask(@Valid @RequestBody ChatRequest req,
                      @RequestHeader("X-Session-Id") String sessionId) {
        // LLM 호출 전에 상담원 전환을 먼저 본다. 전환이면 모델을 부르지 않고 연결 안내를 바로 돌려준다.
        HandoffResult handoff = handoffDetector.detect(req.message());
        if (handoff.handoff()) {
            log.info("[Handoff] trigger={} — LLM 호출 없음", handoff.trigger());
            return handoff.message();
        }
        return chatClient.prompt()
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
