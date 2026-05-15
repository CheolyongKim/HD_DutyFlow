package order;

import exception.BusinessException;
import exception.ErrorCode;

public interface OrderState {
	
    String name();

    default void pay(Order order) {
        throw new BusinessException(ErrorCode.DENIED_PAY);
    }

    default void cancel(Order order) {
        throw new BusinessException(ErrorCode.DENIED_CANCLE);
    }

    default void pickup(Order order) {
        throw new BusinessException(ErrorCode.DENIED_PICKUP);
    }
    void verify(Order order);

	
}

