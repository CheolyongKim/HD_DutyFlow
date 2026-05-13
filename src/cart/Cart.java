package cart;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.ValidationException;
import product.Product;

public class Cart {
	// 장바구니 상품 목록
	private Map<Product, Integer> products = new HashMap<>();
	
	// 장바구니에 상품 추가
    public void addToCart(Product p, int wishAmount) {
    	
        // 상품 정보가 없는 경우
        if (p == null) {
            throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 추가하려는 수량이 0 이하인 경우
        if (wishAmount <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }

    	// 이미 장바구니에 존재하는 상품이면 기존 수량에 추가
    	// 존재하지 않으면 새로 추가
    	products.put(p, products.getOrDefault(p, 0) + wishAmount);
    }
    
    // 장바구니 내 특정 상품 수량 변경
    public void updateQuantity(Product p, int newAmount) {

        // 상품 정보가 없는 경우
        if (p == null) {
            throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 수량이 0 이하인 경우
        if (newAmount <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }

        // 장바구니에 존재하지 않는 상품인 경우
        if (!products.containsKey(p)) {
            throw new DataNotFoundException(ErrorCode.CART_PRODUCT_NOT_FOUND);
        }

        // 상품 수량 변경
        products.put(p, newAmount);
    }
    
    
    // 장바구니 콘솔 출력
    public void printCart() {

        if (products.isEmpty()) {
            System.out.println("장바구니가 비어 있습니다.");
            return;
        }
        
	    // 장바구니 출력 중 일부만 출력되고 예외가 발생하는 상황을 방지하기 위해
	    // 출력 전에 상품 정보 및 수량 데이터를 먼저 검증
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            
            // 상품 정보가 없는 경우
            if (product == null) {
                throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // 수량이 0 이하인 경우
            if (quantity <= 0) {
                throw new ValidationException(ErrorCode.INVALID_QUANTITY);
            }

            // 상품 가격 정보가 없는 경우
            if (product.getDollarPrice() == null || product.getWonPrice() == null) {
                throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
            }
        }

        int totalQuantity = 0;

        BigDecimal totalDollarPrice = BigDecimal.ZERO;
        BigDecimal totalWonPrice = BigDecimal.ZERO;

        System.out.println("=========== [ 장바구니 조회 ] =============");

        // 상품별 출력
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {

            Product product = entry.getKey();
            int quantity = entry.getValue();


            BigDecimal dollarPrice = product.getDollarPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal wonPrice = product.getWonPrice().multiply(BigDecimal.valueOf(quantity));

            // 상품명, 수량, 달러가격, 원화가격 출력
            System.out.println(product.getProductName() + "\t" + quantity + "\t" + dollarPrice + "\t" + wonPrice);

            // 총합 계산
            totalQuantity += quantity;
            totalDollarPrice = totalDollarPrice.add(dollarPrice);
            totalWonPrice = totalWonPrice.add(wonPrice);
        }

        System.out.println("====================================");

        // 총합 출력
        System.out.println("총합\t" + totalQuantity + "\t" + totalDollarPrice + "\t" + totalWonPrice);
    }
    
}
