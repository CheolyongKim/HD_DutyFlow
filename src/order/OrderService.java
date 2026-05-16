package order;

import member.Member;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import brandSystem.dto.BrandOrderRequestDTO;
import common.Grade;
import exception.BusinessException;
import exception.ErrorCode;
import flight.FlightDAO;
import flight.FlightService;
import member.MemberDAO;
import membership.MembershipService;
import order.dto.OrderDTO;
import order.state.PendingState;
import payment.Payment;
import payment.PaymentDAO;
import payment.PaymentService;
import regulation.RegulationDAO;
import regulation.RegulationDTO;
import shoppingCart.ShoppingCartService;
import shoppingCart.dto.CartItemDTO;
import shoppingCart.dto.TotalCartDTO;
import tax.AlcoholTaxStrategy;
import tax.GeneralTaxStrategy;
import tax.PerfumeTaxStrategy;
import tax.TaxCalculator;
import tax.TaxStrategy;

public class OrderService {

	private final OrderDAO orderDAO = new OrderDAO();
	private final RegulationDAO regulationDAO = new RegulationDAO();

	private final FlightDAO flightDAO = new FlightDAO();
	private final PaymentDAO paymentDAO = new PaymentDAO();

	private final FlightService flightService = new FlightService(flightDAO);
	private final PaymentService paymentService = new PaymentService();
	private final MembershipService membershipService = new MembershipService();
	private final ShoppingCartService shoppingCartService = new ShoppingCartService();
	
	// CategoryId 상수 (DB 기준)
	private static final int CATEGORY_GENERAL = 1;
	private static final int CATEGORY_ALCOHOL = 2;
	private static final int CATEGORY_PERFUME = 4;

	// -------------------------------------------------------
	// 조회
	// -------------------------------------------------------

	/** 전체 주문 목록 조회 */
	public List<OrderDTO> getAllOrders() {
		return orderDAO.findAll();
	}

	/** 특정 사용자의 주문 목록 조회 */
	public List<OrderDTO> getOrdersByMemberId(int memberId) {
		return orderDAO.findByLoginId(memberId);
	}

