package product;

import java.math.BigDecimal;
import java.util.List;

import category.Category;
import common.Currency;
import exception.SystemException;
import product.dto.ProductDTO;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<ProductDTO> printAllProducts() throws SystemException {
        return productDAO.getAllProducts();
    }

    public List<ProductDTO> printAllProducts(Category category) throws SystemException {
        return productDAO.getProductsByCategory(category);
    }

    public ProductDTO printProduct(String productName) throws SystemException {
        return productDAO.getProductsByProductName(productName);
    }

    public List<ProductDTO> printProduct(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Currency currency
    ) throws SystemException {
        return productDAO.getProductsFilterByPrice(minPrice, maxPrice, currency);
    }

    // 신규 상품 등록
    public void registerNewProduct(
            String brandName,
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue
    ) throws SystemException {

        if (brandName == null || brandName.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드명은 비어 있을 수 없습니다.");
        }

        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 비어 있을 수 없습니다.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("용량은 1 이상이어야 합니다.");
        }

        if (priceUsd == null || priceUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("달러 가격은 0 이상이어야 합니다.");
        }

        if (priceKrw == null || priceKrw.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("원화 가격은 0 이상이어야 합니다.");
        }

        if (thresholdValue < 0) {
            throw new IllegalArgumentException("임계값은 0 이상이어야 합니다.");
        }

        if (productDAO.existsByBrandNameAndProductName(brandName, productName)) {
            throw new IllegalStateException("이미 등록된 상품입니다. productName = " + productName);
        }

        int brandId = productDAO.findBrandIdByBrandName(brandName);
        int categoryId = productDAO.findCategoryIdByCategoryName(categoryName);

        int result = productDAO.insertProduct(
                categoryId,
                brandId,
                productName,
                capacity,
                priceUsd,
                priceKrw,
                thresholdValue
        );

        if (result == 1) {
            System.out.println("[신규 상품 등록 완료]");
            System.out.println("브랜드명: " + brandName);
            System.out.println("카테고리명: " + categoryName);
            System.out.println("상품명: " + productName);
        } else {
            System.out.println("[신규 상품 등록 실패]");
        }
    }


    public void deleteProduct(String brandName, String productName) throws SystemException {

        if (brandName == null || brandName.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드명은 비어 있을 수 없습니다.");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }

        if (!productDAO.existsByBrandNameAndProductName(brandName, productName)) {
            throw new IllegalStateException("삭제할 상품이 존재하지 않습니다. productName = " + productName);
        }

        int result = productDAO.deleteProductByBrandNameAndProductName(brandName, productName);

        if (result == 1) {
            System.out.println("[상품 삭제 완료]");
            System.out.println("브랜드명: " + brandName);
            System.out.println("상품명: " + productName);
        } else {
            System.out.println("[상품 삭제 실패]");
        }
    }
}