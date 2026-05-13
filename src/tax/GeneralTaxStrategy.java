package tax;

import java.math.BigDecimal;

import order.Order;

public class GeneralTaxStrategy implements TaxStrategy{
	
	//  면세 한도 (800달러)
	private static final BigDecimal DUTY_FREE_LIMIT_USD = new BigDecimal("800");
	
	// 초과분 적용 로직 (일반상품 - 15%)
	private static final BigDecimal GENERAL_TAX_RATE = new BigDecimal("0.15");

	@Override
	public BigDecimal calculateTax(Order order) {
		BigDecimal totalDollarsPrice = order.getTotalPrice();
		
		BigDecimal exceededAmount = totalDollarsPrice.subtract(DUTY_FREE_LIMIT_USD);
		
		return exceededAmount.multiply(GENERAL_TAX_RATE);
	}

}
