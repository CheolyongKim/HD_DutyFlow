package payment;

import java.math.BigDecimal;

import exception.ErrorCode;
import exception.ValidationException;

public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentQueue paymentQueue = PaymentQueue.getInstance();

    // 결제 요청 접수
    public int requestPayment(PaymentDTO paymentDTO) {
        validatePaymentRequest(paymentDTO);

        // 카드번호 형식/Luhn 검증 실패 시 결제 요청 자체를 막음
        if (!isValidCardNumber(paymentDTO.getCardNumber())) {
            throw new ValidationException(ErrorCode.INVALID_CARD_NUMBER);
        }

        int paymentId = paymentDAO.insertPendingPayment(paymentDTO);

        paymentQueue.enqueue(paymentId);

        return paymentId;
    }

    // 결제 요청 시 데이터 검증
    private void validatePaymentRequest(PaymentDTO paymentDTO) {
    	// paymentDTO가 null인 경우
        if (paymentDTO == null) { 
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        // 결제 금액이 null이거나 0이하인 경우
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 카드 번호가 null이거나 비어있을 경우
        if (paymentDTO.getCardNumber() == null || paymentDTO.getCardNumber().isBlank()) {
            throw new ValidationException(ErrorCode.INVALID_CARD_NUMBER);
        }
    }

    // 카드번호 검증
    private boolean isValidCardNumber(String cardNumber) {
    	// 숫자가 아닌 문자는 제거하는 정규화 과정
    	String onlyDigits = cardNumber.replaceAll("[^0-9]", "");

        // 16자리 숫자인지 정규식 검사
        if (!onlyDigits.matches("\\d{16}")) {
            return false;
        }

        return luhnCheck(onlyDigits);
    }

    // Luhn 알고리즘 검증
    // 카드번호가 유효한지 검증하는 체크섬 기반 알고리즘
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = cardNumber.charAt(i) - '0';

            if (doubleDigit) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }
}