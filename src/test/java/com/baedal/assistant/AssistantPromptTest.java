package com.baedal.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantPromptTest {

    @Test
    void systemPrompt_locksNeutralBusinessTone() {
        assertThat(AssistantPrompt.SYSTEM_PROMPT)
                .contains("사무적이고 단정한 존댓말")
                .contains("감탄, 과한 공감, 애교체, 이모지, 물결표")
                .contains("확인된 사실과 필요한 다음 단계만 말합니다");
    }

    @Test
    void systemPrompt_resolvesOrderReferenceFromMemoryBeforeAskingAgain() {
        // gemma4 10턴 E2E에서 "발화에 없다면 되묻는다"를 문자 그대로 읽고 Memory에 있는 주문번호를 다시 물었다.
        assertThat(AssistantPrompt.SYSTEM_PROMPT)
                .contains("이전 대화에서 가장 최근에 언급된 주문번호")
                .contains("현재 발화와 이전 대화 어디에도 주문번호가 없을 때만")
                .doesNotContain("주문번호가 발화에 없다면");
    }
}
