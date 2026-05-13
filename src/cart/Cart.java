package cart;

import java.util.HashMap;
import java.util.Map;

import product.Product;

public class Cart {
	// 장바구니 상품 목록
	private Map<Product, Integer> products = new HashMap<>();
	
	// 장바구니에 상품 추가
    public void addToCart(Product p, int wishAmount) {

    	// 이미 장바구니에 존재하는 상품이면 기존 수량에 추가
    	// 존재하지 않으면 새로 추가
    	products.put(p, products.getOrDefault(p, 0) + wishAmount);
    }
}
