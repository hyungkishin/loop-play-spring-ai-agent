package com.baedal.support.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Round 6 — 한 턴에서 Memory와 RAG가 실제로 무엇을 붙였는지 한 줄로 남기는 추적 Advisor (order=30).
 *
 * <p>Memory(10)·RAG(20)가 요청을 가공한 뒤, OutputGuardrail(50)보다 안쪽에 둔다.
 * 이 자리에서는 이미 과거 대화가 prompt instructions에 끼어 있고, 검색 문서가 context에 들어 있다.
 * 10턴 E2E에서 "이 턴은 Memory 덕분"이라는 말을 추정이 아니라 로그로 확인하려고 붙였다.</p>
 *
 * <p>Tool 호출은 모델 호출 안쪽(ToolCallingManager)에서 일어나므로 여기서는 못 본다.
 * Tool 기여는 {@code [Tool] ...} 로그와 {@code baedal.agent.tool.invoke} 메트릭으로 본다.</p>
 */
@Slf4j
@Component
public class TurnTraceAdvisor implements CallAdvisor {

    @Override
    public String getName() {
        return "TurnTraceAdvisor";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        log.info("[Turn] conversationId={} memoryMessages={} ragDocs={}",
                request.context().get(ChatMemory.CONVERSATION_ID),
                memoryMessageCount(request.prompt().getInstructions()),
                ragSummary(request.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS)));
        return chain.nextCall(request);
    }

    /** system 메시지와 이번 턴 user 메시지를 뺀 나머지 = Memory가 끼워 넣은 과거 대화 수. */
    static int memoryMessageCount(List<Message> instructions) {
        long nonSystem = instructions.stream()
                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                .count();
        return (int) Math.max(0, nonSystem - 1);
    }

    static String ragSummary(Object retrieved) {
        if (!(retrieved instanceof List<?> docs) || docs.isEmpty()) {
            return "[]";
        }
        return docs.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .map(d -> d.getMetadata().getOrDefault("faqId", d.getId())
                        + (d.getScore() == null ? "" : String.format(Locale.ROOT, "(%.2f)", d.getScore())))
                .toList()
                .toString();
    }
}
