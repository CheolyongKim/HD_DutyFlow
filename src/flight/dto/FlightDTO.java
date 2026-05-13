package flight.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
	private int flightId;
	private String flightCode;
	private LocalDate departureAt;
	private int isDelayed; // 0: 정상, 1: 지연
	
}
