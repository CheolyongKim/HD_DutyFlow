package tax;

import java.math.BigDecimal;

import order.Order;
import regulation.RegulationDTO;

public class GeneralTaxStrategy implements TaxStrategy{
	
	private final RegulationDTO regulationDTO;
	
	 public GeneralTaxStrategy(RegulationDTO regulationDTO) {
	        this.regulationDTO = regulationDTO;
	 }

	@Override
	public BigDecimal calculateTax(Order order) {
		 
		BigDecimal dutyFreeLimit = BigDecimal.valueOf(regulationDTO.getLimitCapacity());
	     
		BigDecimal taxRate = BigDecimal.valueOf(regulationDTO.getOverageRate()).divide(BigDecimal.valueOf(100));
		
		BigDecimal totalDollarsPrice = order.getTotalPrice();
		BigDecimal exceededAmount = totalDollarsPrice.subtract(dutyFreeLimit);
		
		if (exceededAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		
		return exceededAmount.multiply(taxRate);
	}

}
