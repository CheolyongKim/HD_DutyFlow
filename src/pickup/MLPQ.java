package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import airplane.Airplane;
import common.CurrentTime;
import exception.ErrorCode;
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
		//enqueue에서 NPE 터짐 방
		this.aq = new PriorityQueue<>(); 
	    this.bq = new PriorityQueue<>(); 
	}
	
	public PickUpTicket oneMinutePassed() {
		PickUpTicket poll = null;
		// 1분이 흐른다
		CurrentTime.curTime = CurrentTime.curTime.plusMinutes(1);
		
		// BQ 안의 모든 티켓들의 대기한 시간 증가 - ticketIssueTime이 고정되어있으므로 자동적
		// (자동적) BQ 안의 모든 티켓들 중 조건 만족 시 promote
		this.promote();
		
		// 0순위로 starvation 방지용 자동 poll
		poll = this.handleStarvation();
		if (poll != null) return poll;
		
		// 1순위로 AQ에 티켓이 하나라도 있으면 poll
		// 없으면, 2순위로 BQ에서 poll
		try {
			return this.pop();
		} catch (QueueException e) {
			// TODO: handle exception
		}
		// 없으면, continue = return null;
		return null;
	}
	
	public void enqueue(Airplane airplane, Member member) {
		if (this.size()==0 ||
				Duration.between(CurrentTime.curTime, airplane.getDepartureAt()).getSeconds()/60 < this.promotionThresholdMinutes) {
			this.aq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}else {
			this.bq.add(new PickUpTicket(member, airplane, ++this.lastNum));
		}
	}
	
	// MLPQ.java 내부의 pop과 peek 메서드 수정
	
		public PickUpTicket pop() {
			if (this.aq.size() > 0) return this.aq.poll();
			else if (this.bq.size() > 0) return this.bq.poll();
			else throw new QueueException(ErrorCode.QUEUE_EMPTY, new Exception("호출할 대기열이 비어있습니다."));
		}
		
		public PickUpTicket peek() {
			if (this.aq.size() > 0) return this.aq.peek();
			else if (this.bq.size() > 0) return this.bq.peek();
			else throw new QueueException(ErrorCode.QUEUE_EMPTY, new Exception("조회할 대기열이 비어있습니다."));
		}
	
	private void moveToA(PickUpTicket p) {
		this.aq.add(this.bq.poll());
	}
	
	// 수정 예시: removeIf 활용 (Java 8 이상)
	private void promote() {
	    // 출국시간이 30분 미만인 것들을 임시 리스트에 담고 aq에 넣은 뒤 bq에서 제거
	    List<PickUpTicket> toPromote = this.bq.stream()
	        .filter(p -> Duration.between(CurrentTime.curTime, p.getAirplane().getDepartureAt()).getSeconds()/60 < this.promotionThresholdMinutes)
	        .collect(Collectors.toList()); // <--- 이 부분을 수정했습니다!
	        
	    this.aq.addAll(toPromote);
	    this.bq.removeAll(toPromote);
	}
	
	private PickUpTicket handleStarvation() {
		for (PickUpTicket p : this.aq) {
			if (Duration.between(p.getTicketIssueTime(), CurrentTime.curTime).getSeconds()/60 >= this.maxWaitTimeMinutes) {
				PickUpTicket tempP = p;
				this.aq.remove(p);
				return tempP;
			}
		}
		for (PickUpTicket p : this.bq) {
			if (Duration.between(p.getTicketIssueTime(), CurrentTime.curTime).getSeconds()/60 >= this.maxWaitTimeMinutes) {
				PickUpTicket tempP = p;
				this.bq.remove(p);
				return tempP;
			}
		}
		return null;
	}
	
	public void makeMLPQ(SortStrategy aqStrategy, SortStrategy bqStrategy) {
		this.aq = new PriorityQueue<PickUpTicket>(aqStrategy.getComparator());
		this.bq = new PriorityQueue<PickUpTicket>(bqStrategy.getComparator());
	}
	
	
	public int size() {
		return this.aq.size() + this.bq.size();
	}
	
	// aq 복사본
	public List<PickUpTicket> getAllFromAq() {
	    return new ArrayList<>(this.aq);
	}

	// bq 복사본
	public List<PickUpTicket> getAllFromBq() {
	    return new ArrayList<>(this.bq);
	}

	public void clearAll() {
	    this.aq.clear();
	    this.bq.clear();
	}
}
