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
	private int amount;
	private int capacity;
	private BigDecimal dollarPrice;
	private BigDecimal wonPrice;
	private double eventSaleRate;
	private int threshold;
	private LocalDate madeAt;

}