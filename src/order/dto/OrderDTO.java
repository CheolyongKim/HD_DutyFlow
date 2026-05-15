package order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import order.OrderState;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

	//category
	private int categoryId;
	private String categoryName;
	private int capacity;
    
	
    // Orders
    private int orderId;
    private int memberId;
    private int reservationId;
    private LocalDate exchangeDate;
    private LocalDateTime orderedAt;
    private OrderState orderState;
    private BigDecimal totalAmount;

    // OrderDetail
    private int productId;
    private int quantity;
    private BigDecimal discountPrice;
    private BigDecimal dollarPrice;

    // Product
    private String productName;
}
