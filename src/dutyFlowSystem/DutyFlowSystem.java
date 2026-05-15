package dutyFlowSystem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

import exchangeRate.ExchangeRate;
import exchangeRate.ExchangeRateScheduler;
import exchangeRate.ExchangeRateService;
import member.Member;
import order.Order;
import payment.PaymentWorker;
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
	
	// 백그라운드에서 실행될 결제 Worker
	private final PaymentWorker paymentWorker = new PaymentWorker();

	// PaymentWorker를 실행할 Thread
	private Thread paymentWorkerThread;

	// PaymentWorker 실행
	// DutyFlowSystem 시작 시 백그라운드에서 Queue 감시 시작
	public void startPaymentWorker() {
	    paymentWorkerThread = new Thread(paymentWorker);
	    paymentWorkerThread.start();
	}

	// PaymentWorker 종료
	// DutyFlowSystem 종료 시 Worker도 함께 종료
	public void stopPaymentWorker() {
	    paymentWorker.stop();

	    // sleep 상태의 Worker를 즉시 깨워 종료 처리
	    if (paymentWorkerThread != null) {
	        paymentWorkerThread.interrupt();
	    }
	}
	
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
