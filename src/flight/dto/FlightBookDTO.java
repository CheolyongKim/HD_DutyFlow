package flight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightBookDTO {
    private int reservationId;
    private int memberId;
    private int flightId;
    private String reservationCode;  
}