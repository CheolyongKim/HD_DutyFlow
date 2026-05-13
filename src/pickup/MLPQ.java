package pickup;

import java.util.Map;
import java.util.PriorityQueue;

public class MLPQ {
	private PriorityQueue<Map<Airplane, Member>> aq;
	private PriorityQueue<Map<Airplane, Member>> aq;
	
	private int promotionThresholdMinutes;
	private int maxWaitTimeMinutes;
	
	public Map<Airplane, Member> peek(){
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
