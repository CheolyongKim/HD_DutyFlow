package cart;

import java.math.BigDecimal;

public record CartItemDto(
    String productName,
    int quantity,
    BigDecimal dollarPrice,
    BigDecimal wonPrice
) {
}