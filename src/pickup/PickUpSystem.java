package pickup;

import java.time.LocalDateTime;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import exception.BusinessException;
import exception.ErrorCode;
import exception.ValidationException;
import member.Member;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpSystem {
	public MLPQ pq; 
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	
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
						passportNum, null, false, aqdto.getGrade(), null, null));
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
}