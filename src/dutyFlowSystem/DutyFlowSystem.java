package dutyFlowSystem;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import brandSystem.BrandSystem;
import brandSystem.dto.BrandOrderRequestDTO;
import exception.BusinessException;
import exception.ErrorCode;
import java.time.LocalDate;
import exception.ValidationException;
import exchangeRate.ExchangeRate;
import exchangeRate.ExchangeRateScheduler;
import exchangeRate.ExchangeRateService;
import flight.FlightBookDTO;
import flight.FlightService;
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
	
	private Queue<Order> orderQueue = new LinkedList<>();
	private List<BrandSystem> brandList;
	public String cardNumber = "4111-1111-1111-1111"; // 추후 Swing 입력 값

	// private TaxCalculator taxCalculator;

	private final ShoppingCartService shoppingCartService = new ShoppingCartService();
	private final FlightService flightService = new FlightService();;
    private final OrderService orderService = new OrderService();
    private final MemberService memberService = new MemberService();
    
    // 초기에 null로 설정, 로그인 성공 시 loginMemberId값 세팅
    private Integer loginMemberId = null;
    
	// 백그라운드에서 실행될 결제 Worker
	private final PaymentWorker paymentWorker = new PaymentWorker();

	// PaymentWorker를 실행할 Thread
	private Thread paymentWorkerThread;

	public DutyFlowSystem(List<BrandSystem> brandList) {
		this.brandList = brandList;
	}
	
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
		System.out.println("시스템");
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
	 * 
	 * @param reservationId 항공편 예약 ID
	 * @param cartItems     주문할 상품 리스트
	 * @param cardNumber    결제 카드 번호
	 * @return 생성된 주문 ID (orderId)
	 */

	public void addOrderQueue(Order order) {
		orderQueue.offer(order);
	}

	public void processOrderQueue() {
	    while (!orderQueue.isEmpty()) {
	        try {
	            Order order = orderQueue.poll();
	            processOrder(order);
	        } catch (Exception e) {
	            e.printStackTrace();
	            break; // 👈 에러 나면 다음 루프 돌지 말고 즉시 멈추기!
	        }
	    }
	}

	/**
	 * 나의 전체 주문 내역을 조회합니다.
	 */
	public List<OrderDTO> getMyOrders() {
		return orderService.getOrdersByMemberId(getLoginMemberId());
	}

	/**
	 * 특정 주문의 상세 상품 정보를 조회합니다.
	 */
	public List<OrderDTO> getOrderDetails(int orderId) {
		return orderService.getOrdersByOrderId(orderId);
	}

	private void processOrder(Order order) {

	    int reservationId;
	    List<OrderDTO> orderDetails;

	    try {
	        orderDetails =
	                getOrderDetails(order.getOrderId());

	        FlightBookDTO flightBookDto =
	                flightService.getFlightBookByMemberId(
	                		getLoginMemberId()
	                );

	        reservationId =
	                flightBookDto.getReservationId();

	    } catch (BusinessException e) {

	        throw new BusinessException(
	                ErrorCode.DATA_NOT_FOUND,
	                e
	        );
	    }

	    try {

	        List<BrandOrderRequestDTO> requests =
	                orderService.placeOrder(
	                		order,
	                		order.getOrderId(),
	                		getLoginMemberId(),
	                        reservationId,
	                        orderDetails,
	                        cardNumber
	                );

	        for (BrandOrderRequestDTO request : requests) {
	            BrandSystem brandSystem =
	                    findBrandSystem(request.getBrandName());

	            if (brandSystem == null) {

	                throw new BusinessException(
	                        ErrorCode.DATA_NOT_FOUND
	                );
	            }

	            brandSystem.receiveOrder(
	                    request.getProductName(),
	                    request.getOrderAmount()
	            );
	        }

	    } catch (BusinessException e) {

	        throw new BusinessException(
	                ErrorCode.DATA_NOT_FOUND,
	                e
	        );
	    }
	}
	
	private BrandSystem findBrandSystem(String brandName) {

	    if (brandList == null || brandName == null) {
	        throw new BusinessException(ErrorCode.NOT_FOUNT_BRAND);
	    }

	    String target = normalize(brandName);

	    for (BrandSystem brandSystem : brandList) {

	        String candidate = normalize(brandSystem.getBrandName());

	        if (candidate.equals(target)) {
	            return brandSystem;
	        }
	    }

	    throw new BusinessException(ErrorCode.NOT_FOUNT_BRAND);
	}

	private String normalize(String value) {
	    return value
	            .trim()
	            .replaceAll("\\s+", "")   // 모든 공백 제거
	            .toLowerCase();
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
	
	public void setBrandList(List<BrandSystem> brandList) {
	    this.brandList = brandList;
	}
	
	public int makeOrder() {
	    int memberId = getLoginMemberId();

	    TotalCartDTO totalCart = shoppingCartService.getCart(memberId);

	    if (totalCart == null
	            || totalCart.getItems() == null
	            || totalCart.getItems().isEmpty()) {

	        throw new BusinessException(ErrorCode.INVALID_INPUT);
	    }

	    FlightBookDTO flightBookDto = flightService.getFlightBookByMemberId(memberId);
	    
	    int reservationId = flightBookDto.getReservationId();

	    int orderId = orderService.createOrder(
	            memberId,
	            reservationId,
	            totalCart.getItems()
	    );

	    Order order = new Order();
	    order.setOrderId(orderId);
	    order.setMemberId(memberId);
	    order.setReservationId(reservationId);

	    addOrderQueue(order);

	    deleteFromCart();

	    return orderId;
	}
	

//	public void makeOrder() {
//		// 1. 현재 로그인된 회원 ID 확인
//		int memberId = getLoginMemberId();
//
//		// 2. 회원의 장바구니 데이터 조회
//		TotalCartDTO totalCart = shoppingCartService.getCart(memberId); 
//		if (totalCart == null || totalCart.getItems() == null || totalCart.getItems().isEmpty()) {
//			throw new BusinessException(ErrorCode.INVALID_INPUT); // 장바구니가 비어있을 경우 예외 처리
//		}
//
//		// 3. 새 주문(Order) 객체 생성 및 기본 정보 설정
//		Order order = new Order();
//		order.setMemberId(memberId);
//		order.setOrderedAt(LocalDate.now());
//
//		// 4. [중요] OrderService를 통해 DB에 Order를 선행 insert하고 발급된 orderId를 받아옴
//		// (또는 OrderService 내부에서 order와 orderDetail을 한 번에 트랜잭션으로 처리하는 메서드가 있다면 그것을 호출해야 합니다.)
//		int generatedOrderId = orderService.createOrder(order); 
//		order.setOrderId(generatedOrderId);
//
//		// 5. 장바구니 아이템들을 OrderDetail 형식으로 변환하여 Order DTO 또는 리스트에 바인딩
//		// (현재 코드상 processOrder에서 orderDetails = getOrderDetails(order.getOrderId()); 로 
//		// orderService를 통해 상세 데이터를 다시 조회하므로, DB에 먼저 반영되어 있어야 합니다.)
//		for (var cartItem : totalCart.getCartItems()) {
//			OrderDTO orderDetail = new OrderDTO();
//			orderDetail.setOrderId(generatedOrderId);
//			orderDetail.setProductName(cartItem.getProduct().getProductName());
//			orderDetail.setBrandName(cartItem.getProduct().getBrandName());
//			orderDetail.setOrderAmount(cartItem.getAmount()); // 수량
//			// 필요 시 가격 정보 등 추가 세팅
//			// orderDetail.setPriceKrw(cartItem.getProduct().getPriceKrw());
//
//			// OrderService를 통해 OrderDetail table에 insert 수행
//			orderService.insertOrderDetail(orderDetail);
//		}
//
//		// 6. 모든 주문 데이터(마스터+상세)가 DB에 반영된 후, 결제 처리를 위해 큐에 Order 주입
//		addOrderQueue(order);
//
//		// 7. 주문이 성공적으로 큐에 들어갔으므로 장바구니 비우기
//		deleteFromCart();
//	}
	
//	public void makeOrder() {
//        Order order = new Order();
//        
//        
//        ()
//        
//        
//        addOrderQueue(order);
//	}
	
}
