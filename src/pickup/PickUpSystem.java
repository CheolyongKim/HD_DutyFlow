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
import order.dto.OrderUpdateDTO;
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

	// [PickUpSystem.java 에 추가할 메서드]
	public void passTime() {
		this.pq.passTime();
		System.out.println("SYSTEM: ⏳ 1분이 경과하였습니다. (가상 현재시간: " + CurrentTime.curTime.toLocalTime() + ")");
	}

	// [PickUpSystem.java 기존 realPickUp 메서드 덮어쓰기]
	public void realPickUp(String passportNum, int flightResNum) {
		System.out.println("\n--- 📦 [realPickUp] 물품 인도 프로세스 가동 ---");
		System.out.println("▶ 1. 창구 방문 고객 정보: 여권[" + passportNum + "], 예약번호[" + flightResNum + "]");

		// 1. 신원 검증
		this.validateInfo(passportNum, flightResNum);
		System.out.println("▶ 2. 신원 검증 완료: 픽업 가능 시간 및 노쇼 여부 정상 확인");

		// 2. 큐에서 대상자 팝 (여기서 MLPQ의 Starvation, Promote 로직이 빛을 발함!)
		PickUpTicket ticket = this.popQueue();
		System.out.println("▶ 3. 📢 큐 시스템 호출 대상자: [" + ticket.getMember().getName() + "] 고객님!");

		// 3. 일치 확인
		if (!ticket.getMember().getPassportNum().equals(passportNum)) {
			throw new ValidationException(ErrorCode.INVALID_INPUT,
					new Exception("호출된 대기열 순번의 고객과 창구에 방문한 고객 정보가 일치하지 않습니다."));
		}
		System.out.println("▶ 4. 방문 고객과 시스템 호출 대상자 일치 확인 완료!");

		// 4. 대상 주문 ID 찾기 및 상태 변경
		int targetOrderId = this.pickUpDAO.getOrderIdForPickup(passportNum, flightResNum);
		OrderUpdateDTO oud = new OrderUpdateDTO(targetOrderId, "PICKED_UP");
		this.updateOrderState(oud);

		System.out.println("✔️ 5. 정상 인도 및 DB 업데이트(상태: PICKED_UP, 수령시간: SYSDATE) 완료!");
		System.out.println("-------------------------------------------------");
	}

	public void appendQueue(String passportNum, int flightResNum) {
		// 1. 신원 검증 (실패 시 여기서 Validation, System, DataNotFound 예외가 자동으로 날아감)
		this.validateInfo(passportNum, flightResNum);

		// 2. 큐 삽입용 정보 수령
		AppendQueueDTO aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);

		// 3. 큐에 삽입
		this.pq.enqueue(new Airplane(0, aqdto.getFlightCode(), aqdto.getDepartureAt()), new Member(aqdto.getMemberId(),
				null, null, aqdto.getName(), null, null, passportNum, null, false, aqdto.getGrade(), null));
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