package dutyFlowSystem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Queue;

import exception.ErrorCode;
import exception.ValidationException;
import exchangeRate.ExchangeRate;
import exchangeRate.ExchangeRateScheduler;
import exchangeRate.ExchangeRateService;
import member.Member;
import member.MemberService;
import member.MemberSignupDTO;
import order.Order;
import order.OrderService;
import order.dto.OrderDTO;
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
    private final OrderService orderService = new OrderService();
    private final MemberService memberService = new MemberService();
    
    // 초기에 null로 설정, 로그인 성공 시 loginMemberId값 세팅
    private Integer loginMemberId = null;
    
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
	
	// 회원가입
	public void signup(MemberSignupDTO dto) {
	    memberService.signup(dto);
	}
	
	// 로그인
	public void login(String loginId, String password) {
	    loginMemberId = memberService.login(loginId, password);
	}

	// 로그아웃
	public void logout() {
	    loginMemberId = null;
	}

	// 로그인된 회원의 memberId 반환
	private int getLoginMemberId() {

	    if (loginMemberId == null) {
	        throw new ValidationException(ErrorCode.NOT_LOGGED_IN);
	    }

	    return loginMemberId;
	}

	// 여권 정보 등록
	public void registerPassport(String passportNum, LocalDate passportExpiredDate) {

	    memberService.registerPassport(getLoginMemberId(),passportNum,passportExpiredDate);
	}
	
	// 회원 장바구니에 상품 추가
	public void addToCart(Product p, int wishAmount) {
		shoppingCartService.addToCart(getLoginMemberId(), p, wishAmount);
	}
	
	// 장바구니 내 특정 상품 수량 변경
	public void updateQuantity(Product p, int newAmount) {
		shoppingCartService.updateQuantity(getLoginMemberId(), p, newAmount);
	}

	// 장바구니 조회
	public TotalCartDTO printCart() {
	    return shoppingCartService.getCart(getLoginMemberId());
	}
	
	// 장바구니 비우기
	public void deleteFromCart() {
		shoppingCartService.flush(getLoginMemberId());
	}
	
	// 장바구니 선택 상품 제거
	public void deleteFromCart(List<Product> selectedProducts) { 
		shoppingCartService.flush(getLoginMemberId(), selectedProducts);
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
	
	// -------------------------------------------------------
    // 주문(Order) 관련 서비스 위임 메서드
    // -------------------------------------------------------

    /**
     * 장바구니 상품들을 실제 주문으로 생성하고 결제 프로세스를 시작합니다.
     * @param reservationId 항공편 예약 ID
     * @param cartItems 주문할 상품 리스트
     * @param cardNumber 결제 카드 번호
     * @return 생성된 주문 ID (orderId)
     */
    public int placeOrder(int reservationId, List<OrderDTO> cartItems, String cardNumber) {
        // 현재 시스템의 로그인된 회원(member)의 ID를 사용하여 주문 요청
        return orderService.placeOrder(this.member.getMemberId(), reservationId, cartItems, cardNumber);
    }

    /**
     * 나의 전체 주문 내역을 조회합니다.
     */
    public List<OrderDTO> getMyOrders() {
        return orderService.getOrdersByMemberId(this.member.getMemberId());
    }

    /**
     * 특정 주문의 상세 상품 정보를 조회합니다.
     */
    public List<OrderDTO> getOrderDetails(int orderId) {
        return orderService.getOrdersByOrderId(orderId);
    }

    /**
     * 상품 수령을 위한 픽업 예약을 수행합니다. (PAID 상태여야 함)
     */
    public void reservePickup(int orderId) {
        orderService.reservePickup(orderId);
    }

    /**
     * 공항 등에서 상품 인도(수령) 완료 처리를 합니다.
     */
    public void completePickup(int orderId) {
        orderService.completePickup(orderId);
    }

    /**
     * 주문을 취소합니다. (결제 완료 전/후 상태에 따라 내부 로직 실행)
     */
    public void cancelOrder(int orderId) {
        orderService.cancelOrder(orderId);
    }

    /**
     * 고객이 지정된 시간에 상품을 수령하지 않았을 때 미수령 처리를 합니다.
     */
    public void markNoShow(int orderId) {
        orderService.markNoShow(orderId);
    }

    // -------------------------------------------------------
    // 관리자(Admin) 기능을 위한 메서드 (필요 시)
    // -------------------------------------------------------

    /**
     * [관리자용] 시스템의 모든 주문 목록을 조회합니다.
     */
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }
	
	
}
