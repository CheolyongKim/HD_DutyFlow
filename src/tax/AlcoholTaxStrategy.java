package tax;

import java.math.BigDecimal;

import order.Order;
import regulation.RegulationDTO;

public class AlcoholTaxStrategy implements TaxStrategy{

	private final RegulationDTO regulationDTO;

	public AlcoholTaxStrategy(RegulationDTO regulationDTO) {
	        this.regulationDTO = regulationDTO;
	 }

	@Override
    public BigDecimal calculateTax(Order order) {

        int limitCapacity = regulationDTO.getLimitCapacity();
        
        BigDecimal taxRate = BigDecimal.valueOf(regulationDTO.getOverageRate())
                                      .divide(BigDecimal.valueOf(100)); 

        int totalAlcohol = order.getTotalAlcohol();
        int exceeded = totalAlcohol - limitCapacity;

        if (exceeded <= 0) {
        	return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(exceeded).multiply(taxRate);
    }


}
