package pickup;

import java.util.List;
import exception.QueueException;
import member.Member;
import order.Order;

public class PickUpSystem {
	private MLPQ pq; 
	private List<Member> members;
	private List<Order> orders;
	
	public void realPickUp(String passportNum, int flightResNum) {
		/*
		 * 여권 실물 검증과 같은 작업은 현실에서 이루어진다고 가정
		 * 파라미터 passportNum, flightResNum: 자신의 순번이 호출되어 인도받으러 온
		 * 고객이 현실에서 인도장관리자에게 (최종 신원 검증용으로) 제시한 여권번호와 탑승번호
		 */
		
		// 파라미터로 제시된 고객의 정보가 DB에 존재하는지 확인
		
		// 존재함
			// updateOrderState()의 대상 = popQueue()
			// popQueue()에서 발생 가능한 예외(비어있는 큐에서 pop시도) catch
		
		// 존재하지 않음 -> 예외처리
	}
	
	public void appendQueue(String passportNum, int flightResNum) {
		// TODO: DB에서 비행기 정보 flightResNum 일치하는 것 SELECT
		// this.pq.enqueue();
	}
	
	public PickUpTicket popQueue() throws QueueException{
		return this.pq.pop();
	}
}
