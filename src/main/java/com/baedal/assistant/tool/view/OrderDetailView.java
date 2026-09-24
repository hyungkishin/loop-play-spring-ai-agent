package com.baedal.assistant.tool.view;

import java.util.List;

/**
 * 주문 상세 View. 시각은 {@link KoreanTime} 문자열("9월 25일 오전 3시 40분")로 넘긴다.
 */
public record OrderDetailView(
        String orderId,
        String storeName,
        List<Line> items,
        int totalAmount,
        String status,
        String orderedAt,
        String estimatedDeliveryAt
) {
    public record Line(String menuName, int quantity, int unitPrice) {}
}