	/** 주문 상세 조회 */
	public OrderDTO getOrder(int orderId, int productId) {
		OrderDTO order = orderDAO.findByorderIdAndProductId(orderId, productId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return order;
	}

	/** 한 주문의 상품 리스트 조회 */
	public List<OrderDTO> getOrdersByOrderId(int orderId) {
		List<OrderDTO> orders = orderDAO.findByOrderId(orderId);
		if (orders == null || orders.isEmpty()) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return orders;
	}

	/**
	 * 브랜드명으로 주문/판매 내역 조회
	 */
	public List<OrderDTO> getOrdersByBrandName(String brandName) {

		if (brandName == null || brandName.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		List<OrderDTO> orders = orderDAO.findOrdersByBrandName(brandName);

		if (orders == null || orders.isEmpty()) {
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
		}

		return orders;
	}

	// -------------------------------------------------------
	// 주문 생성 + 결제
	// -------------------------------------------------------

	public int createOrder(int memberId, int reservationId, List<CartItemDTO> cartItems) {

	    if (cartItems == null || cartItems.isEmpty()) {
	        throw new BusinessException(ErrorCode.INVALID_INPUT);
	    }

	    // 1. 총액 계산 (dollarPrice = 단가 * 수량 합산값)
	    BigDecimal totalPrice = cartItems.stream()
	            .map(CartItemDTO::getDollarPrice)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    // 2. Order 도메인 객체 구성
	    Order order = new Order();
	    order.setMemberId(memberId);
	    order.setReservationId(reservationId);
	    order.setTotalPrice(totalPrice);

	    // 3. CartItemDTO → OrderDTO 변환
	    //    insertOrder()의 detailSql에서 dollarPrice는 상품 단가이므로 역산
	    List<OrderDTO> orderItems = cartItems.stream()
	            .map(item -> OrderDTO.builder()
	                    .productId(item.getProductId())
	                    .categoryId(item.getCategoryId())   // ← 추가
	                    .capacity(item.getCapacity())        // ← 추가
	                    .quantity(item.getQuantity())
	                    .dollarPrice(
	                        item.getDollarPrice()
	                            .divide(BigDecimal.valueOf(item.getQuantity()), 2, BigDecimal.ROUND_HALF_UP)
	                    )
	                    .discountPrice(BigDecimal.ZERO)
	                    .build())
	            .collect(Collectors.toList());

	    // 4. orders + order_detail 트랜잭션 insert → orderId 반환
	    return orderDAO.insertOrder(order, orderItems);
	}
	
	
	
	/**
	 * 주문 생성 + 결제 프로세스 시작
	 */

	public List<BrandOrderRequestDTO> placeOrder(Order order,int orderId, int memberId,
                                                 int reservationId,
                                                 List<OrderDTO> cartItems,
                                                 String cardNumber) {
    	
    	TotalCartDTO cartDTO =  shoppingCartService.getCart(memberId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        // 1. 규정 조회 (categoryName 기반)
        RegulationDTO generalReg = regulationDAO.getByCategoryName("GENERAL");
        RegulationDTO alcoholReg = regulationDAO.getByCategoryName("ALCOHOL");
        RegulationDTO cosmeticsReg = regulationDAO.getByCategoryName("COSMETICS");

        // 2. 면세 체크
        DutyCheckResult dutyResult = checkDutyFreeLimits(cartItems, alcoholReg, cosmeticsReg);

        if (dutyResult.isExceeded()) {
            System.out.println("⚠ 면세 한도 초과");
            System.out.println(dutyResult.getMessage());
            System.out.println("예상 세금 = $" + dutyResult.getEstimatedTax());
            System.out.println("그래도 구매하시겠습니까? (Y/N)");

            Scanner sc = new Scanner(System.in);
            String answer = sc.nextLine();

            if (!answer.equalsIgnoreCase("Y")) {
                System.out.println("주문이 취소되었습니다.");
                return Collections.emptyList();
            }
        }

        BigDecimal totalBeforeTax = cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getDollarPrice();
                    BigDecimal discountRate = item.getDiscountPrice().divide(BigDecimal.valueOf(100));
                    BigDecimal discounted = price.subtract(price.multiply(discountRate));
                    return discounted.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalBeforeTax);

        // 4. 결제
        order(orderId, generalReg, alcoholReg, cosmeticsReg, cardNumber);

        // 5. 브랜드 DTO 반환
        return cartItems.stream()
                .map(item -> BrandOrderRequestDTO.builder()
                        .brandName(item.getBrandName())
                        .productName(item.getProductName())
                        .orderAmount(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }


	/**
	 * 최종 결제 승인
	 */

	public void order(int orderId, RegulationDTO generalReg, RegulationDTO alcoholReg, RegulationDTO perfumeReg,
			String cardNumber) {

		System.out.println("in Order");

		// 1. 주문 데이터 조회
		List<OrderDTO> orderItems = getOrdersByOrderId(orderId);
		Order order = orderDAO.findOneOrderByOrderId(orderId);

		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		// 2. 상태 검증
		if (!(order.getState() instanceof PendingState)) {
			throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
		}

		// 고객의 항공편 예약 번호 검증
		String reservationCode = orderDAO.findReservationCodeByOrderId(orderId);
		flightService.validateReservationCode(reservationCode);

		// 멤버십 할인 반영
		MemberDAO memberDAO = new MemberDAO();
		Member member = memberDAO.findById(order.getMemberId());
		if (member == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND); // 멤버가 없을 때의 예외 처리
		}
		Grade grade = member.getGrade();

		// 멤버십 할인액 계산: (현재가 * 할인율 / 100)
		BigDecimal membershipDiscount = order.getTotalPrice().multiply(grade.getDiscountRate())
				.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

		// 주문 객체에 즉시 반영 (할인액 기록 및 총액 차감)
		order.setDiscountPrice(membershipDiscount);
		order.setTotalPrice(order.getTotalPrice().subtract(membershipDiscount));

		// 검증 완료 상태 전환
		order.verify();

		// 3. 세금 계산 (전략 패턴)
		BigDecimal totalTax = calculateTax(orderItems, generalReg, alcoholReg, perfumeReg);
		BigDecimal finalAmount = order.getTotalPrice().add(totalTax);

		try {
			// 5. 외부 결제 요청
			boolean paySuccess = paymentService.payment(orderId, finalAmount, cardNumber);
			if (!paySuccess) {
				throw new BusinessException(ErrorCode.PAYMENT_FAILED);
			}

			// 6. 세금 반영 금액을 결제가 완료된 후에 DB에 insert
			if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
				order.applyTax(totalTax);
			}

			// 7. 결제 완료 상태 변경
			order.pay();

			// TODO: 재고 감소 메서드 위치

			// 8. DB 반영
			orderDAO.update(order);
			
			membershipService.updateMembershipGrade(order.getMemberId());

			System.out.println();
			System.out.println("✅ 결제 및 DB 반영 완료");

		} catch (BusinessException e) {
			throw new BusinessException(ErrorCode.PAYMENT_FAILED);
		} catch (Exception e) {
			// 🔥 원래 터진 진짜 에러 원인(세금 계산 오류 등)을 콘솔에 출력!!
			System.err.println("❌ [시스템 에러 디버그] 결제 처리 중 내부 예외 발생:");
			e.printStackTrace();
		}
	}

	/**
	 * 면세 한도 초과 여부 확인
	 */
	private DutyCheckResult checkDutyFreeLimits(List<OrderDTO> items, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {
		int totalAlcohol = aggregateCapacity(items, CATEGORY_ALCOHOL);
		int totalPerfume = aggregateCapacity(items, CATEGORY_PERFUME);

		boolean exceeded = false;
		StringBuilder message = new StringBuilder();

		if (alcoholReg != null && totalAlcohol > alcoholReg.getLimitCapacity()) {
			exceeded = true;
			message.append("- 주류 면세 한도(").append(alcoholReg.getLimitCapacity()).append("ml)를 초과했습니다.\n");
		}

		if (perfumeReg != null && totalPerfume > perfumeReg.getLimitCapacity()) {
			exceeded = true;
			message.append("- 향수/화장품 면세 한도(").append(perfumeReg.getLimitCapacity()).append("ml)를 초과했습니다.\n");
		}

		// 예상 세금 계산 (generalReg 없이도 한도 체크용이므로 null 허용)
		RegulationDTO generalReg = regulationDAO.getRegulationByCategoryId(CATEGORY_GENERAL);
		BigDecimal estimatedTax = calculateTax(items, generalReg, alcoholReg, perfumeReg);

		return new DutyCheckResult(exceeded, message.toString(), estimatedTax);
	}

	/**
	 * 세금 계산 - 전략 패턴 적용 HeejinMain의 calculate()와 동일한 방식
	 */
	private BigDecimal calculateTax(List<OrderDTO> items, RegulationDTO generalReg, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {

		// orderItems → Order 도메인 객체로 집계
		Order order = buildOrderForTax(items);

		System.out.println("주류 총 용량 = " + order.getTotalAlcohol() + "ml");
		System.out.println("향수 총 용량 = " + order.getTotalPerfume() + "ml");

		return calculate(order, generalReg, alcoholReg, perfumeReg);
	}

	/**
	 * 전략 구성 및 세금 계산
	 */
	private BigDecimal calculate(Order order, RegulationDTO generalReg, RegulationDTO alcoholReg,
			RegulationDTO perfumeReg) {

		List<TaxStrategy> strategies = new ArrayList<>();

		// 일반상품은 항상 포함
		if (generalReg != null) {
			strategies.add(new GeneralTaxStrategy(generalReg));
		}

		// 주류 한도 초과 시에만 추가
		if (alcoholReg != null && order.getTotalAlcohol() > alcoholReg.getLimitCapacity()) {
			strategies.add(new AlcoholTaxStrategy(alcoholReg));
		}

		// 향수 한도 초과 시에만 추가
		if (perfumeReg != null && order.getTotalPerfume() > perfumeReg.getLimitCapacity()) {
			strategies.add(new PerfumeTaxStrategy(perfumeReg));
		}

		if (strategies.isEmpty()) {
			return BigDecimal.ZERO;
		}

		TaxCalculator calculator = new TaxCalculator(strategies);
		return calculator.calculateTax(order);
	}

	/**
	 * OrderDTO 리스트 → 세금 계산용 Order 객체 생성
	 */
	private Order buildOrderForTax(List<OrderDTO> items) {

		BigDecimal totalPrice = BigDecimal.ZERO;
		int totalAlcohol = 0;
		int totalPerfume = 0;

		for (OrderDTO item : items) {
			int categoryId = item.getCategoryId();
			int totalCapacity = item.getCapacity() * item.getQuantity();

			System.out.println("상품명: " + item.getProductName() + ", 용량: " + item.getCapacity());

			BigDecimal itemPrice = item.getDollarPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			totalPrice = totalPrice.add(itemPrice);

			if (categoryId == CATEGORY_ALCOHOL) {
				totalAlcohol += totalCapacity;
			} else if (categoryId == CATEGORY_PERFUME) {
				totalPerfume += totalCapacity;
			}
		}

		Order order = new Order();
		order.setTotalPrice(totalPrice);
		order.setTotalAlcohol(totalAlcohol);
		order.setTotalPerfume(totalPerfume);
		return order;
	}

	/**
	 * categoryId에 해당하는 상품의 총 용량 집계
	 */
	private int aggregateCapacity(List<OrderDTO> items, int categoryId) {
		int total = 0;
		for (OrderDTO item : items) {
			if (item.getCategoryId() == categoryId) {
				total += item.getCapacity() * item.getQuantity();
			}
		}
		return total;
	}

	/**
	 * 외부 결제 Mock
	 */
	private boolean dummyPaymentGateway(BigDecimal amount) {
		System.out.println();
		System.out.println("결제 요청 금액 = $" + amount);
		return true;
	}

	// -------------------------------------------------------
	// TODO
	// -------------------------------------------------------

	/** 픽업 예약 — PAID 상태에서만 가능 */
	public void reservePickup(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.reservePickup(); // 불가능한 상태면 자동으로 예외 발생
		orderDAO.update(order);

		System.out.println("✅ 픽업 예약 완료 orderId = " + orderId);
	}

	/** 픽업 완료 — PICKUP_RESERVED 상태에서만 가능 */
	public void completePickup(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.pickup();
		orderDAO.update(order);

		System.out.println("✅ 픽업 완료 orderId = " + orderId);
	}

	private boolean canclePay(Order order) {
		try {
			Payment payment = paymentDAO.findSuccessByOrderId(order.getOrderId());
			int paymentId = payment.getPaymentId();
			paymentService.cancelPayment(paymentId);
			System.out.println("✅ 주문 취소 완료 orderId = " + order.getOrderId());
			membershipService.updateMembershipGrade(order.getMemberId());
			return true;
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED, e);
		}
	}

	/** 주문 취소 — ORDERED, PAID 상태에서만 가능 */
	public void cancelOrder(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.cancel();
		orderDAO.update(order);

		canclePay(order);

	}

	/** 미수령 처리 — PICKUP_RESERVED 상태에서만 가능 */
	public void markNoShow(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.noShow();
		orderDAO.update(order);

		canclePay(order);
		System.out.println("✅ 미수령 처리 완료 orderId = " + orderId);
	}
}