package stock.observer;

import product.Product;

public interface StockObserver {
    void onStockShortageDetected(Product product);
}
