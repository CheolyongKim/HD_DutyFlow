package flight;

import java.time.LocalDate;
import java.time.LocalDateTime;

import airplane.Airplane;
import exception.BusinessException;
import exception.ErrorCode;

public class FlightService {

    private final FlightDAO flightDAO;

    //비행기 예약 번호의 정규식
    private static final String RESV_CODE_REGEX = "^RESV-[A-Z]{2}\\d{3}-\\d{3}$";
    
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
    
    /**
     * 예약 코드 유효성 검증 (정규식 + DB 존재 여부)
     */
    public void validateReservationCode(String reservationCode) {
        // 1. 형식 검증 (정규식)
        if (reservationCode == null || !reservationCode.matches(RESV_CODE_REGEX)) {
            System.out.println("[FlightService] 유효하지 않은 예약 코드 형식: " + reservationCode);
            throw new BusinessException(ErrorCode.INVALID_INPUT); // 적절한 에러코드 사용
        }

        // 2. DB 존재 여부 검증
        FlightDTO flight = flightDAO.getFlightByReservationCode(reservationCode);
        if (flight == null) {
            System.out.println("[FlightService] 존재하지 않는 예약 코드: " + reservationCode);
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND); 
        }
    }
    
}