package order;

import exception.BusinessException;
import exception.ErrorCode;

public interface OrderState {
    // 1. 검증 (Base -> Check)
    default void handleVerify(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    // 2. 결제 (Check -> Paid / PayFailed)
    default void handlePay(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    // 3. 주문 취소 (Paid -> Cancel)
    default void handleCancel(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    // 4. 픽업 요청 (Paid -> PickupReady)
    default void handlePickupRequest(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    // 5. 픽업 완료 (PickupReady -> PickupDone)
    default void handlePickupComplete(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
    // 6. 노쇼 처리 (PickupReady -> NoShow)
    default void handleNoShow(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
}