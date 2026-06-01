package com.baedal.support;

import com.baedal.assistant.tool.OrderTools;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
public class SupportController {

    private final ChatClient chatClient;

    public SupportController(ChatClient.Builder builder,
                             MessageChatMemoryAdvisor memoryAdvisor,
                             PerformanceLoggingAdvisor performanceAdvisor,
                             OrderTools orderTools) {
        this.chatClient = builder
                .defaultSystem(BaedalPrompt.SYSTEM_PROMPT)
                .defaultAdvisors(memoryAdvisor, performanceAdvisor)
                .defaultTools(orderTools)
                .build();
    }

    @PostMapping
    public SupportResponse triage(@Valid @RequestBody ChatRequest req,
                                  @RequestHeader("X-Session-Id") String sessionId) {
        return chatClient.prompt()
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .entity(SupportResponse.class);
    }
}
