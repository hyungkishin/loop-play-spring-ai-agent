package com.baedal.assistant.tool.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Round 6 — Tool 결과의 시각을 모델이 다시 해석하지 않도록 사람이 읽는 문자열로 넘긴다.
 *
 * <p>{@code LocalDateTime}을 그대로 넘겼을 때 gemma4가 새벽 03:40을 "15시 40분", "오후 3시 40분"으로
 * 10번 중 7번 바꿔 말했다. 해석을 모델에 맡기지 않고 View에서 오전/오후까지 확정한다. (docs/5주차/06)</p>
 */
public final class KoreanTime {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("M월 d일 a h시 m분", Locale.KOREAN);

    private KoreanTime() {}

    public static String format(LocalDateTime time) {
        return time == null ? null : time.format(FORMAT);
    }
}
