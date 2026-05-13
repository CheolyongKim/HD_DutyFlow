package cart;

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
}
