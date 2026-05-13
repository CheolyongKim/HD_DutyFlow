package pickup;

import java.util.PriorityQueue;

public class MLPQ {
	private PriorityQueue<PickUpTicket> aq;
	private PriorityQueue<PickUpTicket> aq;
	
	private int promotionThresholdMinutes;
	private int maxWaitTimeMinutes;
	
	public PickUpTicket peek(){
		if (this.size()==0) {
			// TODO: raise Exception
			return null;
		}else {
			return aq.peek();
		}
	}
	
	public int size() {
		return this.aq.size() + this.bq.size();
	}
}
