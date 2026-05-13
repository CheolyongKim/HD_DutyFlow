package dutyFlowSystem;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Queue;

import exchangeRate.ExchangeRate;
import order.Order;

public class DutyFlowSystem {
	
	private Member member;
	
	private List<Member> members;
	
	private Queue<Order> orderQueue;

	private List<ExchangeRate> exchangeRate;
	
	// private List<BrandSystemDao> brandRepository;
	
	// private TaxCalculator taxCalculator;
}
