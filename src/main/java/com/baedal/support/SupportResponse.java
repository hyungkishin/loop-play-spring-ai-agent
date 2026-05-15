package com.baedal.support;

import java.util.List;

public record SupportResponse(
        String summary,
        Category category,
        Urgency urgency,
        String nextAction,
        List<String> neededInfo,
        Integer estimatedResolutionMinutes,
        Confidence confidenceLevel
) {
    public enum Category {
        ORDER,
        DELIVERY,
        QUALITY,
        SAFETY,
        PAYMENT,
        REFUND,
        ETC
    }

    public enum Urgency { LOW, NORMAL, HIGH, CRITICAL }

    public enum Confidence { LOW, MEDIUM, HIGH }
}
