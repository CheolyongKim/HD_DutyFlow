package order;

import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import product.Product;

public class Order {
	private int flightResNum;
	// private OrderState orderState;
	private String loginId;
	private List<Map<Product, Integer>> products;
	private BigDecimal totalPrice;
	private int totalPerfume;
	private int totalAlcohol;
	private LocalDateTime orderedAt;
	private BigDecimal discountPrice;
}
