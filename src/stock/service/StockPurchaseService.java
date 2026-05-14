package stock.service;

import java.time.LocalDate;
import java.util.List;

import exception.SystemException;
import stock.dao.StockDao;
import stock.dao.StockPurchaseDao;
import stock.domain.Stock;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;

public class StockPurchaseService {

    private final StockPurchaseDao stockPurchaseDao = new StockPurchaseDao();
    private final StockDao stockDao = new StockDao();

    public void requestPurchase(String productName, int amount) throws SystemException {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("발주 수량은 1개 이상이어야 합니다.");
        }

        int result = stockPurchaseDao.insertPurchaseByProductName(productName, amount);

        if (result == 1) {
            System.out.println("[발주 요청 완료]");
            System.out.println("상품명: " + productName);
            System.out.println("발주 수량: " + amount);
            System.out.println("상태: " + StockPurchaseStatus.REQUESTED);
        } else {
            System.out.println("[발주 요청 실패] 존재하지 않는 상품명일 수 있습니다.");
        }
    }

    
    public void cancelPurchase(int purchaseId) throws SystemException {
        StockPurchase purchase = stockPurchaseDao.findById(purchaseId);

        if (purchase == null) {
            System.out.println("[발주 취소 실패] 존재하지 않는 발주입니다. purchaseId = " + purchaseId);
            return;
        }

        if (purchase.getStatus() != StockPurchaseStatus.REQUESTED) {
            System.out.println("[발주 취소 실패] REQUESTED 상태의 발주만 취소할 수 있습니다.");
            System.out.println("현재 발주 상태: " + purchase.getStatus());
            return;
        }

        int result = stockPurchaseDao.cancelPurchase(purchaseId);

        if (result == 1) {
            System.out.println("[발주 취소 완료] purchaseId = " + purchaseId);
        } else {
            System.out.println("[발주 취소 실패] 이미 입고 완료되었거나 취소된 발주입니다.");
        }
    }

    
    public void receivePurchase(int purchaseId) throws SystemException {
        StockPurchase purchase = stockPurchaseDao.findById(purchaseId);

        if (purchase == null) {
            System.out.println("[입고 실패] 존재하지 않는 발주입니다. purchaseId = " + purchaseId);
            return;
        }

        if (purchase.getStatus() != StockPurchaseStatus.REQUESTED) {
            System.out.println("[입고 실패] REQUESTED 상태의 발주만 입고 처리할 수 있습니다.");
            System.out.println("현재 발주 상태: " + purchase.getStatus());
            return;
        }

        Stock stock = Stock.builder()
                .productId(purchase.getProductId())
                .manufacturedDate(LocalDate.now())
                .amount(purchase.getAmount())
                .build();

        int insertResult = stockDao.insertStock(stock);
        int updateResult = stockPurchaseDao.markAsReceived(purchaseId);

        if (insertResult == 1 && updateResult == 1) {
            System.out.println("[입고 완료]");
            System.out.println("purchaseId = " + purchaseId);
            System.out.println("productId = " + purchase.getProductId());
            System.out.println("입고 수량 = " + purchase.getAmount());
        } else {
            System.out.println("[입고 처리 실패]");
        }
    }


    
    public void completeReceivablePurchases() throws SystemException {
        List<StockPurchase> purchases = stockPurchaseDao.findReceivablePurchases();

        for (StockPurchase purchase : purchases) {
            receivePurchase(purchase.getPurchaseId());
        }
    }


    
    public List<StockPurchase> getAllPurchaseHistory() throws SystemException {
        return stockPurchaseDao.findAll();
    }


    public List<StockPurchase> getPurchaseHistoryByProductName(String productName) throws SystemException {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }

        return stockPurchaseDao.findByProductName(productName);
    }


    public List<StockPurchase> getPurchaseHistoryByStatus(StockPurchaseStatus status) throws SystemException {
        if (status == null) {
            throw new IllegalArgumentException("발주 상태는 null일 수 없습니다.");
        }

        return stockPurchaseDao.findByStatus(status);
    }


    public StockPurchase getPurchaseById(int purchaseId) throws SystemException {
        return stockPurchaseDao.findById(purchaseId);
    }


    public List<StockPurchase> getRequestedPurchases() throws SystemException {
        return stockPurchaseDao.findRequestedPurchases();
    }


    public List<StockPurchase> getReceivedPurchases() throws SystemException {
        return stockPurchaseDao.findReceivedPurchases();
    }


    public void printPurchaseHistory(List<StockPurchase> purchases) {
        if (purchases == null || purchases.isEmpty()) {
            System.out.println("조회된 발주 이력이 없습니다.");
            return;
        }

        for (StockPurchase purchase : purchases) {
            System.out.println(purchase);
        }
    }
}