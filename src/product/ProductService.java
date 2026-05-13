package product;

import java.util.List;

import product.dto.productDTO;

public class ProductService {

    private final productDAO productDAO = new productDAO();

    public List<productDTO> printAllProducts(){
        return productDAO.getAllProducts();
    }
}