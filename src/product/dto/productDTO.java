package product.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import category.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class productDTO {
	
	private Category category;
	private String productName;
	private String brandName;
	private int stockAmount;
	private int capacity;
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private double discountRate;
	private int thresholdValue;
	private LocalDate madeAt;


}