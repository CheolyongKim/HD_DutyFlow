package flight;

import java.time.LocalDate;
import java.time.LocalDateTime;

import airplane.Airplane;

public class FlightService {

    private final FlightDAO flightDAO;

    public FlightService(FlightDAO flightDAO) {
        this.flightDAO = flightDAO;
    }
 
    // 항공편 조회 (예약코드 기반)
    public Airplane getFlightInfo(String reservationCode) {

        FlightDTO dto = flightDAO.getFlightByReservationCode(reservationCode);

        if (dto == null) {
            System.out.println("[FlightService] 항공편 없음");
            return null;
        }

        return new Airplane(
                dto.getFlightId(),
                dto.getFlightCode(),
                dto.getDepartureAt()
        );
    }
    


    // 지연 정보 처리
    public void updateDelayedFlight(String flightCode, LocalDateTime newDepartureAt) {

        if (flightCode == null || newDepartureAt == null) {
            System.out.println("[FlightService] 잘못된 입력값");
            return;
        }

        flightDAO.updateDelayedFlight(flightCode, newDepartureAt);

        System.out.println("[FlightService] 지연 업데이트 " + flightCode + " -> " + newDepartureAt);
    }
}