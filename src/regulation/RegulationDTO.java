package regulation;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegulationDTO {
	private int regulationId;
	private int categoryId;
	private int limitCapacity;
	private LocalDate establishedDate;
	private int overageRate;
}