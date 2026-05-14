package tax;

import java.math.BigDecimal;

import order.Order;
import regulation.RegulationDTO;

public class PerfumeTaxStrategy implements TaxStrategy{

	private final RegulationDTO regulationDTO;

	public PerfumeTaxStrategy(RegulationDTO regulationDTO) {
	        this.regulationDTO = regulationDTO;
	 }

	@Override
    public BigDecimal calculateTax(Order order) {
		
        int limitCapacity = regulationDTO.getLimitCapacity();
        
        BigDecimal taxRate = BigDecimal.valueOf(regulationDTO.getOverageRate())
                                      .divide(BigDecimal.valueOf(100));

        int totalPerfume = order.getTotalPerfume();
        int exceeded = totalPerfume - limitCapacity;

        if (exceeded <= 0) {
        	return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(exceeded).multiply(taxRate);
    }

}
