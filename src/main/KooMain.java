package main;

import brandSystem.BrandSystem;
import stock.service.StockService;

public class KooMain {

    public static void main(String[] args) {
        StockService stockService = new StockService();

        stockService.registerObserver(new BrandSystem());

        String productName = "조니워커 블루라벨";

        try {
            System.out.println("===== Observer 테스트 =====");

            int beforeAmount = stockService.getTotalAmountByProductName(productName);
            System.out.println("차감 전 총 재고 = " + beforeAmount);

            int orderAmount = 10;
            System.out.println(orderAmount + "개 구매 처리");

            stockService.deductStockFIFO(productName, orderAmount);

            int afterAmount = stockService.getTotalAmountByProductName(productName);
            System.out.println("차감 후 총 재고 = " + afterAmount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}