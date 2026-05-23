package com.baedal.assistant.tool;

import com.baedal.assistant.domain.Order;
import com.baedal.assistant.service.OrderMockService;
import com.baedal.assistant.tool.view.CancelOrderResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("단계 2 실험, 멱등성 분기 제거 시 무엇이 깨지는지 raw 관찰")
class OrderToolsIdempotencyObservationTest {

    @Test
    @DisplayName("이미 취소된 주문(2024-1238) 재취소 → 실제 message / outcome 출력")
    void observe_alreadyCanceled() {
        OrderMockService service = new OrderMockService();
        service.seed();
        OrderTools tools = new OrderTools(service);

        Order before = service.findById("2024-1238").orElseThrow();
        System.out.println("[before] status=" + before.status()
                + " canceledReason=" + before.canceledReason());

        CancelOrderResult r = tools.cancelOrder("2024-1238", "한 번 더 취소");
        System.out.println("[cancel re-request] outcome=" + r.outcome() + " message=" + r.message());

        Order after = service.findById("2024-1238").orElseThrow();
        System.out.println("[after]  status=" + after.status()
                + " canceledReason=" + after.canceledReason());
    }

    @Test
    @DisplayName("정상 주문(2024-1239)을 두 번 연속 취소 → 두 번째 호출의 동작 출력")
    void observe_doubleCancel() {
        OrderMockService service = new OrderMockService();
        service.seed();
        OrderTools tools = new OrderTools(service);

        CancelOrderResult first = tools.cancelOrder("2024-1239", "집 앞에 사람이 없어요");
        System.out.println("[1st] outcome=" + first.outcome() + " message=" + first.message());

        Order between = service.findById("2024-1239").orElseThrow();
        System.out.println("[between] status=" + between.status()
                + " canceledReason=" + between.canceledReason());

        CancelOrderResult second = tools.cancelOrder("2024-1239", "한 번 더 확인 부탁드려요");
        System.out.println("[2nd] outcome=" + second.outcome() + " message=" + second.message());

        Order after = service.findById("2024-1239").orElseThrow();
        System.out.println("[after] status=" + after.status()
                + " canceledReason=" + after.canceledReason());
    }
}
