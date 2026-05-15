package brandSystem;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import product.ProductService;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;
import stock.observer.StockObserver;
import stock.service.StockPurchaseService;
import stock.service.StockService;

@Getter
public class BrandSystem implements StockObserver {

    private final String brandName;
    private final StockService stockService;
    private final StockPurchaseService purchaseService;
    private final ProductService productService;
    
    public BrandSystem(String brandName) {
        this.brandName = brandName;
        this.stockService = new StockService();
        this.purchaseService = new StockPurchaseService();
        this.productService = new ProductService();

        this.stockService.registerObserver(this); // StockObserver를 구현한 BrandSystem을 stockService에 등록
    }


    public void receiveOrder(String productName, int amount) {
        if (!validateMyBrandProduct(productName)) {
            return;
        }
        
        try {
            System.out.println("===== [" + brandName + "] 주문 접수 =====");
            System.out.println("상품명: " + productName);
            System.out.println("주문 수량: " + amount);

            stockService.deductStockFIFO(productName, amount);

            System.out.println("[주문 처리 완료] 재고가 차감되었습니다.");

        } catch (IllegalStateException e) {
            System.out.println("[주문 처리 실패]");
            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println("[주문 처리 중 오류 발생]");
            e.printStackTrace();
        }
    }


    public void makePurchase(String productName, int amount) {
        if (!validateMyBrandProduct(productName)) {
            return;
        }
        
        try {
            System.out.println("===== [" + brandName + "] 발주 요청 =====");
            purchaseService.requestPurchase(productName, amount);

        } catch (Exception e) {
            System.out.println("[발주 요청 실패]");
            e.printStackTrace();
        }
    }

  
    public void cancelPurchase(int purchaseId) {
        try {
            System.out.println("===== [" + brandName + "] 발주 취소 =====");
            purchaseService.cancelPurchase(purchaseId);

        } catch (Exception e) {
            System.out.println("[발주 취소 실패]");
            e.printStackTrace();
        }
    }

    public void receivePurchase(int purchaseId) {
        try {
            System.out.println("===== [" + brandName + "] 발주 입고 처리 =====");
            purchaseService.receivePurchase(purchaseId);

        } catch (Exception e) {
            System.out.println("[발주 입고 처리 실패]");
            e.printStackTrace();
        }
    }
    
    public void registerNewProduct(
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue
    ) {
        try {
            System.out.println("===== [" + brandName + "] 신규 상품 등록 =====");

            productService.registerNewProduct(
                    brandName,
                    categoryName,
                    productName,
                    capacity,
                    priceUsd,
                    priceKrw,
                    thresholdValue
            );

        } catch (Exception e) {
            System.out.println("[신규 상품 등록 실패]");
            e.printStackTrace();
        }
    }
    
    public void registerNewProductAndPurchase(
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue,
            int purchaseAmount
    ) {
        try {
            System.out.println("===== [" + brandName + "] 신규 상품 등록 및 발주 요청 =====");

            productService.registerNewProduct(
                    brandName,
                    categoryName,
                    productName,
                    capacity,
                    priceUsd,
                    priceKrw,
                    thresholdValue
            );

            purchaseService.requestPurchase(productName, purchaseAmount);

            System.out.println("[신규 상품 등록 및 발주 요청 완료]");
            System.out.println("브랜드명: " + brandName);
            System.out.println("상품명: " + productName);
            System.out.println("발주 수량: " + purchaseAmount);

        } catch (Exception e) {
            System.out.println("[신규 상품 등록 및 발주 요청 실패]");
            System.out.println(e.getMessage());
        }
    }

    public void deleteProduct(String productName) {
        try {
            System.out.println("===== [" + brandName + "] 상품 삭제 =====");

            productService.deleteProduct(brandName, productName);

        } catch (Exception e) {
            System.out.println("[상품 삭제 실패]");
            System.out.println(e.getMessage());
        }
    }
    
    public void printStockStatus(String productName) {
	    if (!validateMyBrandProduct(productName)) {
	        return;
	    }
	    
	    try {
	        int totalAmount = stockService.getTotalAmountByProductName(productName);
	
	        System.out.println("===== [" + brandName + "] 재고 조회 =====");
	        System.out.println("상품명: " + productName);
	        System.out.println("총 재고: " + totalAmount);
	
	    } catch (Exception e) {
	        System.out.println("[재고 조회 실패]");
	        e.printStackTrace();
	    }
	}

	public void printMyBrandStocks() {
        try {
            System.out.println("===== [" + brandName + "] 전체 재고 목록 =====");
            stockService.printAllStockByBrandName(brandName);

        } catch (Exception e) {
            System.out.println("[브랜드 전체 재고 조회 실패]");
            e.printStackTrace();
        }
    }

    public void printPurchaseHistory() {
        try {
            System.out.println("===== [" + brandName + "] 브랜드 발주 이력 =====");

            List<StockPurchase> purchases =
                    purchaseService.getPurchaseHistoryByBrandName(brandName);

            purchaseService.printPurchaseHistory(purchases);

        } catch (Exception e) {
            System.out.println("[발주 이력 조회 실패]");
            e.printStackTrace();
        }
    }


    public void printPurchaseHistoryByProductName(String productName) {
        try {
            System.out.println("===== [" + brandName + "] 상품별 발주 이력 =====");
            System.out.println("상품명: " + productName);

            List<StockPurchase> purchases =
                    purchaseService.getPurchaseHistoryByProductName(productName);

            purchaseService.printPurchaseHistory(purchases);

        } catch (Exception e) {
            System.out.println("[상품별 발주 이력 조회 실패]");
            e.printStackTrace();
        }
    }


    public void printPurchaseHistoryByStatus(StockPurchaseStatus status) {
        try {
            System.out.println("===== [" + brandName + "] 상태별 발주 이력 =====");
            System.out.println("상태: " + status);

            List<StockPurchase> purchases =
                    purchaseService.getPurchaseHistoryByStatus(status);

            purchaseService.printPurchaseHistory(purchases);

        } catch (Exception e) {
            System.out.println("[상태별 발주 이력 조회 실패]");
            e.printStackTrace();
        }
    }

    @Override
    public void onStockShortageDetected(
            String brandName,
            String productName,
            int currentAmount,
            int thresholdValue
    ) {
        if (!this.brandName.equals(brandName)) {
            return;
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println("[브랜드 시스템 재고 부족 알림]");
        System.out.println("브랜드명: " + brandName);
        System.out.println("상품명: " + productName);
        System.out.println("현재 총 재고: " + currentAmount);
        System.out.println("기준 수량: " + thresholdValue);
        System.out.println("재고가 기준 수량 이하입니다. 발주 검토가 필요합니다.");
        System.out.println("====================================");
        System.out.println();
    }
    
    public void logout() {
        System.out.println("[" + brandName + "] 브랜드 시스템 로그아웃");
    }
    
    private boolean validateMyBrandProduct(String productName) {
        try {
            if (!stockService.isBrandProduct(brandName, productName)) {
                System.out.println("[처리 불가] 해당 브랜드의 상품이 아닙니다.");
                System.out.println("브랜드 시스템: " + brandName);
                System.out.println("요청 상품명: " + productName);
                return false;
            }

            return true;

        } catch (Exception e) {
            System.out.println("[브랜드 상품 검증 실패]");
            e.printStackTrace();
            return false;
        }
    }
}