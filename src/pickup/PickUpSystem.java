package pickup;

import java.time.LocalDateTime;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.QueueException;
import exception.SystemException;
import exception.ValidationException;
import member.Member;
import order.Order;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpSystem {
	public MLPQ pq; 
	//private List<Member> members;	// 시뮬레이션용 (인도장에 찾아온) 고객들 리스트
	private List<Order> orders;		// (DB에서 받아온) 픽업 가능 시간이 현재시간-3시간 ~ 현재시간+3시간 이내인, 수령완료하지 않은 주문들의 리스트
	
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	
	public PickUpSystem() {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
		//this.members = members;
	}
	
	public void appendQueue(String passportNum, int flightResNum) {
		List<PickUpDTO> pickUpList;
		AppendQueueDTO aqdto;
		// 신원검증
		try {
			pickUpList = this.validateInfo(passportNum, flightResNum);
			
			// enqueue 위해 Airplane, Member 넣어야 함
			// Airplane: flightResNum 으로 찾아온다 
			// 필요한거- flightCode, departureAt, isDelayed
			// Member: passportNum 으로 찾아온다 
			// 필요한거- memberId, grade
			// -> 한꺼번에 DTO로 찾아온다 (나머지는 불필요)
			aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);
					
			this.pq.enqueue(
				    new Airplane(0, aqdto.getFlightCode(), aqdto.getDepartureAt()),
				    new Member(
				        aqdto.getMemberId(), 
				        null, 
				        null, 
				        aqdto.getName(), // <-- 4번째 자리에 null 대신 이름 넣기!
				        null, 
				        null,
				        passportNum, 
				        null, 
				        false, 
				        aqdto.getGrade(), 
				        null
				    )
				);
		} catch (ValidationException e) {
			// TODO: 신원검증에서 걸림
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public void realPickUp(String passportNum, int flightResNum) {
		List<PickUpDTO> pickUpList;
		// 신원검증
		try {
			pickUpList = this.validateInfo(passportNum, flightResNum);
			// updateOrderState()의 대상 = popQueue()
		} catch (ValidationException e) {
			// TODO: 신원검증에서 걸림
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			e.printStackTrace();
		} catch (QueueException e) {
			// pop 시도하기 때문에 비어있었다면 QueueException 발생 가능
			e.printStackTrace();
		}
	}
	
	public void loadOrders() {
		
	}
	
	// 신원검증 메서드: 문제가 없다면 해당하는 PickUpDTO 리스트 리턴, 문제가 있으면 예외 throw
	private List<PickUpDTO> validateInfo(String passportNum, int flightResNum) throws ValidationException, SystemException, DataNotFoundException{
		/*
		 * 여권 실물 검증과 같은 작업은 현실에서 이루어진다고 가정
		 * 파라미터 passportNum, flightResNum: 자신의 순번이 호출되어 인도받으러 온
		 * 고객이 현실에서 인도장관리자에게 (최종 신원 검증용으로) 제시한 여권번호와 탑승번호
		 */
		
		// 파라미터로 제시된 고객의 정보가 DB에 존재하는지 확인
			// PickUp 테이블 -> PickUpDAO -> RealPickUpDTO 데이터 수령
		List<PickUpDTO> realPickUpList = this.pickUpDAO.getAllPickUp(passportNum, flightResNum);
		
		// 픽업가능시간 <= (Real)PickUp하러 온 현재 시간 <= 출국시간 이어야 함
		LocalDateTime pickUpAvailableAt = realPickUpList.get(0).getPickupAvailableAt();
		LocalDateTime departureAt = realPickUpList.get(0).getDepartureAt();
		if (CurrentTime.curTime.isBefore(pickUpAvailableAt)) {
			throw new ValidationException(ErrorCode.ILLEGAL_STATE);
		}
		if (CurrentTime.curTime.isAfter(departureAt)) {
			// TODO: 노쇼처리 -> 자동환불로 넘어가야 함 (결제내역 테이블의 결제상태 변경)
		}
		return realPickUpList;
	}
	
	public PickUpTicket popQueue() throws QueueException{
		return this.pq.pop();
	}
}
