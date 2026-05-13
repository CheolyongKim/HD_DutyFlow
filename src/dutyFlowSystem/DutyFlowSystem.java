package dutyFlowSystem;

import java.util.List;
import java.util.Queue;

import exchangeRate.ExchangeRate;
import member.Member;
import order.Order;
import product.Product;

public class DutyFlowSystem {
	
	private Member member = new Member();
	private List<Member> members;
	private Queue<Order> orderQueue;
	private List<ExchangeRate> exchangeRate;
	// private List<BrandSystemDao> brandRepository;
	// private TaxCalculator taxCalculator;
	
	// 회원 장바구니에 상품 추가
	public void addToCart(Product p, int wishAmount) {
		member.getCart().addToCart(p, wishAmount);
	}
	
	// 장바구니 내 특정 상품 수량 변경
	public void updateQuantity(Product p, int newAmount) {
		member.getCart().updateQuantity(p, newAmount);
	}

	// 장바구니 조회
	public void printCart() {
	    member.getCart().printCart();
	}
}
