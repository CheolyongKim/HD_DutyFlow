package product;

import java.util.List;

import category.Category;
import product.dto.productDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class ProductService {


	public List<productDTO> printAllProducts() throws SystemException {

		List<productDTO> productList = new ArrayList<>();

		String sql = "select * from product join category using(categoryId) join brand using (brandId) join Event using (productId)";
		System.out.println("sql = " + sql);

		try (Connection conn = OracleConnection.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {

		    boolean hasData = false;

		    while (rs.next()) {
		        hasData = true;

		        productDTO dto = new productDTO();

		        Category category = new Category();
		        category.setCategoryName(rs.getString("categoryName"));
		        category.setDepth(rs.getInt("depth"));
		        dto.setCategory(category);
		        
		        dto.setProductName(rs.getString("productName"));
		        dto.setBrandName(rs.getString("brandName"));
		        
		        dto.setStockAmount(rs.getInt("stockAmount"));
		        dto.setCapacity(rs.getInt("capacity"));
		        dto.setPriceUsd(rs.getBigDecimal("priceUsd"));
		        dto.setPriceKrw(rs.getBigDecimal("priceKrw"));
		        dto.setDiscountRate(rs.getDouble("discountRate"));
		        dto.setThresholdValue(rs.getInt("thresholdValue"));
		        dto.setMadeAt(rs.getDate("madeAt").toLocalDate());
		        	
		        productList.add(dto);
		    }

		    if (!hasData) {
		        System.out.println("조회 결과 없음");
		    }

		} catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION, e); //현재는 DB 를 Main으로 빼지 않았기 때문에 SystemError로 구현
	    }

		return productList;
	}

//	public static void main(String[] args) {
//
//		ProductService service = new ProductService();
//		List<productDTO> list = service.printAllProducts();
//		
//		for (productDTO dto : list) {
//		    System.out.println("========================================");
//		    System.out.println("상품명      : " + dto.getProductName());
//		    System.out.println("카테고리     : " + dto.getCategory().getCategoryName());
//		    System.out.println("브랜드       : " + dto.getBrandName());
//		    System.out.println("재고        : " + dto.getStockAmount());
//		    System.out.println("용량        : " + dto.getCapacity());
//		    System.out.println("USD 가격    : " + dto.getPriceUsd());
//		    System.out.println("KRW 가격    : " + dto.getPriceKrw());
//		    System.out.println("할인율       : " + dto.getDiscountRate());
//		    System.out.println("임계값       : " + dto.getThresholdValue());
//		    System.out.println("========================================\n");
//		}
//
//	}
	
}