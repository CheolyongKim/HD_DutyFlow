package payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Payment {
    private final int paymentId;
    private final int orderId;
    private final String paymentMethod;
    private final PaymentStatus paymentStatus;
    private final BigDecimal requestedAmount;
    private final String cardNumberMask;
    private final LocalDateTime requestedAt;
    private final LocalDateTime processedAt;
    private final String failReason;
}