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
    	 
         // ================================================================
         // 케이스 1. 한도 이하 — 세금 없음
         // 위스키 1000ml (한도 2000ml 이하), 화장품 50ml (한도 100ml 이하)
         // 기대: 세금 $0, totalAmount = $220+$120 = $340
         // ================================================================
         System.out.println("===== 케이스 1: 한도 이하 (세금 없음) =====");
         runOrder(orderService,
                 whisky(750, 1, "220"),   // 750ml * 1 = 750ml  → 한도 이하 (2000)
                 perfume(50, 1, "120")    // 50ml  * 1 = 50ml   → 한도 이하 (100)
         );
  
         // ================================================================
         // 케이스 2. 위스키만 한도 초과
         // 위스키 3000ml (한도 2000ml 초과), 화장품 없음
         // 기대: 위스키 세금 부과
         // ================================================================
         System.out.println("\n===== 케이스 2: 위스키만 한도 초과 =====");
         runOrder(orderService,
                 whisky(750, 4, "220")    // 750ml * 4 = 3000ml → 한도 초과 (2000)
         );
  
         // ================================================================
         // 케이스 3. 화장품만 한도 초과
         // 화장품 200ml (한도 100ml 초과), 위스키 없음
         // 기대: 화장품 세금 부과
         // ================================================================
         System.out.println("\n===== 케이스 3: 화장품만 한도 초과 =====");
         runOrder(orderService,
                 perfume(100, 2, "120")   // 100ml * 2 = 200ml  → 한도 초과 (100)
         );
  
         // ================================================================
         // 케이스 4. 둘 다 한도 초과 (복합)
         // 위스키 3000ml + 화장품 200ml
         // 기대: 위스키 + 화장품 세금 모두 부과
         // ================================================================
         System.out.println("\n===== 케이스 4: 위스키 + 화장품 모두 한도 초과 =====");
         runOrder(orderService,
                 whisky(750, 4, "220"),   // 3000ml → 초과
                 perfume(100, 2, "120")   // 200ml  → 초과
         );
  
         // ================================================================
         // 케이스 5. 경계값 — 한도와 정확히 같음 (초과 아님)
         // 위스키 2000ml, 화장품 100ml
         // 기대: 세금 없음
         // ================================================================
	      // 케이스 5 — DB 실제 capacity=750 기준으로 경계값 맞추기
	      // 750 * 2 = 1500ml (한도 2000 이하) → 세금 없음 ✅
	      runOrder(orderService,
	              whisky(750, 2, "220"),   // 1500ml → 한도 이하
	              perfume(100, 1, "120")   // 100ml  → 경계
	      );

  
         // ================================================================
		   // 케이스 6 — 한도 초과용
		   // 750 * 3 = 2250ml (한도 2000 초과) → 세금 있음 ✅
		   runOrder(orderService,
		           whisky(750, 3, "220"),   // 2250ml → 초과
		           perfume(100, 2, "120")   // 200ml  → 초과
		   );
  
         // ================================================================
         // 케이스 7. 장바구니 비어있음 → 예외
         // ================================================================
         System.out.println("\n===== 케이스 7: 빈 장바구니 (예외) =====");
         try {
             orderService.placeOrder(1, 1, new ArrayList<>());
         } catch (BusinessException e) {
             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
         }
  
         // ================================================================
         // 케이스 8. 장바구니 null → 예외
         // ================================================================
         System.out.println("\n===== 케이스 8: null 장바구니 (예외) =====");
         try {
             orderService.placeOrder(1, 1, null);
         } catch (BusinessException e) {
             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
         }
  
         // ================================================================
         // 조회 테스트
         // ================================================================
  
         System.out.println("\n===== 전체 주문 조회 =====");
         orderService.getAllOrders().forEach(System.out::println);
  
         System.out.println("\n===== 회원(memberId=1) 주문 조회 =====");
         orderService.getOrdersByMemberId(1).forEach(System.out::println);
  
         System.out.println("\n===== 주문 상세 조회 (orderId=1, productId=1) =====");
         System.out.println(orderService.getOrder(1, 1));
  
         System.out.println("\n===== 주문번호 기준 조회 (orderId=1) =====");
         orderService.getOrdersByOrderId(1).forEach(System.out::println);
  
         // ================================================================
         // 케이스 9. 존재하지 않는 주문 조회 → 예외
         // ================================================================
         System.out.println("\n===== 케이스 9: 존재하지 않는 주문 조회 (예외) =====");
         try {
             orderService.getOrder(9999, 9999);
         } catch (BusinessException e) {
             System.out.println("✅ 예외 발생: " + e.getErrorCode() + " / " + e.getMessage());
         }
     }
  
     // ----------------------------------------------------------------
     // 헬퍼 메서드
     // ----------------------------------------------------------------
  
     /** 주문 실행 공통 처리 */
     private static void runOrder(OrderService orderService, OrderDTO... items) {
         try {
             List<OrderDTO> cartItems = new ArrayList<>();
             for (OrderDTO item : items) cartItems.add(item);
  
             orderService.placeOrder(1, 1, cartItems);
             System.out.println("✅ 주문 완료");
         } catch (BusinessException e) {
             System.out.println("❌ 비즈니스 예외: " + e.getErrorCode() + " / " + e.getMessage());
         } catch (Exception e) {
             System.out.println("❌ 시스템 오류: " + e.getMessage());
             e.printStackTrace();
         }
     }
  
     /** 위스키 아이템 생성 (categoryId=2) */
     private static OrderDTO whisky(int capacity, int quantity, String price) {
         return OrderDTO.builder()
                 .productId(1)
                 .productName("조니워커 블루라벨")
                 .categoryId(2)               // DB 기준 위스키 = 2
                 .categoryName("위스키")
                 .capacity(capacity)
                 .quantity(quantity)
                 .dollarPrice(new BigDecimal(price))
                 .discountPrice(BigDecimal.ZERO)
                 .build();
     }
  
     /** 화장품/향수 아이템 생성 (categoryId=4) */
     private static OrderDTO perfume(int capacity, int quantity, String price) {
         return OrderDTO.builder()
                 .productId(3)
                 .productName("샤넬 넘버5")
                 .categoryId(4)               // DB 기준 화장품 = 4
                 .categoryName("화장품")
                 .capacity(capacity)
                 .quantity(quantity)
                 .dollarPrice(new BigDecimal(price))
                 .discountPrice(BigDecimal.ZERO)
                 .build();
     }
    	
    
    
}