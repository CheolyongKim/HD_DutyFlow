package order;

import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import product.Product;

public class Order {
	private int flighResNum;
	// private OrderState orderState;
	private String loginId;
	private List<Map<Product, Integer>> products;
	private BigDecimal totalPrice;
	private LocalDateTime orderedAt;
	private BigDecimal discountPrice;
}
