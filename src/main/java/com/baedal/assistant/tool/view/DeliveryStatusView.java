package com.baedal.assistant.tool.view;

/**
 * 배달 상태 View. 예상 도착 시각은 {@link KoreanTime} 문자열("9월 25일 오전 3시 40분")로 넘긴다.
 */
public record DeliveryStatusView(
        String orderId,
        String status,
        String riderLocation,
        String estimatedDeliveryAt
) {}
