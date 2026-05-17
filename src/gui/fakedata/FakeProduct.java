package gui.fakedata;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FakeProduct {

    private final int productId;
    private final String productName;
    private final String brandName;
    private final String categoryName;
    private final int capacity;
    private final BigDecimal priceUsd;
    private final BigDecimal priceKrw;
    private final int stockAmount;
    private final String description;

    public FakeProduct(
            int productId,
            String productName,
            String brandName,
            String categoryName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int stockAmount,
            String description
    ) {
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.capacity = capacity;
        this.priceUsd = priceUsd;
        this.priceKrw = priceKrw;
        this.stockAmount = stockAmount;
        this.description = description;
    }
}
