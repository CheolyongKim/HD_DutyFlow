package order;

import java.util.List;
import exception.BusinessException;
import exception.ErrorCode;
import order.dto.OrderDTO;

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
    
    
    
    // 4. 주문하기 = 결제 요청 (상태 패턴 활용)
    public void processPayment(int OrderId) {
    	List<OrderDTO> orders = getOrdersByOrderId(OrderId);
    	
    	/*
    	 * 검증해야함~~~
    	 * 
    	 */
    	
//        order.requestPay(); // 내부에서 state.handlePay(this) 호출
//        orderDAO.update(order); // 변경된 상태(PaidState 등) 저장
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