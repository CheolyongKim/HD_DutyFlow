package pickup;

import java.time.LocalDateTime;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import exception.BusinessException;
import exception.ErrorCode;
import exception.ValidationException;
import member.Member;
import order.Order;
import order.OrderDAO;
import order.OrderUpdateDTO;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpSystem {
	public MLPQ pq; 
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	private final OrderDAO orderDAO = new OrderDAO();
    private List<Order> orders; // loadOrders()를 통해 채워질 주문 목록
	
	public PickUpSystem() {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
	}
	
	public void appendQueue(String passportNum, int flightResNum) {
		// 1. 신원 검증 (실패 시 여기서 Validation, System, DataNotFound 예외가 자동으로 날아감)
		this.validateInfo(passportNum, flightResNum);
		
		// 2. 큐 삽입용 정보 수령
		AppendQueueDTO aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);
				
		// 3. 큐에 삽입
		this.pq.enqueue(
				new Airplane(0, aqdto.getFlightCode(), aqdto.getDepartureAt()),
				new Member(aqdto.getMemberId(), null, null, aqdto.getName(), null, null,
						passportNum, null, false, aqdto.getGrade(), null));
	}
	
	// [체크리스트 2] DB에서 조건에 맞는 주문들을 메모리로 로드
    public void loadOrders() {
        System.out.println("SYSTEM: 인도장 시스템에 픽업 대기 중인 주문 목록을 로드합니다...");
        this.orders = this.orderDAO.getPendingOrders(CurrentTime.curTime);
        System.out.println("SYSTEM: 로드 완료 (총 " + this.orders.size() + "건의 대기 주문)");
    }
    
    // [체크리스트 3] 주문 상태와 픽업 시간을 업데이트
    public void updateOrderState(OrderUpdateDTO oud) {
        this.pickUpDAO.updateOrderAndPickupStatus(oud.getOrderId(), oud.getNewState());
    }

    // [체크리스트 1] 실제 픽업 프로세스 실행
    public void realPickUp(String passportNum, int flightResNum) {
        System.out.println("\n--- 📦 물품 픽업 프로세스 시작 ---");
        
        // 1. 픽업 가능 시간 등 신원 검증
        this.validateInfo(passportNum, flightResNum);
        
        // 2. DB에서 이 고객의 정확한 orderId 찾기
        int targetOrderId = this.pickUpDAO.getOrderIdForPickup(passportNum, flightResNum);
        
        // 3. 큐에서 0순위 대상자 호출
        PickUpTicket ticket = this.popQueue();
        
        // 4. 호출된 순번표의 고객 여권번호와 창구에 온 고객 여권번호 일치 확인
        if(!ticket.getMember().getPassportNum().equals(passportNum)) {
            // 일치하지 않으면 큐에 다시 복구(Rollback)하는 로직을 넣을 수도 있지만, 일단 예외 처리
            throw new ValidationException(ErrorCode.INVALID_INPUT, new Exception("호출된 순번표의 고객 정보와 창구에 방문한 고객이 일치하지 않습니다."));
        }
        
        // 5. DB 상태 변경 (PICKED_UP 으로 변경 및 시간 기록)
        OrderUpdateDTO oud = new OrderUpdateDTO(targetOrderId, "PICKED_UP");
        this.updateOrderState(oud);
        
        System.out.println("✔️ 정상 인도 완료! (고객명: " + ticket.getMember().getName() + ", 주문번호: " + targetOrderId + ")");
        System.out.println("---------------------------------");
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
}