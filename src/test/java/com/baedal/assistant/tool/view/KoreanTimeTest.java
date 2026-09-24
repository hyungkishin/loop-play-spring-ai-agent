package com.baedal.assistant.tool.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class KoreanTimeTest {

    @Test
    @DisplayName("새벽 시각은 오전으로 확정해 넘긴다 (gemma4가 03:40을 오후 3시 40분으로 바꿔 말한 사례)")
    void earlyMorningIsAm() {
        assertThat(KoreanTime.format(LocalDateTime.of(2026, 9, 25, 3, 40))).isEqualTo("9월 25일 오전 3시 40분");
    }

    @Test
    @DisplayName("오후 시각은 12시간제 + 오후로 넘긴다")
    void afternoonIsPm() {
        assertThat(KoreanTime.format(LocalDateTime.of(2026, 9, 25, 15, 40))).isEqualTo("9월 25일 오후 3시 40분");
    }

    @Test
    void nullStaysNull() {
        assertThat(KoreanTime.format(null)).isNull();
    }
}
