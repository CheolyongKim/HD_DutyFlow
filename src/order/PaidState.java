package order;

public class PaidState implements OrderState {
    @Override
    public void handleCancel(Order order) {
        System.out.println("[취소] 결제 취소 및 환불 처리 중...");
        order.setOrderState(new CancelState());
    }

    @Override
    public void handlePickupRequest(Order order) {
        System.out.println("[픽업준비] 상품을 준비 상태로 변경합니다.");
        order.setOrderState(new PickupReadyState());
    }

    @Override
    public String toString() { return "PAID_SUCCESS"; }
}