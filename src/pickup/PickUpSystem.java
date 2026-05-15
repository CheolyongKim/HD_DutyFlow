package pickup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import exception.BusinessException;
import exception.ErrorCode;
import exception.ValidationException;
import flight.FlightObserver;
import member.Member;
import order.Order;
import order.OrderDAO;
import order.dto.OrderUpdateDTO;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;


public class PickUpSystem implements FlightObserver{
	
	public MLPQ pq; 
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	private final OrderDAO orderDAO = new OrderDAO();
	private List<Order> orders; // loadOrders()를 통해 채워질 주문 목록
	private PickUpTicket currentTicket;
	private boolean isCounterOpen = false; // 창구 오픈 상태

	public PickUpSystem() {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
	}
	// 이제 가상 시계(pickedUpAt)를 함께 받습니다.
	public void updateOrderState(OrderUpdateDTO oud, LocalDateTime pickedUpAt) {
		this.pickUpDAO.updateOrderAndPickupStatus(oud.getOrderId(), oud.getNewState(), pickedUpAt);
	}
	

	// ---------------------------------------------------------
	// [호출] 다음 대기자를 부르고 시스템에 기억시킴
	// ---------------------------------------------------------
	// 💡 1. 창구 오픈 메서드 (업무 시작)
	public void openCounter() {
		this.isCounterOpen = true;
		System.out.println("\n🏢 [시스템] " + CurrentTime.curTime.toLocalTime() + ", 인도장 창구 업무가 시작되었습니다.");
		this.tryCallNextCustomer();
	}

	// 💡 2. 밖에서 현재 호출된 사람을 확인할 수 있게 getter 추가
	public PickUpTicket getCurrentTicket() {
		return this.currentTicket;
	}

	// 💡 3. 자동 호출 감지기 (오픈되어 있을 때만 부름!)
	private void tryCallNextCustomer() {
		if (this.isCounterOpen && this.currentTicket == null && this.pq.size() > 0) {
			this.currentTicket = this.pq.pop();
			System.out
					.println("\n📢 [시스템 자동 호출] 띵동~ [" + this.currentTicket.getMember().getName() + "] 고객님, 창구로 와주세요!");
		}
	}

	// 실제 물품 인도 프로세스
	public void processPickUp(String passportNum, int flightResNum, int processingTime) {
		if (this.currentTicket == null) {
			throw new BusinessException(ErrorCode.ILLEGAL_STATE, new Exception("현재 호출된 고객이 없습니다."));
		}

		System.out.println("▶ 1. 창구 방문 고객 확인: 여권[" + passportNum + "]");
		if (!this.currentTicket.getMember().getPassportNum().equals(passportNum)) {
			throw new ValidationException(ErrorCode.INVALID_INPUT,
					new Exception("호출된 대상[" + currentTicket.getMember().getName() + "]과 방문 고객 정보가 일치하지 않습니다."));
		}
		System.out.println("▶ 2. 본인 확인 완료! [" + currentTicket.getMember().getName() + "] 고객님 물품 인도를 시작합니다.");

		this.validateInfo(passportNum, flightResNum);

		System.out.println("▶ 3. 물품 확인 및 인도 중... (소요 예정: " + processingTime + "분)");
		for (int i = 0; i < processingTime; i++) {
			this.passTime(); // 처리 시간 흐름 (여기서 다른 고객들 승격 여부 판별)
		}

		int targetOrderId = this.pickUpDAO.getOrderIdForPickup(passportNum, flightResNum);
		this.pickUpDAO.updateOrderAndPickupStatus(targetOrderId, "PICKED_UP", CurrentTime.curTime);

		System.out.println("✔️ 4. [" + currentTicket.getMember().getName() + "]님 인도 완료. (시각: "
				+ CurrentTime.curTime.toLocalTime() + ")");
		System.out.println("-------------------------------------------------");

		// 🚨 업무 종료 -> 옵저버 해제 -> 창구 비움 -> 다음 사람 자동 호출!
		this.currentTicket.getAirplane().removeObserver(this);
		this.currentTicket = null;
		
		this.tryCallNextCustomer();
	}

	// 시간 흐름 (시간이 흘러서 대기자가 생겼는데 창구가 비어있으면 호출됨!)
	public void passTime() {
		this.pq.passTime();
		System.out.println("   (⏳ " + CurrentTime.curTime.toLocalTime() + " 경과...)");
		this.tryCallNextCustomer(); // 오픈 전이면 무시됨
	}

	// 큐에 번호표 뽑기 (뽑았는데 창구가 비어있으면 즉시 호출됨!)
	public void appendQueue(String passportNum, int flightResNum) {
		this.validateInfo(passportNum, flightResNum);
		AppendQueueDTO aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);
		Airplane airplane = new Airplane(0, aqdto.getFlightCode(), aqdto.getDepartureAt());
		airplane.registerObserver(this);
		this.pq.enqueue(airplane , new Member(aqdto.getMemberId(),
				null, null, aqdto.getName(), null, null, passportNum, null, false, aqdto.getGrade(), null));
		this.tryCallNextCustomer(); // 오픈 전이면 무시됨
	}

	// DB에서 조건에 맞는 주문들을 메모리로 로드
	public void loadOrders() {
		System.out.println("SYSTEM: 인도장 시스템에 픽업 대기 중인 주문 목록을 로드합니다...");
		this.orders = this.orderDAO.getPendingOrders(CurrentTime.curTime);
		System.out.println("SYSTEM: 로드 완료 (총 " + this.orders.size() + "건의 대기 주문)");
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

	    System.out.println("===== aq (긴급 큐) =====");

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

	    System.out.println("===== bq (일반 큐) =====");

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
}