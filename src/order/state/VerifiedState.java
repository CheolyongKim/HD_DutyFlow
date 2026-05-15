package order.state;

import exception.BusinessException;
import exception.ErrorCode;
import order.Order;
import order.OrderState;

public class VerifiedState implements OrderState {

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "VERIFIED";
	}

    @Override
    public void verify(Order order) {
        throw new BusinessException(ErrorCode.INVALID_ORDER_STATE);
    }

    @Override
    public void pay(Order order) {
        order.setState(new PaidState());
    }

}
