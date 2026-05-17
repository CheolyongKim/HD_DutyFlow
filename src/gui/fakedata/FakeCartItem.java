package gui.fakedata;

import java.math.BigDecimal;

public class FakeCartItem {

    private final FakeProduct product;
    private int quantity;

    public FakeCartItem(FakeProduct product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public FakeProduct getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public BigDecimal getLineTotalKrw() {
        return product.getPriceKrw().multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getLineTotalUsd() {
        return product.getPriceUsd().multiply(BigDecimal.valueOf(quantity));
    }
}