package product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale.Category;

public abstract class Product {
	private Category category;
	private String productName;
	private String brandName;
	private int stockAmount;
	private int capacity;
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private int discountRate;
	private int thresholdValue;
	private LocalDate madeAt;
}
