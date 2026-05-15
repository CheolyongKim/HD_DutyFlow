package order;

import java.math.BigDecimal;
import java.util.List;
import exception.BusinessException;
import exception.ErrorCode;
import order.dto.OrderDTO;
import order.state.PendingState;
import regulation.RegulationDAO;
import regulation.RegulationDTO;

public class OrderService {

	private final OrderDAO orderDAO = new OrderDAO();

	// 1. 모든 주문 목록 조회
	public List<OrderDTO> getAllOrders() {
		return orderDAO.findAll();
	}

	// 2. 특정 사용자의 주문 목록 조회
	public List<OrderDTO> getOrdersByMemberId(int memberId) {
		return orderDAO.findByLoginId(memberId);
	}

	// 3. 주문 상세 조회
	public OrderDTO getOrder(int orderId, int productId) {
		OrderDTO order = orderDAO.findByorderIdAndProductId(orderId, productId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return order;
	}

	// 4. 한 번의 주문의 상품 리스트 보기
	public List<OrderDTO> getOrdersByOrderId(int orderId) {
		List<OrderDTO> order = orderDAO.findByOrderId(orderId);
		if (order == null) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		return order;
	}

	// -----------------------------------------------------------

	/**
	 * [핵심] 주문 프로세스 진입점: 주문 생성 후 즉시 결제 시도
	 */
		public void placeOrder(int memberId, int reservationId, List<OrderDTO> cartItems) {
			System.out.println("주문 프로세스 진입");
			if (cartItems == null || cartItems.isEmpty()) {
				throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
			}
	
			// 1. 면세 한도 사전 검증 (DB에 넣기 전에 체크)
			validateDutyFreeLimits(cartItems);
			
			Order order = new Order();
		    order.setMemberId(memberId);
		    order.setReservationId(reservationId);
		    
		    BigDecimal total = cartItems.stream()
		            .map(i -> i.getDollarPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
		            .reduce(BigDecimal.ZERO, BigDecimal::add);

		    order.setTotalPrice(total);
			
			// 2. 주문 정보 DB 초기 저장 (orders, order_detail)
		    int orderId = orderDAO.insertOrder(order, cartItems);	
		    order.setOrderId(orderId);

		    // 2. 결제
		    order.pay();

		    // 3. 상태 업데이트
		    orderDAO.update(order);

			
			// 3. 생성된 주문번호로 결제 승인 프로세스 진행
			try {
				order(orderId);
			} catch (BusinessException e) {
				// 결제 실패 시 처리 (필요 시 DB에서 삭제하거나 'FAILED' 상태로 변경하는 로직 추가 가능)
				System.err.println("주문 생성 후 결제 단계에서 오류 발생: " + e.getMessage());
				throw e;
			}
		}

	
		/**
		 * 최종 결제 승인 요청 (세금 합산 및 상태 변경)
		 */
		public void order(int orderId) {
			System.out.println("in Order");
			// [1] 데이터 준비
			List<OrderDTO> orderItems = getOrdersByOrderId(orderId);
			Order order = orderDAO.findOneOrderByOrderId(orderId);
	
			if (order == null)
				throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
	
			// 상태 검증
			if (order.getState() instanceof PendingState) {
				// 실시간 세금 계산
				BigDecimal totalTax = calculateAndValidateDuty(orderItems);
				// 최종 결제 금액 (기본가 + 세금)
				BigDecimal finalAmount = order.getTotalPrice().add(totalTax);
		
				try {
					boolean paySuccess = dummyPaymentGateway(finalAmount);
		
					if (paySuccess) {
						// 1. 성공 시 세금 적용
						if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
							order.applyTax(totalTax);
						}
		
						// 2. 상태 객체에 결제 처리 요청 (CheckState -> PaidState로 전환됨)
						// 인터페이스 수정 덕분에 이제 여기서 예외가 발생하지 않습니다.
//						order.requestPay();
		
						// 3. 최종 상태(PAID)와 금액을 DB에 업데이트
						orderDAO.update(order);
		
						System.out.println("✅ 결제 및 DB 반영 완료");
					}
				} catch (Exception e) {
					// 결제 실패 시 PayFailedState 등으로 변경하는 로직은 CheckState 내부에 두거나 여기서 처리
					throw new BusinessException(ErrorCode.PAYMENT_FAILED);
				}
			}else {
				throw new BusinessException(ErrorCode.INVALID_ORDER_STATE);
			}
	
		}
	
		private boolean dummyPaymentGateway(BigDecimal amount) {
			return true;
		}
	
		/**
		 * 실시간 카테고리별 규정 체크 및 세금 계산
		 */
		private BigDecimal calculateAndValidateDuty(List<OrderDTO> items) {
			BigDecimal totalEstimatedTax = BigDecimal.ZERO;
			RegulationDAO regulationDAO = new RegulationDAO();
	
			for (var item : items) {
				RegulationDTO reg = regulationDAO.getRegulationByCategoryId(item.getCategoryId());
				if (reg == null)
					continue;
	
				int currentTotalCapacity = item.getCapacity() * item.getQuantity();
	
				if (currentTotalCapacity > reg.getLimitCapacity()) {
					BigDecimal rate = new BigDecimal(reg.getOverageRate()).divide(new BigDecimal("100"));
					BigDecimal itemTotalPrice = item.getDollarPrice().multiply(new BigDecimal(item.getQuantity()));
					BigDecimal taxAmount = itemTotalPrice.multiply(rate);
	
					totalEstimatedTax = totalEstimatedTax.add(taxAmount);
				}
			}
			return totalEstimatedTax;
		}
	
		/**
		 * 전체 면세 한도 검증
		 */
		private void validateDutyFreeLimits(List<OrderDTO> items) {
			System.out.println("in validateDutyFreeLimits");
			System.out.println("=== validateDutyFreeLimits START ===");

			for (var item : items) {
			    System.out.println("item = " + item);
			    System.out.println("category = " + item.getCategoryName());
			    System.out.println("product = " + item.getProductName());
			}
			
			
			int totalLiquorCapacity = 0;
			BigDecimal totalLiquorPrice = BigDecimal.ZERO;
			int totalPerfumeCapacity = 0;
	
			for (var item : items) {
				String categoryName = item.getCategoryName();
				int qty = item.getQuantity();
				int capacity = item.getCapacity();
	
				if (categoryName.contains("주류") || categoryName.contains("위스키") || categoryName.contains("와인")) {
					totalLiquorCapacity += (capacity * qty);
					totalLiquorPrice = totalLiquorPrice.add(item.getDollarPrice().multiply(new BigDecimal(qty)));
				} else if (categoryName.contains("화장품") || categoryName.contains("향수")
						|| item.getProductName().contains("향수")) {
					totalPerfumeCapacity += (capacity * qty);
				}
			}
	
			if (totalLiquorCapacity > 2000 || totalLiquorPrice.compareTo(new BigDecimal("400")) > 0) {
				throw new BusinessException(ErrorCode.DUTY_FREE_LIQUOR_EXCEEDED);
			}
			if (totalPerfumeCapacity > 100) {
				throw new BusinessException(ErrorCode.DUTY_FREE_PERFUME_EXCEEDED);
			}
		}

	// 5. 주문 취소 요청
	public void cancelOrder(int flightResNum) {
//        Order order = getOrder(flightResNum);
//        order.requestCancel(); // state.handleCancel(this) 호출 -> CancelState로 변경
//        orderDAO.update(order);
	}

	// 6. 픽업 완료 처리
	public void completePickup(int flightResNum) {
//        Order order = getOrder(flightResNum);
//        order.requestPickup(); // state.handlePickup(this) 호출 -> PickupDoneState로 변경
//        orderDAO.update(order);
	}

}