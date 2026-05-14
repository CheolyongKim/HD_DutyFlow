package order;

import exception.BusinessException;
import exception.ErrorCode;

public class CheckState implements OrderState {
    @Override
    public void handlePay(Order order) {
        try {
            System.out.println("[결제] 프로세스 시작...");
            boolean isSuccess = true; // 결제 로직 시뮬레이션

            if (isSuccess) {
                order.setOrderState(new PaidState());
            } else {
                throw new BusinessException(ErrorCode.ORDER_PAYMENT_FAILED);
            }
        } catch (Exception e) {
            order.setOrderState(new PayFailedState());
            order.saveFailedOrder(); // 결제 실패 시 직렬화 수행
            throw e;
        }
    }

    @Override
    public String toString() { return "CHECK_PAYMENT"; }
}