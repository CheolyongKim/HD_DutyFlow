package product;

import java.util.List;

import category.Category;
import product.dto.productDTO;

public class ProductService {

	private final productDAO productDAO = new productDAO();

	public List<productDTO> printAllProducts() {
		return productDAO.getAllProducts();
	}

	public List<productDTO> printAllProducts(Category category) {
		return productDAO.getProductsByCategory(category);
	}

	public productDTO printProduct(String productName) {
		return productDAO.getProductsByProductName(productName);
	}

}