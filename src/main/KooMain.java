package main;

import java.math.BigDecimal;
import java.util.List;

import brandSystem.BrandSystem;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;
import stock.scheduler.StockCleanupScheduler;
import stock.scheduler.StockPurchaseScheduler;

public class KooMain {

    public static void main(String[] args) {

        String brandName = "Johnnie Walker";
        String productName = "조니워커 블루라벨";
        String otherBrandProductName = "샤넬 넘버5 오드퍼퓸";

        BrandSystem brandSystem = new BrandSystem(brandName);

        StockPurchaseScheduler purchaseScheduler =
                new StockPurchaseScheduler(brandSystem.getPurchaseService());

        StockCleanupScheduler cleanupScheduler =
                new StockCleanupScheduler(brandSystem.getStockService());

        try {
            System.out.println("===== BrandSystem 전체 통합 테스트 시작 =====");

            System.out.println("\n===== 0. 스케줄러 시작 =====");
            purchaseScheduler.start();
            cleanupScheduler.start();

            System.out.println("\n===== 1. 내 브랜드 전체 재고 목록 조회 =====");
            brandSystem.printMyBrandStocks();

            System.out.println("\n===== 2. 특정 상품 재고 조회 =====");
            brandSystem.printStockStatus(productName);

            System.out.println("\n===== 3. 다른 브랜드 상품 접근 제한 테스트 =====");
            brandSystem.printStockStatus(otherBrandProductName);

            System.out.println("\n===== 4. 주문 접수 및 FIFO 재고 차감 테스트 =====");
            brandSystem.receiveOrder(productName, 10);

            System.out.println("\n===== 5. 주문 처리 후 재고 조회 =====");
            brandSystem.printStockStatus(productName);

            System.out.println("\n===== 6. 재고 부족 예외 테스트 =====");
            brandSystem.receiveOrder(productName, 999);

            System.out.println("\n===== 7. 발주 요청 테스트 =====");
            brandSystem.makePurchase(productName, 10);

            System.out.println("\n===== 8. 브랜드 발주 이력 조회 =====");
            brandSystem.printPurchaseHistory();

            System.out.println("\n===== 9. REQUESTED 발주 조회 =====");
            brandSystem.printPurchaseHistoryByStatus(StockPurchaseStatus.REQUESTED);

            System.out.println("\n===== 10. 발주 취소 테스트 =====");
            List<StockPurchase> requestedPurchases =
                    brandSystem.getPurchaseService()
                            .getPurchaseHistoryByStatus(StockPurchaseStatus.REQUESTED);

            if (!requestedPurchases.isEmpty()) {
                int cancelPurchaseId = requestedPurchases.get(0).getPurchaseId();
                brandSystem.cancelPurchase(cancelPurchaseId);
            } else {
                System.out.println("취소할 REQUESTED 발주가 없습니다.");
            }

            System.out.println("\n===== 11. 발주 취소 후 이력 조회 =====");
            brandSystem.printPurchaseHistory();

            System.out.println("\n===== 12. 자동 입고 테스트용 발주 요청 =====");
            brandSystem.makePurchase(productName, 10);

            System.out.println("\n===== 13. 3분 10초 대기 =====");
            System.out.println("자동 입고 스케줄러: 3분 지난 REQUESTED 발주를 RECEIVED 처리합니다.");
            System.out.println("재고 정리 스케줄러: amount = 0인 Stock row를 삭제합니다.");
            Thread.sleep(190_000);

            System.out.println("\n===== 14. 자동 입고 후 발주 이력 조회 =====");
            brandSystem.printPurchaseHistory();

            System.out.println("\n===== 15. 자동 입고 후 재고 조회 =====");
            brandSystem.printStockStatus(productName);

            System.out.println("\n===== 16. 신규 상품 등록 테스트 =====");
            String newProductName = "조니워커 테스트라벨";

            brandSystem.registerNewProduct(
                    "위스키",
                    newProductName,
                    700,
                    new BigDecimal("70"),
                    new BigDecimal("95000"),
                    10
            );

            System.out.println("\n===== 17. 신규 상품 등록 후 내 브랜드 전체 재고 목록 조회 =====");
            brandSystem.printMyBrandStocks();

            System.out.println("\n===== 18. 신규 상품 등록 및 발주 요청 테스트 =====");
            String newProductName2 = "조니워커 테스트라벨2";

            brandSystem.registerNewProductAndPurchase(
                    "위스키",
                    newProductName2,
                    700,
                    new BigDecimal("80"),
                    new BigDecimal("108000"),
                    10,
                    20
            );

            System.out.println("\n===== 19. 신규 상품 발주 이력 조회 =====");
            brandSystem.printPurchaseHistoryByProductName(newProductName2);

            System.out.println("\n===== 20. 신규 상품 자동 입고 대기 =====");
            System.out.println("신규 상품 발주도 3분 뒤 Stock에 입고되어야 합니다.");
            Thread.sleep(190_000);

            System.out.println("\n===== 21. 신규 상품 입고 후 재고 조회 =====");
            brandSystem.printStockStatus(newProductName2);

            System.out.println("\n===== 22. 상품 삭제 테스트 =====");
            System.out.println("참조 중인 상품은 FK 제약조건 때문에 삭제 실패할 수 있습니다.");
            brandSystem.deleteProduct(newProductName);

            System.out.println("\n===== 23. 최종 내 브랜드 전체 재고 목록 조회 =====");
            brandSystem.printMyBrandStocks();

            System.out.println("\n===== 24. 로그아웃 =====");
            brandSystem.logout();

            System.out.println("\n===== BrandSystem 전체 통합 테스트 종료 =====");

        } catch (InterruptedException e) {
            System.out.println("[대기 중 인터럽트 발생]");
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            System.out.println("[테스트 중 오류 발생]");
            e.printStackTrace();

        } finally {
            purchaseScheduler.stop();
            cleanupScheduler.stop();

            System.out.println("===== 자동 입고 스케줄러 종료 =====");
            System.out.println("===== 재고 정리 스케줄러 종료 =====");
        }
    }
}