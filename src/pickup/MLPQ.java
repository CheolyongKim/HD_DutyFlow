package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.PriorityQueue;

import airplane.Airplane;
import member.Member;

public class MLPQ {
	private PriorityQueue<PickUpTicket> aq;
	private PriorityQueue<PickUpTicket> bq;
	
	private int lastNum;
	private final int promotionThresholdMinutes;
	private final int maxWaitTimeMinutes;
	
	public MLPQ() {
		this.aq = new PriorityQueue(DepartureSoonSortStrategy.getComparator());
		this.bq = new PriorityQueue(PrioritySortStrategy.getComparator());
		
		this.lastNum = 1;
		this.promotionThresholdMinutes = 30;
		this.maxWaitTimeMinutes = 40;
	}
	
	public void enqueue(Airplane airplane, Member member) {
		if (this.size()==0 ||
				Duration.between(/*Main.curTime*/, airplane.getDepartureAt).getMinutes() < 30) {
			this.aq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}else {
			this.bq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}
	}
	
	public PickUpTicket pop() throws QueueException{
		if (this.aq.size()>0) return this.aq.poll();
		else return this.bq.poll();
	}
	
	private void moveToA(PickUpTicket p) {
		this.aq.add(this.bq.poll());
	}
	
	private void promote() {
		for (PickUpTicket p : this.bq) {
			if (Duration.between(p.getAirplane().getDepartureAt(), /*Main.curTime*/).getSeconds()/60 < 30) {
				PickUpTicket tempP = p;
				this.aq.add(tempP);
				this.bq.remove(p);
			}
		}
	}
	
	public PickUpTicket peek() throws QueueException{
		return aq.peek();
	}
	
	public int size() {
		return this.aq.size() + this.bq.size();
	}
}
