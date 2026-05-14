package brandSystem;

import stock.observer.StockObserver;

public class BrandSystem implements StockObserver {

    @Override
    public void onStockShortageDetected(String productName, int currentAmount, int thresholdValue) {
        System.out.println("[재고 부족 알림]");
        System.out.println("상품명: " + productName);
        System.out.println("현재 재고: " + currentAmount);
        System.out.println("기준 수량: " + thresholdValue);
    }
}
