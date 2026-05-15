package order.state;

import exception.BusinessException;
import exception.ErrorCode;
import order.Order;
import order.OrderState;

public class PendingState implements OrderState {
    @Override
    public String name() {
        return "ORDERED";
    }

    @Override
    public void pay(Order order) {
        throw new BusinessException(ErrorCode.INVALID_ORDER_STATE);
    }
    
    @Override
    public void verify(Order order) {
        order.setState(new VerifiedState());
    }

}