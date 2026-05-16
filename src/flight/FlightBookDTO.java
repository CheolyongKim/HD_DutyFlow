package flight;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FlightBookDTO {

    private int reservationId;
    private int memberId;
    private int flightId;
    private String reservationCode;
}