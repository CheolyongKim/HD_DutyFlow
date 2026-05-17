package gui.fakedata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FakeMemberStore {

    private static final List<FakeProduct> products = new ArrayList<>();
    private static final List<FakeCartItem> cartItems = new ArrayList<>();
    private static final List<String> orderHistories = new ArrayList<>();

    static {
        products.add(new FakeProduct(
                1,
                "조니워커 블루라벨",
                "Johnnie Walker",
                "위스키",
                750,
                new BigDecimal("220.00"),
                new BigDecimal("298000"),
                19,
                "부드러운 풍미와 깊은 향을 가진 프리미엄 위스키입니다."
        ));

        products.add(new FakeProduct(
                2,
                "샤넬 No.5",
                "CHANEL",
                "향수",
                100,
                new BigDecimal("145.00"),
                new BigDecimal("196000"),
                8,
                "클래식한 플로럴 향을 가진 대표적인 여성 향수입니다."
        ));

        products.add(new FakeProduct(
                3,
                "디올 립글로우",
                "DIOR",
                "화장품",
                4,
                new BigDecimal("38.00"),
                new BigDecimal("51000"),
                25,
                "자연스러운 혈색을 살려주는 인기 립밤 제품입니다."
        ));

        products.add(new FakeProduct(
                4,
                "정관장 홍삼정",
                "정관장",
                "건강식품",
                240,
                new BigDecimal("95.00"),
                new BigDecimal("128000"),
                0,
                "면역력 관리에 도움을 주는 대표 홍삼 제품입니다."
        ));
    }

    public static List<FakeProduct> getProducts() {
        return products;
    }

    public static List<FakeCartItem> getCartItems() {
        return cartItems;
    }

    public static List<String> getOrderHistories() {
        return orderHistories;
    }

    public static void addToCart(FakeProduct product, int quantity) {
        for (FakeCartItem item : cartItems) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.addQuantity(quantity);
                return;
            }
        }

        cartItems.add(new FakeCartItem(product, quantity));
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static void createFakeOrder() {
        if (cartItems.isEmpty()) {
            return;
        }

        String orderText = "주문번호 #" + (orderHistories.size() + 1)
                + " / 주문일시: " + LocalDateTime.now()
                + " / 상품 수: " + cartItems.size()
                + "건";

        orderHistories.add(orderText);
        cartItems.clear();
    }
}