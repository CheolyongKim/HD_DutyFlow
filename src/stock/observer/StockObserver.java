package stock.observer;

public interface StockObserver {
    void onStockShortageDetected(String productName, int currentAmount, int thresholdValue);
}