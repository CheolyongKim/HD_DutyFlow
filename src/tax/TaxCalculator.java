package tax;

import java.math.BigDecimal;
import java.util.List;

import order.Order;

public class TaxCalculator {

	private List<TaxStrategy> strategies;
	
	public TaxCalculator(List<TaxStrategy> strategies) {
		this.strategies = strategies;
	}
	
	public BigDecimal calculateTax(Order order) {
		BigDecimal total = BigDecimal.ZERO;
		
		for(TaxStrategy strategy : strategies) {
			total = total.add(strategy.calculateTax(order));
		}

		return total;
	}
	
	
	
	
}
