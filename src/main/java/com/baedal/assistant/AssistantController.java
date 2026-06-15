package com.baedal.assistant;

import com.baedal.assistant.tool.OrderTools;
import com.baedal.support.ChatRequest;
import com.baedal.support.PerformanceLoggingAdvisor;
import com.baedal.support.guardrail.InputGuardrailAdvisor;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final ChatClient chatClient;

    public AssistantController(ChatClient.Builder builder,
                               InputGuardrailAdvisor inputGuardrail,
                               MessageChatMemoryAdvisor memoryAdvisor,
                               QuestionAnswerAdvisor ragAdvisor,
                               PerformanceLoggingAdvisor performanceAdvisor,
                               OrderTools orderTools) {
        this.chatClient = builder
                .defaultSystem(AssistantPrompt.SYSTEM_PROMPT)
                // Round 5: inputGuardrail(5) → memory(10) → rag(20) → performance(100).
                // 입력 Guardrail을 가장 바깥(5)에 두어 차단 발화가 Memory(10)에 저장되기 전에 잘라낸다.
                // (OutputGuardrail(50)은 2단계에서 memory·rag 뒤, performance 앞에 끼운다.)
                .defaultAdvisors(inputGuardrail, memoryAdvisor, ragAdvisor, performanceAdvisor)
                .defaultTools(orderTools)
                .build();
    }

    @PostMapping
    public String ask(@Valid @RequestBody ChatRequest req,
                      @RequestHeader("X-Session-Id") String sessionId) {
        return chatClient.prompt()
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
