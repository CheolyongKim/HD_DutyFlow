package payment;

public class PaymentWorker {

    private final PaymentQueue paymentQueue = PaymentQueue.getInstance();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Queue에 쌓인 결제 요청을 순차 처리
    public void processAll() {
        while (!paymentQueue.isEmpty()) { // Queue에 결제 요청이 남아있는 동안 반복 처리
            int paymentId = paymentQueue.dequeue();
            
            System.out.println("[WORKER] 결제 처리 시작 | paymentId = " + paymentId);

            Payment payment = paymentDAO.findById(paymentId);

            if (payment == null) {
                System.out.println("[WORKER] 결제 정보 없음 | paymentId = " + paymentId);
                paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_NOT_FOUND");
                continue;
            }

            paymentDAO.updateStatusToProcessing(paymentId);
            Payment processingPayment = paymentDAO.findById(paymentId);
            
            System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + processingPayment.getPaymentStatus());
            
            try {
                // 카드번호 검증은 requestPayment 단계에서 이미 완료됨
                // Worker에서는 실제 결제 승인 처리만 Mock으로 성공 처리
                paymentDAO.updateStatusToSuccess(paymentId);
                Payment successPayment = paymentDAO.findById(paymentId);
                System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + successPayment.getPaymentStatus());

                // 추후 OrderStatus 업데이트 메서드 추가
            } catch (Exception e) {
                paymentDAO.updateStatusToFailed(paymentId, "PAYMENT_PROCESSING_ERROR");
                System.out.println("결제 실패: paymentId = " + paymentId);
                
                Payment failedPayment = paymentDAO.findById(paymentId);
                System.out.println("[PAYMENT] 상태 변경 완료 | paymentId = " + paymentId + " | currentStatus = " + failedPayment.getPaymentStatus());
            }
        }
    }
}