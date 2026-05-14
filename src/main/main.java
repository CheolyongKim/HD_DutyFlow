package main;

import java.util.List;

import category.Category;
import product.ProductService;
import product.dto.productDTO;

public class main {

    public static void main(String[] args) {

        ProductService service = new ProductService();

        // =========================
        // 1. 전체 조회 테스트
        // =========================
        System.out.println("===== 전체 상품 조회 =====");

        List<productDTO> all = service.printAllProducts();

        for (productDTO p : all) {
            System.out.println(p);
        }

        // =========================
        // 2. 카테고리 조회 테스트
        // =========================
        System.out.println("===== 카테고리별 상품 조회 =====");

        Category category = Category.builder()
                .categoryName("전자제품") // DB에 있는 값으로 맞춰야 함
                .build();

        List<productDTO> byCategory = service.printAllProducts(category);

        for (productDTO p : byCategory) {
            System.out.println(p);
        }
    }
}