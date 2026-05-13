package pickup;

import java.util.List;

import member.Member;
import order.Order;

public class PickUpSystem {
	private MLPQ pq; 
	private List<Member> members;
	private List<Order> orders;
	// private QueueSorter queueSorter;
	
	public void appendQueue(String passportNum, int flightResNum) {
		// TODO: DB에서 비행기 정보 flightResNum 일치하는 것 SELECT
		// this.pq.enqueue();
	}
	
	public PickUpTicket popQueue() {
		
	}
}
