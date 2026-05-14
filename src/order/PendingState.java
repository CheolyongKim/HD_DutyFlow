package order;

public class PendingState implements OrderState {
    @Override
    public void handleVerify(Order order) {
        System.out.println("[검증] 상품 및 재고 확인 중...");
        // 예: if (order.getProducts().isEmpty()) throw new ValidationException(ErrorCode.EMPTY_SELECTED_PRODUCTS);
        
        System.out.println("[검증 성공] 결제 대기 상태로 전환합니다.");
        order.setOrderState(new CheckState());
    }

    @Override
    public String toString() { return "ORDER_PENDING"; }
}