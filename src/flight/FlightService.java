package flight;

import java.time.LocalDateTime;

import airplane.Airplane;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.ValidationException;

public class FlightService {

    private final FlightDAO flightDAO;

    public FlightService(FlightDAO flightDAO) {
        this.flightDAO = flightDAO;
    }
    
    private void validateFlightCode(String flightCode) {
        if (flightCode == null || flightCode.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }
 
    // 항공편 조회 (예약코드 기반)
    public Airplane getFlightInfo(String reservationCode) {
    	
    	validateFlightCode(reservationCode);

        FlightDTO dto = flightDAO.getFlightByReservationCode(reservationCode);

        if (dto == null) {
            throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
        }

        return new Airplane(
                dto.getFlightId(),
                dto.getFlightCode(),
                dto.getDepartureAt()
        );
    }
    
    
    // 예약 정보 조회 (회원Id, 항공편 코드 기반)
    public FlightBookDTO getBookByMemberAndFlight(int memberId, String flightCode) {

    	validateFlightCode(flightCode);
    	
    	if (memberId <= 0) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    	
        FlightBookDTO bookDto = flightDAO.getBookByMemberAndFlight(memberId, flightCode);

        if (bookDto == null) {
            throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
        }

        return bookDto;
    }
    


    // 항공편 지연 정보 업데이트 
    public void updateDelayedFlight(String flightCode, LocalDateTime newDepartureAt) {

        if (flightCode == null || newDepartureAt == null) {
        	throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        flightDAO.updateDelayedFlight(flightCode, newDepartureAt);
    }
}