package order;

import exception.BusinessException;
import exception.ErrorCode;

public interface OrderState {
    default void handlePay(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }

    default void handleCancel(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }

    default void handlePickup(Order order) {
        throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
    }
}