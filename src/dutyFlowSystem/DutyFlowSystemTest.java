package dutyFlowSystem;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import brandSystem.BrandSystem;
import member.MemberSignupDTO;
import order.Order;
import order.dto.OrderDTO;
import product.Product;
import product.ProductService;

public class DutyFlowSystemTest {

    public static void main(String[] args) throws InterruptedException {

        // =====================================================
        // 1. 브랜드 시스템 Mock
        // =====================================================
        BrandSystem johnnieWalker = new BrandSystem("Johnnie Walker");
        BrandSystem chanel = new BrandSystem("Chanel");
        BrandSystem iqosBrand = new BrandSystem("IQOS");

        List<BrandSystem> brandList = Arrays.asList(
                johnnieWalker,
                chanel,
                iqosBrand
        );

        // =====================================================
        // 2. 시스템 생성
        // =====================================================
        DutyFlowSystem system = new DutyFlowSystem(brandList);
        ProductService productService = new ProductService();

        // =====================================================
        // 3. Worker 시작
        // =====================================================
        system.startPaymentWorker();
        system.startExchangeRateScheduler();

        // =====================================================
        // 4. 회원가입
        // =====================================================
        try {
            MemberSignupDTO signupDTO = new MemberSignupDTO(
                    "testuser01",
                    "1234",
                    "김민준",
                    LocalDate.of(1998, 5, 10),
                    "01012345678"
            );

            system.signup(signupDTO);

            System.out.println("회원가입 성공");

        } catch (Exception e) {
            System.out.println("회원가입 실패 | reason = " + e.getMessage());
        }

        // =====================================================
        // 5. 로그인
        // =====================================================
        system.login("testuser01", "1234");

        // =====================================================
        // 6. 여권 등록
        // =====================================================
        system.registerPassport("M12345678", LocalDate.of(2030, 12, 31));

        // =====================================================
        // 7. 상품 조회
        // =====================================================
        List<Product> products = productService.getAllProducts();

        Product p1 = products.get(0);
        Product p2 = products.get(1);
        Product p3 = products.get(2);

        // =====================================================
        // 8. 장바구니 테스트
        // =====================================================
        system.addToCart(p1, 1);
        system.addToCart(p2, 2);
        system.addToCart(p3, 1);
        
        system.makeOrder();
        
        System.out.println("========== CART ==========");
        System.out.println(system.printCart());

        // 수량 수정 테스트
        system.updateQuantity(p2, 1);

        System.out.println("========== CART AFTER UPDATE ==========");
        System.out.println(system.printCart());

        // =====================================================
        // 9. 주문 큐 적재
        // =====================================================
        
        system.makeOrder();
        


        // =====================================================
        // 10. 주문 처리 실행
        // =====================================================
        System.out.println("========== PROCESS ORDER QUEUE ==========");
        system.processOrderQueue();

        Thread.sleep(2000);

        // =====================================================
        // 11. 주문 조회 테스트
        // =====================================================
        System.out.println("========== MY ORDERS ==========");
        List<OrderDTO> myOrders = system.getMyOrders();
        System.out.println(myOrders);

        // =====================================================
        // 12. 픽업 예약 테스트
        // =====================================================
        system.reservePickup(1);

        // =====================================================
        // 13. 픽업 완료 테스트
        // =====================================================
        system.completePickup(1);

        // =====================================================
        // 14. 환율 테스트
        // =====================================================
        System.out.println("오늘 환율 = " + system.getTodayExchangeRate());
        System.out.println("주간 환율 = " + system.getWeeklyExchangeRates());
        System.out.println("월간 환율 = " + system.getMonthlyExchangeRates());

        // =====================================================
        // 15. 시스템 종료
        // =====================================================
        system.stopPaymentWorker();
        system.stopExchangeRateScheduler();

        System.out.println("========== SYSTEM END ==========");
    }
}