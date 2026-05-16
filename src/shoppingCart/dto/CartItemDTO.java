package shoppingCart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartItemDTO {
    private final int productId;     
    private final String productName;
    private final int quantity;
    private final BigDecimal dollarPrice;
    private final BigDecimal wonPrice;
}