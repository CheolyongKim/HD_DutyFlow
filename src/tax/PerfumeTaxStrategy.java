package tax;

import java.math.BigDecimal;

import order.Order;

public class PerfumeTaxStrategy implements TaxStrategy{
	
	private static final int Perfume_LIMIT_CAPACITY = 100;
	
	// 초과분 적용 로직 (15%)
	private static final BigDecimal Perfume_TAX_RATE = new BigDecimal("0.15");

	@Override
	public BigDecimal calculateTax(Order order) {
		int totalPerfumeCapacity = order.getTotalAlcohol();
		
		BigDecimal exceededCapacity = BigDecimal.valueOf(totalPerfumeCapacity - Perfume_LIMIT_CAPACITY);

		return exceededCapacity.multiply(Perfume_TAX_RATE);
 
	}

}
