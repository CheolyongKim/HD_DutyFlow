
package order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import exception.BusinessException;
import exception.ErrorCode;
import order.dto.OrderDTO;
import order.state.PendingState;

public class OrderService {

	private final OrderDAO orderDAO = new OrderDAO();

	/**
	 * 전체 주문 목록 조회
	 */
	public List<OrderDTO> getAllOrders() {
		return orderDAO.findAll();
	}

	/**
	 * 특정 사용자의 주문 목록 조회
	 */
	public List<OrderDTO> getOrdersByMemberId(int memberId) {
		return orderDAO.findByLoginId(memberId);
	}

	/**
	 * 주문 상세 조회
	 */
	public OrderDTO getOrder(int orderId, int productId) {

		OrderDTO order = orderDAO.findByorderIdAndProductId(orderId, productId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return order;
	}

	/**
	 * 한 주문의 상품 리스트 조회
	 */
	public List<OrderDTO> getOrdersByOrderId(int orderId) {

		List<OrderDTO> orders = orderDAO.findByOrderId(orderId);
		if (orders == null || orders.isEmpty()) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return orders;
	}

	// -----------------------------------------------------------

	/**
	 * 주문 생성 + 결제 프로세스 시작
	 */
	public void placeOrder(int memberId, int reservationId, List<OrderDTO> cartItems) {

		if (cartItems == null || cartItems.isEmpty()) {
			throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
		}

		// 1. 면세 한도 확인
		DutyCheckResult dutyResult = checkDutyFreeLimits(cartItems);

		// 2. 면세 초과 안내
		if (dutyResult.isExceeded()) {

			System.out.println();
			System.out.println("⚠ 면세 한도 초과");
			System.out.println(dutyResult.getMessage());

			System.out.println("예상 세금 = $" + dutyResult.getEstimatedTax());

			System.out.println();
			System.out.println("그래도 구매하시겠습니까? (Y/N)");

			Scanner sc = new Scanner(System.in);
			String answer = sc.nextLine();

			if (!answer.equalsIgnoreCase("Y")) {
				System.out.println("주문이 취소되었습니다.");
				return;
			}
		}

		// 3. 주문 객체 생성
		Order order = new Order();

		order.setMemberId(memberId);
		order.setReservationId(reservationId);

		BigDecimal total = cartItems.stream()
				.map(item -> item.getDollarPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		order.setTotalPrice(total);

		// 4. 주문 저장
		int orderId = orderDAO.insertOrder(order, cartItems);
		System.out.println("생성된 orderId = " + orderId);

		// 5. 결제 프로세스 진행
		try {

			order(orderId);
			System.out.println("넘어가는 orderId = " + orderId);
		} catch (BusinessException e) {

			System.err.println("주문 생성 후 결제 단계 오류: " + e.getMessage());

			throw e;
		}
	}

	/**
	 * 최종 결제 승인
	 */
	public void order(int orderId) {

		System.out.println("in Order");

		// 1. 주문 데이터 조회
		List<OrderDTO> orderItems = getOrdersByOrderId(orderId);
		Order order = orderDAO.findOneOrderByOrderId(orderId);

		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}

		// 2. 상태 검증
		if (!(order.getState() instanceof PendingState)) {
			throw new BusinessException(ErrorCode.INVALID_ORDER_STATE);
		}

		// 3. 세금 계산
		BigDecimal totalTax = calculateAndValidateDuty(orderItems);
		BigDecimal finalAmount = order.getTotalPrice().add(totalTax);

		// 4. 검증 완료 상태 전환
		order.verify();

		try {
			// 5. 외부 결제 요청
			boolean paySuccess = dummyPaymentGateway(finalAmount);

			if (!paySuccess) {
				throw new BusinessException(ErrorCode.PAYMENT_FAILED);
			}

			// 6. 세금 반영
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
	 * 외부 결제 Mock
	 */
	private boolean dummyPaymentGateway(BigDecimal amount) {

		System.out.println();
		System.out.println("결제 요청 금액 = $" + amount);

		return true;
	}

	/**
	 * 카테고리별 세금 계산
	 */
	private BigDecimal calculateAndValidateDuty(List<OrderDTO> items) {

		BigDecimal totalTax = BigDecimal.ZERO;

		int liquorCapacity = 0;
		BigDecimal liquorPrice = BigDecimal.ZERO;

		int perfumeCapacity = 0;
		BigDecimal perfumePrice = BigDecimal.ZERO;

		// 카테고리별 총합 계산
		for (OrderDTO item : items) {

			String category = item.getCategoryName();
			int totalCapacity = item.getCapacity() * item.getQuantity();
			BigDecimal totalPrice = item.getDollarPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			// 주류
			if (category.contains("주류") || category.contains("위스키") || category.contains("와인")) {
				liquorCapacity += totalCapacity;
				liquorPrice = liquorPrice.add(totalPrice);
			}

			// 향수
			else if (category.contains("향수") || category.contains("화장품")) {
				perfumeCapacity += totalCapacity;
				perfumePrice = perfumePrice.add(totalPrice);
			}
		}

		// ---------------------------
		// 주류 세금
		// ---------------------------
		System.out.println("주류 총 용량 = " + liquorCapacity);
		System.out.println("주류 총 금액 = $" + liquorPrice);

		System.out.println("향수 총 용량 = " + perfumeCapacity);
		System.out.println("향수 총 금액 = $" + perfumePrice);

		// 주류 세금
		if (liquorCapacity > 2000 || liquorPrice.compareTo(new BigDecimal("400")) > 0) {
			BigDecimal liquorTaxRate = new BigDecimal("0.2");
			BigDecimal liquorTax = liquorPrice.multiply(liquorTaxRate);
			totalTax = totalTax.add(liquorTax);
			System.out.println("주류 세금 = $" + liquorTax);
		}

		// 향수 세금
		if (perfumeCapacity > 100) {
			BigDecimal perfumeTaxRate = new BigDecimal("0.3");
			BigDecimal perfumeTax = perfumePrice.multiply(perfumeTaxRate);
			totalTax = totalTax.add(perfumeTax);
			System.out.println("향수 세금 = $" + perfumeTax);
		}
		return totalTax;
	}

	/**
	 * 면세 한도 체크
	 */
	private DutyCheckResult checkDutyFreeLimits(List<OrderDTO> items) {

		int totalLiquorCapacity = 0;
		BigDecimal totalLiquorPrice = BigDecimal.ZERO;
		int totalPerfumeCapacity = 0;

		for (OrderDTO item : items) {
			String categoryName = item.getCategoryName();
			int qty = item.getQuantity();
			int capacity = item.getCapacity();

			// 주류 총 용량, 총 가격
			if (categoryName.contains("주류") || categoryName.contains("위스키") || categoryName.contains("와인")) {
				totalLiquorCapacity += (capacity * qty);
				totalLiquorPrice = totalLiquorPrice.add(item.getDollarPrice().multiply(BigDecimal.valueOf(qty)));
			}

			// 향수 총 용량
			else if (categoryName.contains("화장품") || categoryName.contains("향수") || item.getProductName().contains("향수")) {
				totalPerfumeCapacity += (capacity * qty);
			}
		}

		boolean exceeded = false;
		StringBuilder message = new StringBuilder();

		// 주류 초과
		if (totalLiquorCapacity > 2000 || totalLiquorPrice.compareTo(new BigDecimal("400")) > 0) {
			exceeded = true;
			message.append("- 주류 면세 한도를 초과했습니다.\n");
		}

		// 향수 초과
		if (totalPerfumeCapacity > 100) {
			exceeded = true;
			message.append("- 향수 면세 한도를 초과했습니다.\n");
		}

		BigDecimal estimatedTax = calculateAndValidateDuty(items);
		return new DutyCheckResult(exceeded, message.toString(), estimatedTax);
		
	}

	/**
	 * 주문 취소
	 */
	public void cancelOrder(int orderId) {

		// TODO
	}

	/**
	 * 픽업 완료
	 */
	public void completePickup(int orderId) {

		// TODO
	}
}