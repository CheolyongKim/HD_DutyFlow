package pickup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.AirportManagerService;
import airplane.Airplane;
import common.CurrentTime;
import exception.BusinessException;
import exception.ErrorCode;
import exception.ValidationException;
import flight.FlightObserver;
import member.Member;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpSystem implements FlightObserver{
	
	public MLPQ pq; 
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	
	private final AirportManagerService airportManagerService = new AirportManagerService(new AirportManagerDao());
	
	public PickUpSystem() {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
	}
	
	public void appendQueue(String passportNum, int flightResNum) {
		// 1. 신원 검증 (실패 시 여기서 Validation, System, DataNotFound 예외가 자동으로 날아감)
		this.validateInfo(passportNum, flightResNum);
		
		// 2. 큐 삽입용 정보 수령
		AppendQueueDTO aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);
		
		// 3. Observer 등록 
		Airplane airplane = new Airplane(0, aqdto.getFlightCode(), aqdto.getDepartureAt());
		
		airplane.registerObserver(this);
		
		Member member = new Member(aqdto.getMemberId(), null, null, aqdto.getName(), null, null,
						passportNum, null, false, aqdto.getGrade(), null, null);
				
		// 4. 큐에 삽입
		this.pq.enqueue(airplane, member);
	}
	
	public void realPickUp(String passportNum, int flightResNum) {
		// 1. 픽업 시 최종 신원 검증
		this.validateInfo(passportNum, flightResNum);
		
		// 2. 큐에서 대상자 pop (비어있으면 여기서 QueueException 날아감)
		PickUpTicket ticket = this.popQueue();
		
		// 3. 픽업한 사람과 검증된 정보가 일치하는지 확인하는 로직 (선택사항)
		if(!ticket.getMember().getPassportNum().equals(passportNum)) {
			throw new ValidationException(ErrorCode.INVALID_INPUT, new Exception("호출된 순번의 고객 정보와 일치하지 않습니다."));
		}
		
		// 4. Observer 해제
		ticket.getAirplane().removeObserver(this);
		
		// TODO: DB updateOrderState() 로직 등 수행
	}
	
	private List<PickUpDTO> validateInfo(String passportNum, int flightResNum) {
		// DB 조회
		List<PickUpDTO> realPickUpList = this.pickUpDAO.getAllPickUp(passportNum, flightResNum);
		
		LocalDateTime pickUpAvailableAt = realPickUpList.get(0).getPickupAvailableAt();
		LocalDateTime departureAt = realPickUpList.get(0).getDepartureAt();
		
		// 검증 1: 아직 픽업 가능 시간이 안 된 경우
		if (CurrentTime.curTime.isBefore(pickUpAvailableAt)) {
			throw new ValidationException(ErrorCode.ILLEGAL_STATE, new Exception("아직 픽업 가능 시간이 아닙니다."));
		}
		// 검증 2: 이미 비행기가 떠난 경우 (No-Show)
		if (CurrentTime.curTime.isAfter(departureAt)) {
			// TODO: 노쇼처리 -> 자동환불
			throw new BusinessException(ErrorCode.NO_SHOW, new Exception("출국 시간이 경과하여 인도받을 수 없습니다."));
		}
		
		return realPickUpList;
	}
	
	public PickUpTicket popQueue() {
		return this.pq.pop(); // 내부에서 QueueException 발생 가능
	}
	
	//Observer 콜백 (이미 갱신된 departureAt Airplane 이 들어옴)
	@Override
	public void onFlightDelayReceived(Airplane airplane) {
		System.out.println("[PickUpSystem] 지연 이벤트 수신" 
							+ " | flightCode " + airplane.getFlightCode() 
							+ " | 지연 : " +  airplane.getDepartureAt());
		
		rescheduledPq();
	}
	
	// PQ 재정렬 (전부 꺼내서 다시 enqueue 바뀐 값 기준으로 재정렬) 
	public void rescheduledPq() {
		
		if (pq.size() == 0) {
	        System.out.println("[PickUpSystem] 재정렬할 대기열 없음");
	        return;
	    }

	    List<PickUpTicket> allTickets = new ArrayList<>();
	    allTickets.addAll(pq.getAllFromAq());
	    allTickets.addAll(pq.getAllFromBq());

	    pq.clearAll();

	    // enqueue 내부에서 출국 임박 여부 재판단
	    for (PickUpTicket ticket : allTickets) {
	        pq.enqueue(ticket.getAirplane(), ticket.getMember());
	    }

	    System.out.println("[PickUpSystem] 재정렬 완료 || 현재 대기 수: " + pq.size());
	    
	    printCurrentQueue();
	}

	public void printCurrentQueue() {

	    if (pq.size() == 0) {
	        System.out.println("[PickUpSystem] 대기열 없음");
	        return;
	    }

	    System.out.println("===== ap (긴급 큐) =====");

	    int rank = 1;
	    for (PickUpTicket ticket : pq.getAllFromAq()) {

	        System.out.println(
	            "[" + rank++ + "] "
	            + ticket.getMember()
	            + " | "
	            + ticket.getAirplane().getFlightCode()
	            + " | 출국: "
	            + ticket.getAirplane().getDepartureAt()
	        );
	    }

	    System.out.println("===== dp (일반 큐) =====");

	    rank = 1;

	    for (PickUpTicket ticket : pq.getAllFromBq()) {

	        System.out.println(
	            "[" + rank++ + "] "
	            + ticket.getMember()
	            + " | "
	            + ticket.getAirplane().getFlightCode()
	            + " | 출국: "
	            + ticket.getAirplane().getDepartureAt()
	        );
	    }
	}
	
	// flightCode로 PQ안 Airplane 찾아서 지연 처리하기 위함
	public void delayFlight(String flightCode, LocalDateTime newDepartureAt) {
		
		// aq
		for (PickUpTicket ticket : pq.getAllFromAq()) {
	        if (ticket.getAirplane().getFlightCode().equals(flightCode)) {
	            ticket.getAirplane().setDepartureAt(newDepartureAt);
	            return;
	        }
	    }
		
		// bq
		for (PickUpTicket ticket : pq.getAllFromBq()) {
	        if (ticket.getAirplane().getFlightCode().equals(flightCode)) {
	            ticket.getAirplane().setDepartureAt(newDepartureAt);
	            return;
	        }
	    }
		
		System.out.println("[PickUpSystem] 해당 항공편 없음 ");
	}
	
	// 로그인 
	public void login(int managerId, String password) {
        airportManagerService.login(managerId, password); 
    }
	
	// 로그아웃 
	public void logout() {
        airportManagerService.logout(); 
    }
	
	// 전체 픽업 목록
	public void printAllPickUpList() {
        airportManagerService.printAllPickUpList(); 
    }
	
	// 특정 회원 픽업 목록
	public void printAllPickUpList(Member member) {
		airportManagerService.printAllPickUpList(member);
	}
	
	// 기간별 픽업 목록 
	public void printAllPickUpList(LocalDate start, LocalDate end) {
        airportManagerService.printAllPickUpList(start, end);
    }
	
	
}