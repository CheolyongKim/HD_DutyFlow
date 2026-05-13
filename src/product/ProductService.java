package product;

import java.util.List;

import category.Category;
import product.dto.productDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;

public class ProductService {

	/***
	 * 
	 * @return
	 */
	public List<productDTO> printAllProducts() {

		List<productDTO> productList = new ArrayList<>();

		String sql = "select * from product join category using(categoryId)";
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
		        dto.setAmount(rs.getInt("amount"));
		        dto.setCapacity(rs.getInt("capacity"));
		        dto.setDollarPrice(rs.getBigDecimal("dollarPrice"));
		        dto.setWonPrice(rs.getBigDecimal("wonPrice"));
		        dto.setEventSaleRate(rs.getDouble("eventSaleRate"));
		        dto.setThreshold(rs.getInt("threshold"));
		        dto.setMadeAt(rs.getDate("madeAt").toLocalDate());
		        	
		        productList.add(dto);
		    }

		    if (!hasData) {
		        System.out.println("조회 결과 없음");
		    }

		} catch (Exception e) {
		    e.printStackTrace();
		}

		return productList;
	}

	public static void main(String[] args) {

		ProductService service = new ProductService();
		List<productDTO> list = service.printAllProducts();

		for (productDTO dto : list) {
			System.out.println(dto.getProductName() + " / " + dto.getProductName() + " / " + dto.getAmount() + " / "
					+ dto.getCapacity() + " / " + dto.getDollarPrice() + " / " + dto.getWonPrice() + " / "
					+ dto.getEventSaleRate() + " / " + dto.getThreshold());
		}
	}
}