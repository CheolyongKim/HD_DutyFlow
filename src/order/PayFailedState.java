package order;

public class PayFailedState implements OrderState {
    @Override
    public void handlePay(Order order) {
        System.out.println("[재결제] 실패했던 주문에 대해 다시 결제를 시도합니다.");
        // 재시도 시 로직...
    }

    @Override
    public String toString() { return "PAYMENT_FAILED"; }
}