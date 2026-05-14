package main;

import java.math.BigDecimal;
import java.util.List;

import category.Category;
import common.Currency;
import product.ProductService;
import product.dto.ProductDTO;

public class chaeyeonMain {

    public static void main(String[] args) {

        ProductService service = new ProductService();

        // =========================
        // 1. 전체 조회 테스트
        // =========================
        System.out.println("===== 전체 상품 조회 =====");

        List<ProductDTO> all = service.printAllProducts();

        for (ProductDTO p : all) {
            System.out.println(p);
        }

        // =========================
        // 2. 카테고리 조회 테스트
        // =========================
        System.out.println("===== 카테고리별 상품 조회 =====");

        Category category = Category.builder()
                .categoryName("전자제품") // DB에 있는 값으로 맞춰야 함
                .build();

        List<ProductDTO> byCategory = service.printAllProducts(category);

        for (ProductDTO p : byCategory) {
            System.out.println(p);
        }
        
	     // =========================
	     // 3. 상품명으로  단건 조회 테스트
	     // =========================
	     System.out.println("===== 상품명으로 상품 1개 조회 =====");
	     ProductDTO product = service.printProduct("조니워커 블루라벨");
	     System.out.println(product);
	     
	     // =========================
	     //특정 금액 범위 테스트
	     // =========================

	        BigDecimal min = new BigDecimal("100");
	        BigDecimal max = new BigDecimal("50000");
	
	        try {
	            List<ProductDTO> list =
	                service.printProduct(min, max, Currency.KRW);
	
	            System.out.println("===== 결과 =====");
	            for (ProductDTO p : list) {
	                System.out.println(p);
	            }
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
    
	     
    }
}