package payment.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import payment.PaymentDTO;
import payment.PaymentService;
import payment.PaymentWorker;

public class PaymentTest {

    public static void main(String[] args) {

        PaymentService paymentService = new PaymentService();
        PaymentWorker paymentWorker = new PaymentWorker();

        List<PaymentDTO> paymentRequests = new ArrayList<>();

        // =========================
        // 1. 정상 결제 요청 - 기본 카드번호
        // =========================
        paymentRequests.add(new PaymentDTO(1, new BigDecimal("50000"), "4111111111111111"));

        // =========================
        // 2. 정상 결제 요청 - 하이픈 포함 카드번호
        // =========================
        paymentRequests.add(new PaymentDTO(2, new BigDecimal("120000"), "4111-1111-1111-1111"));

        // =========================
        // 3. 정상 결제 요청 - 공백 포함 카드번호
        // =========================
        paymentRequests.add(new PaymentDTO(3, new BigDecimal("30000"), "4111 1111 1111 1111"));

        // =========================
        // 4. 정상 결제 요청 - 큰 금액
        // =========================
        paymentRequests.add(new PaymentDTO(4, new BigDecimal("410000"), "4111111111111111"));

        // =========================
        // 5. 정상 결제 요청 - 추가 테스트 1
        // =========================
        paymentRequests.add(new PaymentDTO(5, new BigDecimal("150000"), "4111111111111111"));

        // =========================
        // 6. 정상 결제 요청 - 추가 테스트 2
        // =========================
        paymentRequests.add(new PaymentDTO(6, new BigDecimal("230000"), "4111111111111111"));

        // =========================
        // 7. 정상 결제 요청 - 추가 테스트 3
        // =========================
        paymentRequests.add(new PaymentDTO(7, new BigDecimal("99000"), "4111111111111111"));

        // =========================
        // 8. 정상 결제 요청 - 추가 테스트 4
        // =========================
        paymentRequests.add(new PaymentDTO(8, new BigDecimal("76000"), "4111111111111111"));

        // =========================
        // 9. 예외 테스트 - Luhn 검증 실패
        // =========================
        paymentRequests.add(new PaymentDTO(9, new BigDecimal("30000"), "1234-5678-1234-5678"));

        // =========================
        // 10. 예외 테스트 - 결제 금액 0원
        // =========================
        paymentRequests.add(new PaymentDTO(10, new BigDecimal("0"), "4111111111111111"));

        // =========================
        // 11. 예외 테스트 - 결제 금액 음수
        // =========================
        paymentRequests.add(new PaymentDTO(11, new BigDecimal("-1000"), "4111111111111111"));

        // =========================
        // 12. 예외 테스트 - 카드번호 null
        // =========================
        paymentRequests.add(new PaymentDTO(12, new BigDecimal("50000"), null));

        // =========================
        // 13. 예외 테스트 - 카드번호 공백
        // =========================
        paymentRequests.add(new PaymentDTO(13, new BigDecimal("50000"), " "));

        System.out.println("===== 결제 요청 접수 시작 =====");

        for (PaymentDTO request : paymentRequests) {
            try {
                int paymentId = paymentService.requestPayment(request);

                System.out.println("결제 요청 접수 완료 | orderId = "
                        + request.getOrderId()
                        + " | paymentId = "
                        + paymentId
                        + " | status = PENDING");
            } catch (Exception e) {
                System.out.println("결제 요청 접수 실패 | orderId = "
                        + request.getOrderId()
                        + " | reason = "
                        + e.getMessage());
            }

            System.out.println();
        }

        System.out.println("===== 결제 Queue 처리 시작 =====");

        paymentWorker.processAll();

        System.out.println("===== 결제 Queue 처리 종료 =====");
    }
}