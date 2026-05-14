package pickup;

import java.time.LocalDateTime;
import java.util.List;

import common.CurrentTime;
import exception.DataNotFoundException;
import exception.QueueException;
import exception.SystemException;
import member.Member;
import order.Order;
import pickup.dto.RealPickUpDTO;
import product.productDAO;

public class PickUpSystem {
	private MLPQ pq; 
	private List<Member> members;	// 시뮬레이션용 
	private List<Order> orders;
	
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	
	public void realPickUp(String passportNum, int flightResNum) {
		/*
		 * 여권 실물 검증과 같은 작업은 현실에서 이루어진다고 가정
		 * 파라미터 passportNum, flightResNum: 자신의 순번이 호출되어 인도받으러 온
		 * 고객이 현실에서 인도장관리자에게 (최종 신원 검증용으로) 제시한 여권번호와 탑승번호
		 */
		
		// 파라미터로 제시된 고객의 정보가 DB에 존재하는지 확인
			// PickUp 테이블 -> PickUpDAO -> RealPickUpDTO 데이터 수령
		try {
			List<RealPickUpDTO> realPickUpList = this.pickUpDAO.getAllRealPickUp(passportNum, flightResNum);
			
			// 존재함
			try {
				// 픽업가능시간 <= RealPickUp하러 온 현재 시간 <= 출국시간 이어야 함
				LocalDateTime pickUpAvailableAt = realPickUpList.get(0).getPickupAvailableAt();
				LocalDateTime departureAt = realPickUpList.get(0).getDepartureAt();
				if (CurrentTime.curTime.isBefore(pickUpAvailableAt)) {
					System.out.println("아직 픽업 가능 시간이 아닙니다.");
				} else if (CurrentTime.curTime.isAfter(departureAt)) {
					System.out.println("출국시간이 지나 노쇼 처리되셨습니다.");
				} else {
					// updateOrderState()의 대상 = popQueue()
					
				}
			} catch (QueueException e) {
				// popQueue()에서 발생 가능한 예외(비어있는 큐에서 pop시도) catch
			}
		} catch (SystemException e) {
			
		} catch (DataNotFoundException e) {
			// 존재하지 않음 -> 예외처리
			System.out.println("주문한 적이 없거나 오늘 픽업 대상자가 아닙니다.");
		}
	}
	
	public void appendQueue(String passportNum, int flightResNum) {
		// TODO: DB에서 비행기 정보 flightResNum 일치하는 것 SELECT
		// this.pq.enqueue();
	}
	
	public PickUpTicket popQueue() throws QueueException{
		return this.pq.pop();
	}
}
