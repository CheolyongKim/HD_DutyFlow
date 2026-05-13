package tax;

import java.math.BigDecimal;

import order.Order;

public class AlcoholTaxStrategy implements TaxStrategy{
	
	private static final int ALCOHOL_LIMIT_CAPACITY = 2;
	
	// 초과분 적용 로직 (15%)
	private static final BigDecimal ALCOHOL_TAX_RATE = new BigDecimal("0.70");


	@Override
	public BigDecimal calculateTax(Order order) {
		int totalAlcoholCapacity = order.getTotalAlcohol();
			
		BigDecimal exceededCapacity = BigDecimal.valueOf(totalAlcoholCapacity - ALCOHOL_LIMIT_CAPACITY);

		return exceededCapacity.multiply(ALCOHOL_TAX_RATE);

	}

}
