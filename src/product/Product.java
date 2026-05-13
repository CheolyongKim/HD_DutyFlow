package product;

import java.math.BigDecimal;
import java.time.LocalDate;
import category.Category;

public class Product {
	private Category category;
	private String productName;
	private int amount;
	private int capacity;
	private BigDecimal dollarPrice;
	private BigDecimal wonPrice;
	private int eventSaleRate;
	private int threshold;
	private LocalDate madeAt;
}
