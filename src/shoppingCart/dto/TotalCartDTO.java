package shoppingCart.dto;

import java.math.BigDecimal;
import java.util.List;

public record TotalCartDTO(
    List<CartItemDTO> items,
    int totalQuantity,
    BigDecimal totalDollarPrice,
    BigDecimal totalWonPrice
) {
}