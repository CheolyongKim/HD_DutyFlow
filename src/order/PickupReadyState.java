package order;

public class PickupReadyState implements OrderState {
    @Override
    public void handlePickupComplete(Order order) {
        System.out.println("[픽업완료] 상품 전달 완료.");
        order.setOrderState(new PickupDoneState());
    }

    @Override
    public void handleNoShow(Order order) {
        System.out.println("[노쇼] 미방문 처리.");
        order.setOrderState(new NoShowState());
    }

    @Override
    public String toString() { return "PICKUP_READY"; }
}