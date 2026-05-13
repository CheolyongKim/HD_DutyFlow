package pickup;

import java.time.Duration;
import java.util.PriorityQueue;

public class MLPQ {
	private PriorityQueue<PickUpTicket> aq;
	private PriorityQueue<PickUpTicket> bq;
	
	private int lastNum;
	private int promotionThresholdMinutes;
	private int maxWaitTimeMinutes;
	
	public void enqueue(Airplane airplane, Member member) {
		if (this.size()==0 ||
				Duration.between(/*Main.curTime*/, airplane.getDepartureAt).getMinutes() < 30) {
			this.aq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}else {
			this.bq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}
	}
	
	public PickUpTicket pop() throws QueueException{
		return this.aq.poll();
	}
	
	public PickUpTicket peek() throws QueueException{
		return aq.peek();
	}
	
	public int size() {
		return this.aq.size() + this.bq.size();
	}
}
