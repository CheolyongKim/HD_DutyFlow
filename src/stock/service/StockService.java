package stock.service;

import java.util.ArrayList;
import java.util.List;

import exception.SystemException;
import stock.dao.StockDao;
import stock.domain.Stock;
import stock.observer.StockObserver;

public class StockService {

    private final StockDao stockDao = new StockDao();
    private final List<StockObserver> listObservers = new ArrayList<>();

    public void registerObserver(StockObserver observer) {
        listObservers.add(observer);
    }

    public void deleteObserver(StockObserver observer) {
        listObservers.remove(observer);
    }

    private void notifyObservers(
            String brandName,
            String productName,
            int currentAmount,
            int thresholdValue
    ) {
        for (StockObserver observer : listObservers) {
            observer.onStockShortageDetected(
                    brandName,
                    productName,
                    currentAmount,
                    thresholdValue
            );
        }
    }

    public int getTotalAmountByProductName(String productName) throws SystemException {
        return stockDao.getTotalAmountByProductName(productName);
    }

    public int getTotalAmountByProductId(int productId) throws SystemException {
        return stockDao.getTotalAmountByProductId(productId);
    }

    public boolean canSell(String productName, int amount) throws SystemException {
        int totalAmount = stockDao.getTotalAmountByProductName(productName);
        return totalAmount >= amount;
    }

    public boolean canSell(int productId, int amount) throws SystemException {
        int totalAmount = stockDao.getTotalAmountByProductId(productId);
        return totalAmount >= amount;
    }

    public void deductStockFIFO(String productName, int orderAmount) throws SystemException {

        if (orderAmount <= 0) {
            throw new IllegalArgumentException("구매 수량은 1개 이상이어야 합니다.");
        }

        int productId = stockDao.findProductIdByProductName(productName);

        if (!canSell(productId, orderAmount)) {
            throw new IllegalStateException("재고가 부족합니다. 상품명: " + productName);
        }

        List<Stock> stockList = stockDao.findByProductIdOrderByManufacturedDate(productId);

        int remainAmount = orderAmount;

        for (Stock stock : stockList) {
            if (remainAmount == 0) {
                break;
            }

            int currentAmount = stock.getAmount();

            if (currentAmount >= remainAmount) {
                int newAmount = currentAmount - remainAmount;
                stockDao.updateAmount(stock.getStockId(), newAmount);
                remainAmount = 0;
            } else {
                stockDao.updateAmount(stock.getStockId(), 0);
                remainAmount -= currentAmount;
            }
        }

        checkThreshold(productName);
    }

    public void checkThreshold(String productName) throws SystemException {
        int productId = stockDao.findProductIdByProductName(productName);

        int currentAmount = stockDao.getTotalAmountByProductId(productId);
        int thresholdValue = stockDao.getThresholdValueByProductId(productId);
        String brandName = stockDao.getBrandNameByProductId(productId);

        if (currentAmount <= thresholdValue) {
            notifyObservers(brandName, productName, currentAmount, thresholdValue);
        }
    }
    
    public int deleteEmptyStocks() throws SystemException {
        return stockDao.deleteZeroAmountStocks();
    }
    
    public boolean isBrandProduct(String brandName, String productName) throws SystemException {
        String actualBrandName = stockDao.getBrandNameByProductName(productName);
        return brandName.equals(actualBrandName);
    }
    
    public void printAllStockByBrandName(String brandName) throws SystemException {
        stockDao.getAllStockByBrandName(brandName)
                .forEach(System.out::println);
    }
    
}