package com.baedal.assistant;

import com.baedal.assistant.tool.OrderTools;
import com.baedal.support.ChatRequest;
import com.baedal.support.PerformanceLoggingAdvisor;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
                               MessageChatMemoryAdvisor memoryAdvisor,
                               PerformanceLoggingAdvisor performanceAdvisor,
                               OrderTools orderTools) {
        this.chatClient = builder
                .defaultSystem(AssistantPrompt.SYSTEM_PROMPT)
                .defaultAdvisors(memoryAdvisor, performanceAdvisor)
                .defaultTools(orderTools)
                .build();
    }

    @PostMapping
    public String ask(@Valid @RequestBody ChatRequest req,
                      @RequestHeader(value = "X-Session-Id", defaultValue = ChatMemory.DEFAULT_CONVERSATION_ID)
                      String sessionId) {
        return chatClient.prompt()
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
