package pickup;

import java.util.List;

import member.Member;

public class PickUpSystem {
	private MLPQ pq; 
	private List<Member> members;
	private List<Order> orders;
	private QueueSorter queueSorter;
}
