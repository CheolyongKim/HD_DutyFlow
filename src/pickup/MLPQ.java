package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

import airplane.Airplane;
import exception.QueueException;
import main.Application;
import member.Member;

public class MLPQ {
	private PriorityQueue<PickUpTicket> aq;
	private PriorityQueue<PickUpTicket> bq;
	
	private int lastNum;
	private final int promotionThresholdMinutes;
	private final int maxWaitTimeMinutes;
	
	public MLPQ() {
		this.lastNum = 1;
		this.promotionThresholdMinutes = 30;
		this.maxWaitTimeMinutes = 40;
	}
	
	public void enqueue(Airplane airplane, Member member) {
		if (this.size()==0 ||
				Duration.between(Application.curTime, airplane.getDepartureAt()).getSeconds()/60 < this.promotionThresholdMinutes) {
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
	
	private void promote(LocalDateTime currentSimulationTime) {
		for (PickUpTicket p : this.bq) {
			if (Duration.between(p.getAirplane().getDepartureAt(), currentSimulationTime).getSeconds()/60 < this.promotionThresholdMinutes) {
				PickUpTicket tempP = p;
				this.aq.add(tempP);
				this.bq.remove(p);
			}
		}
	}
	
	private void handleStarvation(LocalDateTime currentSimulationTime) {
		for (PickUpTicket p : this.bq) {
			if (Duration.between(p.getTicketIssueTime(), currentSimulationTime).getSeconds()/60 < this.maxWaitTimeMinutes) {
				PickUpTicket tempP = p;
				this.aq.add(tempP);
				this.bq.remove(p);
			}
		}
	}
	
	public void makeMLPQ(SortStrategy aqStrategy, SortStrategy bqStrategy) {
		this.makeQueueWithStrategy(this.aq, aqStrategy.getComparator());
		this.makeQueueWithStrategy(this.bq, bqStrategy.getComparator());
	}
	
	public void makeQueueWithStrategy(PriorityQueue<PickUpTicket> q, Comparator<PickUpTicket> c) {
		q = new PriorityQueue<PickUpTicket>(c);
	}
	
	public PickUpTicket peek() throws QueueException{
		return aq.peek();
	}
	
	public int size() {
		return this.aq.size() + this.bq.size();
	}
}
