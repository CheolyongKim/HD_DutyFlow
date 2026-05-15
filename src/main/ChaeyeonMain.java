package main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import common.Currency;
import exception.BusinessException;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import order.Order;
import order.OrderService;
import order.dto.OrderDTO;
import product.ProductService;
import product.dto.ProductDTO;

public class ChaeyeonMain {

    public static void main(String[] args) {

    	/*
    	// product ---------------------------------------------------------------------------------------------------------
        ProductService service = new ProductService();

        // =========================
        // 1. 전체 조회 테스트
        // =========================
        System.out.println("===== 전체 상품 조회 =====");

        List<ProductDTO> all = service.printAllProducts();

        for (ProductDTO p : all) {
            System.out.println(p);
        }

        // =========================
        // 2. 카테고리 조회 테스트
        // =========================
        System.out.println("===== 카테고리별 상품 조회 =====");

        Category category = Category.builder()
                .categoryName("전자제품") // DB에 있는 값으로 맞춰야 함
                .build();

        List<ProductDTO> byCategory = service.printAllProducts(category);

        for (ProductDTO p : byCategory) { 
            System.out.println(p);
        }
        
	     // =========================
	     // 3. 상품명으로  단건 조회 테스트
	     // =========================
	     System.out.println("===== 상품명으로 상품 1개 조회 =====");
	     ProductDTO product = service.printProduct("조니워커 블루라벨");
	     System.out.println(product);
	     
	     // =========================
	     //특정 금액 범위 테스트
	     // =========================

	        BigDecimal min = new BigDecimal("100");
	        BigDecimal max = new BigDecimal("50000");
	
	        try {
	            List<ProductDTO> list =
	                service.printProduct(min, max, Currency.KRW);
	
	            System.out.println("===== 결과 =====");
	            for (ProductDTO p : list) {
	                System.out.println(p);
	            }
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        
        
    	 */
    	
    	
    	
        // order ---------------------------------------------------------------------------------------------------------
    	
    	/*
        System.out.println("\n\n===== [Order Service Test] =====");
        OrderService orderService = new OrderService();
        
        //1. 전체 주문 내역 확인
        System.out.println("===== [ADMIN] 전체 주문 내역 조회 =====");
        try {
            List<OrderDTO> allOrders = orderService.getAllOrders(); // DTO가 아닌 Order 도메인 사용 권장
            if (allOrders.isEmpty()) {
                System.out.println("등록된 주문 내역이 없습니다.");
            } else {
            	for (OrderDTO order : allOrders) {
                    System.out.print("예약번호: " + order.getReservationId());
                    System.out.print(" | 고객ID: " + order.getMemberId());
                    System.out.print(" | 총액: " + order.getDollarPrice());
                    System.out.print(" | 상품명: " + order.getProductName());
                    System.out.print(" | 수량: " + order.getQuantity());
                    // 상태 패턴의 현재 클래스명을 출력하여 상태 확인
                    System.out.print(" | 현재상태: " + order.getOrderState().getClass().getSimpleName());
                    System.out.println(" | 주문일: " + order.getOrderedAt());
                    System.out.println("---------------------------------------------------------");
                }            }
        } catch (Exception e) {
            throw new SystemException(ErrorCode.DATA_NOT_FOUND, e);
        }
        
        // 2. 특정 사용자 주문 내역 확인
        int memberId = 1;
        System.out.println("\n===== [USER] 회원 번호(" + memberId + ") 주문 내역 조회 =====");
        try {
            List<OrderDTO> myOrders = orderService.getOrdersByMemberId(memberId);
            
            if (myOrders.isEmpty()) {
                System.out.println("해당 회원의 주문 내역이 없습니다.");
            } else {
                for (OrderDTO order : myOrders) {
                    System.out.println(String.format("[주문번호: %d] 상태: %s | 결제금액(USD): %s", 
                        order.getOrderId(), 
                        order.getOrderState().getClass().getSimpleName(),
                        order.getDollarPrice()));
                }
            }
        } catch (Exception e) {
            throw new SystemException(ErrorCode.DATA_NOT_FOUND, e);
        }
        
        // 3. 특정 주문 상품의 상세 내역 확인
        int orderId = 1; // 예시용 예약번호
        int productId = 1;
        
        System.out.println("\n===== [USER] 내 주문 내역 조회 (예약번호: " + orderId + " 상품 번호" + productId + ") =====");
        try {
        	OrderDTO myOrder = orderService.getOrder(orderId, productId);
            System.out.println("주문 상태: " + myOrder.getOrderState().getClass().getSimpleName());
            System.out.println("주문 일자: " + myOrder.getOrderedAt());
            
        } catch (Exception e) {
            System.out.println("해당 주문을 찾을 수 없습니다: " + e.getMessage());
        }
        
        // 4. 한 번의 주문의 상품 리스트 보기
        System.out.println("\n===== [USER] 주문 번호 " + orderId + ") =====");
        try {
        	List<OrderDTO> myOrders = orderService.getOrdersByOrderId(orderId);
        	
        	 for (OrderDTO order : myOrders) {
                 System.out.println(String.format("[주문번호: %d] 상태: %s | 결제금액(USD): %s", 
                     order.getOrderId(), 
                     order.getOrderState().getClass().getSimpleName(),
                     order.getDollarPrice()));
             }

        } catch (Exception e) {
            System.out.println("해당 주문을 찾을 수 없습니다: " + e.getMessage());
        }
        
        */
    	

//    	OrderService orderService = new OrderService();
//        int testOrderId = 1; // DB에 존재하는 주문 번호
//
//        try {
//            System.out.println("=== 결제 및 검증 테스트 시작 ===");
//            orderService.order(testOrderId);
//            System.out.println("=== 테스트 종료: 성공 ===");
//        } catch (BusinessException e) {
//            System.err.println("❌ 검증 실패: " + e.getErrorCode().getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    	
//    	//==================================================
//    	// 5. 주문 생성 + 결제 테스트
//    	System.out.println("\n===== [USER] 주문 생성 및 결제 테스트 =====");
//    	OrderService orderService = new OrderService();
//    	
//    	try {
//
//    	    // 주문할 상품 리스트 생성
//    	    OrderDTO item1 = new OrderDTO();
//    	    item1.setProductId(1);
//    	    item1.setProductName("조니워커 블루라벨");
//    	    item1.setCategoryId(1);
//    	    item1.setCategoryName("위스키");
//    	    item1.setCapacity(750);
//    	    item1.setQuantity(1);
//    	    item1.setDollarPrice(new BigDecimal("220"));
//
//    	    OrderDTO item2 = new OrderDTO();
//    	    item2.setProductId(2);
//    	    item2.setProductName("샤넬 향수");
//    	    item2.setCategoryId(2);
//    	    item2.setCategoryName("향수");
//    	    item2.setCapacity(50);
//    	    item2.setQuantity(1);
//    	    item2.setDollarPrice(new BigDecimal("120"));
//
//    	    List<OrderDTO> cartItems = List.of(item1, item2);
//
//    	    int memberId = 1;
//    	    int reservationId = 1;
//    	    
//			// 주문 요청
//    	    orderService.placeOrder(memberId, reservationId, cartItems);
//
//    	    System.out.println("✅ 주문 및 결제 성공");
//
//    	} catch (BusinessException e) {
//
//    	    System.out.println("❌ 비즈니스 예외 발생");
//    	    System.out.println("에러 코드: " + e.getErrorCode());
//    	    System.out.println("메시지: " + e.getMessage());
//
//    	} catch (Exception e) {
//
//    	    System.out.println("❌ 시스템 오류 발생");
//    	    e.printStackTrace();
//    	}
    	//=====================================================
    	
    	 

        OrderService orderService = new OrderService();

        try {

            // ==============================
            // 테스트 주문 상품 생성
            // ==============================

            List<OrderDTO> cartItems = new ArrayList<>();

            // 상품 1
            OrderDTO whisky = OrderDTO.builder()
                    .productId(1)
                    .productName("조니워커 블루라벨")
                    .categoryId(1)
                    .categoryName("주류")
                    .capacity(750)
                    .quantity(2)
                    .dollarPrice(new BigDecimal("220"))
                    .discountPrice(BigDecimal.ZERO)
                    .build();

            // 상품 2
            OrderDTO perfume = OrderDTO.builder()
                    .productId(3)
                    .productName("샤넬 향수")
                    .categoryId(4)
                    .categoryName("화장품")
                    .capacity(50)
                    .quantity(1)
                    .dollarPrice(new BigDecimal("120"))
                    .discountPrice(BigDecimal.ZERO)
                    .build();
            
            cartItems.add(whisky);
            cartItems.add(perfume);

            // ==============================
            // 주문 요청
            // ==============================

            int memberId = 1;
            int reservationId = 1;

            orderService.placeOrder(
                    memberId,
                    reservationId,
                    cartItems
            );

            System.out.println();
            System.out.println("===== 주문 완료 =====");

            // ==============================
            // 전체 주문 조회
            // ==============================

            System.out.println();
            System.out.println("===== 전체 주문 조회 =====");

            List<OrderDTO> allOrders =
                    orderService.getAllOrders();

            for (OrderDTO order : allOrders) {
                System.out.println(order);
            }

            // ==============================
            // 회원 주문 조회
            // ==============================

            System.out.println();
            System.out.println("===== 회원 주문 조회 =====");

            List<OrderDTO> memberOrders =
                    orderService.getOrdersByMemberId(1);

            for (OrderDTO order : memberOrders) {
                System.out.println(order);
            }

            // ==============================
            // 특정 주문 상품 조회
            // ==============================

            System.out.println();
            System.out.println("===== 주문 상세 조회 =====");

            OrderDTO detail =
                    orderService.getOrder(1, 1);

            System.out.println(detail);

            // ==============================
            // 주문번호 기준 조회
            // ==============================

            System.out.println();
            System.out.println("===== 주문번호 기준 조회 =====");

            List<OrderDTO> orderItems =
                    orderService.getOrdersByOrderId(1);

            for (OrderDTO item : orderItems) {
                System.out.println(item);
            }

        } catch (BusinessException e) {

            System.out.println();
            System.out.println("❌ 비즈니스 예외 발생");
            System.out.println("code = " + e.getErrorCode());
            System.out.println("message = " + e.getMessage());

        } catch (Exception e) {

            System.out.println();
            System.out.println("❌ 시스템 오류 발생");

            e.printStackTrace();
        }
    }
    
}