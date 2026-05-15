package payment;

public class PaymentWorker {

    private final PaymentQueue paymentQueue = PaymentQueue.getInstance();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Queue에 쌓인 결제 요청을 순차 처리
    public void processAll() {
        while (!paymentQueue.isEmpty()) { // Queue에 결제 요청이 남아있는 동안 반복 처리
            int paymentId = paymentQueue.dequeue();

            Payment payment = paymentDAO.findById(paymentId);

            if (payment == null) {
                paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_NOT_FOUND");
                continue;
            }

            paymentDAO.updateStatusToProcessing(paymentId);

            try {
                // 카드번호 검증은 requestPayment 단계에서 이미 완료됨
                // Worker에서는 실제 결제 승인 처리만 Mock으로 성공 처리
                paymentDAO.updateStatusToSuccess(paymentId);
                System.out.println("결제 성공: paymentId = " + paymentId);

                // 추후 OrderStatus 업데이트 메서드 추가
            } catch (Exception e) {
                paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_PROCESSING_ERROR");
                System.out.println("결제 실패: paymentId = " + paymentId);
            }
        }
    }
}