package order.state;

import order.Order;
import order.OrderState;

public class PendingState implements OrderState {

    @Override
    public String name() {
        return "ORDERED";
    }

    // ORDERED → VERIFIED
    @Override
    public void verify(Order order) {
        order.setState(new VerifiedState());
    }

    // ORDERED → CANCELED
    @Override
    public void cancel(Order order) {
        order.setState(new CanceledState());
    }
}