package product;

import java.math.BigDecimal;
import java.util.List;

import category.Category;
import common.Currency;
import product.dto.ProductDTO;

public class ProductService {

	private final ProductDAO productDAO = new ProductDAO();

	public List<ProductDTO> printAllProducts() {
		return productDAO.getAllProducts();
	}

	public List<ProductDTO> printAllProducts(Category category) {
		return productDAO.getProductsByCategory(category);
	}

	public ProductDTO printProduct(String productName) {
		return productDAO.getProductsByProductName(productName);
	}
	public List<ProductDTO> printProduct(BigDecimal minPrice, BigDecimal maxPrice , Currency currency) {
		return productDAO.getProductsFilterByPrice(minPrice, maxPrice, currency);
	}

}