package product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale.Category;

public abstract class Product {
	private Category category;
	private String productName;
	private int amoun;
	private int capacity;
	private BigDecimal dollarPrice;
	private BigDecimal wonPrice;
	private int eventSaleRate;
	private int threshole;
	private LocalDate madeAt;
}
