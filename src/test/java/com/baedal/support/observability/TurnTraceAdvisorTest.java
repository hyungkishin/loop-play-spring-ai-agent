package com.baedal.support.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TurnTraceAdvisorTest {

    @Test
    @DisplayName("system과 이번 턴 user를 뺀 메시지 수를 Memory 기여로 센다")
    void countsMemoryMessages() {
        List<Message> instructions = List.of(
                new SystemMessage("sys"),
                new UserMessage("이전 질문"),
                new AssistantMessage("이전 답"),
                new UserMessage("이번 질문"));

        assertThat(TurnTraceAdvisor.memoryMessageCount(instructions)).isEqualTo(2);
    }

    @Test
    @DisplayName("첫 턴(과거 대화 없음)은 0이다")
    void firstTurnHasNoMemory() {
        assertThat(TurnTraceAdvisor.memoryMessageCount(
                List.of(new SystemMessage("sys"), new UserMessage("안녕하세요")))).isZero();
    }

    @Test
    @DisplayName("검색 문서는 faqId와 점수로 요약하고, 없으면 []")
    void summarizesRetrievedDocs() {
        Document doc = Document.builder()
                .id("chunk-1")
                .text("비 오는 날 지연 보상")
                .metadata(Map.of("faqId", "weather-delay"))
                .score(0.7312)
                .build();

        assertThat(TurnTraceAdvisor.ragSummary(List.of(doc))).isEqualTo("[weather-delay(0.73)]");
        assertThat(TurnTraceAdvisor.ragSummary(null)).isEqualTo("[]");
        assertThat(TurnTraceAdvisor.ragSummary(List.of())).isEqualTo("[]");
    }
}
