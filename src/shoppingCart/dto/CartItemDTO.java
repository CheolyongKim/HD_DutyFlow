package shoppingCart.dto;

import java.math.BigDecimal;

public record CartItemDTO(
    String productName,
    int quantity,
    BigDecimal dollarPrice,
    BigDecimal wonPrice
) {
}