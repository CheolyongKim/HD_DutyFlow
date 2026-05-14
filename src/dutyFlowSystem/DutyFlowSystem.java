package dutyFlowSystem;

import java.util.List;
import java.util.Queue;

import exchangeRate.ExchangeRate;
import member.Member;
import order.Order;
import product.Product;
import shoppingCart.ShoppingCartService;
import shoppingCart.dto.TotalCartDTO;

public class DutyFlowSystem {
	
	private Member member = new Member();
	private List<Member> members;
	private Queue<Order> orderQueue;
	private List<ExchangeRate> exchangeRate;
	// private List<BrandSystemDao> brandRepository;
	// private TaxCalculator taxCalculator;
	
	private final ShoppingCartService shoppingCartService = new ShoppingCartService();
	
	// 회원 장바구니에 상품 추가
	public void addToCart(Product p, int wishAmount) {
		shoppingCartService.addToCart(member.getMemberId(), p, wishAmount);
	}
	
	// 장바구니 내 특정 상품 수량 변경
	public void updateQuantity(Product p, int newAmount) {
		shoppingCartService.updateQuantity(member.getMemberId(), p, newAmount);
	}

	// 장바구니 조회
	public TotalCartDTO printCart() {
	    return shoppingCartService.getCart(member.getMemberId());
	}
	
	// 장바구니 비우기
	public void deleteFromCart() {
		shoppingCartService.flush(member.getMemberId());
	}
	
	// 장바구니 선택 상품 제거
	public void deleteFromCart(List<Product> selectedProducts) { 
		shoppingCartService.flush(member.getMemberId(), selectedProducts);
	}
}
