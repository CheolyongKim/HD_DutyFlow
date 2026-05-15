package order;

import member.Member;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import common.Grade;
import exception.BusinessException;
import exception.ErrorCode;
import member.MemberDAO;
import order.dto.OrderDTO;
import order.state.PendingState;
import regulation.RegulationDAO;
import regulation.RegulationDTO;
import tax.AlcoholTaxStrategy;
import tax.GeneralTaxStrategy;
import tax.PerfumeTaxStrategy;
import tax.TaxCalculator;
import tax.TaxStrategy;

public class OrderService {

	private final OrderDAO orderDAO = new OrderDAO();
	private final RegulationDAO regulationDAO = new RegulationDAO();

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

	// -------------------------------------------------------
	// 주문 생성 + 결제
	// -------------------------------------------------------

	/**
	 * 주문 생성 + 결제 프로세스 시작
	 */
	public int placeOrder(int memberId, int reservationId, List<OrderDTO> cartItems) {

		if (cartItems == null || cartItems.isEmpty()) {
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
		}

		// 1. 규정 조회 (딱 한 번)
		RegulationDTO generalReg = regulationDAO.getRegulationByCategoryId(CATEGORY_GENERAL);
		RegulationDTO alcoholReg = regulationDAO.getRegulationByCategoryId(CATEGORY_ALCOHOL);
		RegulationDTO perfumeReg = regulationDAO.getRegulationByCategoryId(CATEGORY_PERFUME);

		// 2. 면세 한도 확인
		DutyCheckResult dutyResult = checkDutyFreeLimits(cartItems, alcoholReg, perfumeReg);

		// 3. 면세 초과 안내
		if (dutyResult.isExceeded()) {
			System.out.println();
			System.out.println("⚠ 면세 한도 초과");
			System.out.println(dutyResult.getMessage());
			System.out.println("예상 세금 = $" + dutyResult.getEstimatedTax());
			System.out.println();
			System.out.println("그래도 구매하시겠습니까? (Y/N)");

			Scanner sc = new Scanner(System.in); // TODO: sc swing
			String answer = sc.nextLine();

			if (!answer.equalsIgnoreCase("Y")) {
				System.out.println("주문이 취소되었습니다.");
				return -1;
			}
		}

		// 4. 주문 객체 생성
		Order order = new Order();
		order.setMemberId(memberId);
		order.setReservationId(reservationId);

		// [계산 로직] (단가 * 수량)에서 할인율(%) 적용
		BigDecimal totalBeforeTax = cartItems.stream().map(item -> {
			BigDecimal price = item.getDollarPrice();
			BigDecimal discountRate = BigDecimal.valueOf(item.getDiscountPrice().doubleValue() / 100.0);
			BigDecimal discountAmount = price.multiply(discountRate);
			// 실구매가 = (단가 - 할인액) * 수량
			return price.subtract(discountAmount).multiply(BigDecimal.valueOf(item.getQuantity()));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		order.setTotalPrice(totalBeforeTax); // 상품 할인 적용된 합계 세팅
		order.setMemberId(memberId);
		
		// 5. 주문 저장
		int orderId = orderDAO.insertOrder(order, cartItems);

		// 6. 결제 프로세스 (조회한 규정 재사용)
		try {
			order(orderId, generalReg, alcoholReg, perfumeReg);
			System.out.println("넘어가는 orderId = " + orderId);
		} catch (BusinessException e) {
			System.err.println("주문 생성 후 결제 단계 오류: " + e.getMessage());
			throw e;
		}
		
		return orderId;
	}

	/**
	 * 최종 결제 승인
	 */
	public void order(int orderId, RegulationDTO generalReg, RegulationDTO alcoholReg, RegulationDTO perfumeReg) {

		System.out.println("in Order");

		// 1. 주문 데이터 조회
		List<OrderDTO> orderItems = getOrdersByOrderId(orderId);
		Order order = orderDAO.findOneOrderByOrderId(orderId);

		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

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

		// 2. 상태 검증
		if (!(order.getState() instanceof PendingState)) {
			throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
		}

		// 3. 세금 계산 (전략 패턴)
		BigDecimal totalTax = calculateTax(orderItems, generalReg, alcoholReg, perfumeReg);
		BigDecimal finalAmount = order.getTotalPrice().add(totalTax);

		// 4. 검증 완료 상태 전환
		order.verify();

		try {
			// 5. 외부 결제 요청
			boolean paySuccess = dummyPaymentGateway(finalAmount);
			if (!paySuccess) {
				throw new BusinessException(ErrorCode.PAYMENT_FAILED);
			}

			// 6. 세금 반영 금액을 결제가 완료된 후에 DB에 insert
			if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
				order.applyTax(totalTax);
			}

			// 7. 결제 완료 상태 변경
			order.pay();

			// 8. DB 반영
			orderDAO.update(order);

			System.out.println();
			System.out.println("✅ 결제 및 DB 반영 완료");

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PAYMENT_FAILED);
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

	/** 주문 취소 — ORDERED, PAID 상태에서만 가능 */
	public void cancelOrder(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.cancel();
		orderDAO.update(order);

		System.out.println("✅ 주문 취소 완료 orderId = " + orderId);
	}

	/** 미수령 처리 — PICKUP_RESERVED 상태에서만 가능 */
	public void markNoShow(int orderId) {

		Order order = orderDAO.findOneOrderByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		order.noShow();
		orderDAO.update(order);

		System.out.println("✅ 미수령 처리 완료 orderId = " + orderId);
	}
}