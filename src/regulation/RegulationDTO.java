package regulation;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RegulationDTO {

	private int regulationId;
    private int categoryId;
    private int limitCapacity;       
    private LocalDate establishedDate;
    private int overageRate;
    
}