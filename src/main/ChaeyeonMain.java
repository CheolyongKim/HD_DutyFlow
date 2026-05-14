package main;

import java.math.BigDecimal;
import java.util.List;

import category.Category;
import common.Currency;
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
        
//        */
	     
    	
    	
    }
}