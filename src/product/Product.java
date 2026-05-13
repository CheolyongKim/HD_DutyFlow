package product;

import java.math.BigDecimal;
import java.time.LocalDate;

import category.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
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