package product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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