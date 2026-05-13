package cart;

import java.math.BigDecimal;
import java.util.List;

public record TotalCartDto(
    List<CartItemDto> items,
    int totalQuantity,
    BigDecimal totalDollarPrice,
    BigDecimal totalWonPrice
) {
}