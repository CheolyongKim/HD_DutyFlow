package product;

import java.math.BigDecimal;
import java.util.List;

import category.Category;
import common.Currency;
import exception.BusinessException;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.ValidationException;
import product.dto.ProductDTO;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<ProductDTO> printAllProducts() {
        return productDAO.getAllProducts();
    }

    public List<ProductDTO> printAllProducts(Category category) {

        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        return productDAO.getProductsByCategory(category);
    }

    public ProductDTO printProduct(String productName) {

        validateProductName(productName);

        ProductDTO product = productDAO.getProductsByProductName(productName);

        if (product == null) {
            throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return product;
    }

    public List<ProductDTO> printProduct(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Currency currency
    ) {
        if (minPrice == null || maxPrice == null || currency == null) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
        }

        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
        }

        return productDAO.getProductsFilterByPrice(minPrice, maxPrice, currency);
    }

    public List<ProductDTO> getProductsByBrandName(String brandName) {

        validateBrandName(brandName);

        return productDAO.getProductsByBrandName(brandName);
    }

    public ProductDTO getProductByBrandNameAndProductName(String brandName, String productName) {

        validateBrandName(brandName);
        validateProductName(productName);

        ProductDTO product = productDAO.getProductByBrandNameAndProductName(brandName, productName);

        if (product == null) {
            throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return product;
    }

    public boolean isBrandProduct(String brandName, String productName) {

        validateBrandName(brandName);
        validateProductName(productName);

        return productDAO.existsByBrandNameAndProductName(brandName, productName);
    }

    public void registerNewProduct(
            String brandName,
            String categoryName,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue
    ) {
        validateBrandName(brandName);
        validateCategoryName(categoryName);
        validateProductName(productName);

        if (capacity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INPUT);
        }

        if (priceUsd == null || priceUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
        }

        if (priceKrw == null || priceKrw.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
        }

        if (thresholdValue < 0) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INPUT);
        }

        if (productDAO.existsByBrandNameAndProductName(brandName, productName)) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS);
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

        if (result != 1) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_INPUT);
        }
    }

    public void deleteProduct(String brandName, String productName) {

        validateBrandName(brandName);
        validateProductName(productName);

        if (!productDAO.existsByBrandNameAndProductName(brandName, productName)) {
            throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        int result = productDAO.deleteProductByBrandNameAndProductName(brandName, productName);

        if (result != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATE);
        }
    }

    private void validateBrandName(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT_INPUT);
        }
    }
}