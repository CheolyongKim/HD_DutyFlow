package dutyFlowSystem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

import exchangeRate.ExchangeRate;
import exchangeRate.ExchangeRateScheduler;
import exchangeRate.ExchangeRateService;
import member.Member;
import order.Order;
import product.Product;
import shoppingCart.ShoppingCartService;
import shoppingCart.dto.TotalCartDTO;

public class DutyFlowSystem {
	
	private Member member = new Member();
	private List<Member> members;
	private Queue<Order> orderQueue;
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
	
	// 환율 서비스 및 스케줄러
	private final ExchangeRateService exchangeRateService = new ExchangeRateService();

	private final ExchangeRateScheduler exchangeRateScheduler = new ExchangeRateScheduler(exchangeRateService);

	// 환율 자동 갱신 스케줄러 시작
	public void startExchangeRateScheduler() {
		exchangeRateScheduler.start();
	}

	// 환율 자동 갱신 스케줄러 종료
	// 스케줄러 lifecycle을 명시적으로 관리하기 위해 추가
	public void stopExchangeRateScheduler() {
		exchangeRateScheduler.stop();
	}
	
	// 오늘 환율 조회
	public BigDecimal getTodayExchangeRate() {
	    return exchangeRateService.getTodayExchangeRate();
	}

	// 최근 일주일 환율 조회
	public List<ExchangeRate> getWeeklyExchangeRates() {
	    return exchangeRateService.getWeeklyExchangeRates();
	}

	// 최근 한 달 환율 조회
	public List<ExchangeRate> getMonthlyExchangeRates() {
	    return exchangeRateService.getMonthlyExchangeRates();
	}
}
