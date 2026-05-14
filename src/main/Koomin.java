package main;

import java.util.List;

import exception.SystemException;
import stock.dao.StockDao;
import stock.dao.StockPurchaseDao;
import stock.domain.Stock;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;
import stock.dto.StockProductDto;

public class Koomin {

    public static void main(String[] args) {

        StockDao stockDao = new StockDao();
        StockPurchaseDao stockPurchaseDao = new StockPurchaseDao();

        try {
            System.out.println("===== 1. 브랜드명으로 재고 상품 조회 =====");
            List<StockProductDto> brandStocks =
                    stockDao.getAllStockByBrandName("Johnnie Walker");

            for (StockProductDto dto : brandStocks) {
                System.out.println(dto);
            }

            System.out.println("\n===== 2. 상품명 기준 FIFO 재고 조회 =====");
            List<Stock> fifoStocks =
                    stockDao.findByProductNameOrderByManufacturedDate("조니워커 블루라벨");

            for (Stock stock : fifoStocks) {
                System.out.println(stock);
            }

            System.out.println("\n===== 3. 상품명 기준 총 재고 수량 조회 =====");
            int totalAmount =
                    stockDao.getTotalAmountByProductName("조니워커 블루라벨");

            System.out.println("조니워커 블루라벨 총 재고 = " + totalAmount);

            System.out.println("\n===== 4. 발주 요청 테스트 =====");
            int insertResult =
                    stockPurchaseDao.insertPurchaseByProductName("조니워커 블루라벨", 10);

            System.out.println("발주 요청 결과 = " + insertResult);

            System.out.println("\n===== 5. 전체 발주 이력 조회 =====");
            List<StockPurchase> purchases = stockPurchaseDao.findAll();

            for (StockPurchase purchase : purchases) {
                System.out.println(purchase);
            }

            System.out.println("\n===== 6. 상품명 기준 발주 이력 조회 =====");
            List<StockPurchase> productPurchases =
                    stockPurchaseDao.findByProductName("조니워커 블루라벨");

            for (StockPurchase purchase : productPurchases) {
                System.out.println(purchase);
            }

            System.out.println("\n===== 7. REQUESTED 상태 발주 조회 =====");
            List<StockPurchase> requestedPurchases =
                    stockPurchaseDao.findByStatus(StockPurchaseStatus.RECEIVED);

            for (StockPurchase purchase : requestedPurchases) {
                System.out.println(purchase);
            }

            System.out.println("\n===== 8. 발주 단건 조회 =====");
            if (!purchases.isEmpty()) {
                int purchaseId = purchases.get(0).getPurchaseId();

                StockPurchase purchase = stockPurchaseDao.findById(purchaseId);
                System.out.println("조회된 발주 = " + purchase);

                System.out.println("\n===== 9. 발주 상태 RECEIVED 변경 테스트 =====");
                int updateResult = stockPurchaseDao.markAsReceived(purchaseId);
                System.out.println("상태 변경 결과 = " + updateResult);

                StockPurchase updatedPurchase = stockPurchaseDao.findById(purchaseId);
                System.out.println("변경 후 발주 = " + updatedPurchase);
            }

        } catch (SystemException e) {
            System.out.println("[시스템 예외 발생]");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[알 수 없는 예외 발생]");
            e.printStackTrace();
        }
    }
}