package order.state;

import order.Order;
import order.OrderState;

public class PendingState implements OrderState {
    @Override
    public String name() {
        return "ORDERED";
    }

    @Override
    public void pay(Order order) {
        order.setState(new PaidState());
    }

}