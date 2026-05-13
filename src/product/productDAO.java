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

public class productDAO {

	public List<productDTO> getAllProducts() throws SystemException {

		List<productDTO> productList = new ArrayList<>();

		String sql = "select * from product join category using(categoryId) join brand using (brandId) join Event using (productId)";

		System.out.println("sql = " + sql);

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			boolean hasData = false;

			while (rs.next()) {
				hasData = true;

				Category category = Category.builder()
				        .categoryName(rs.getString("categoryName"))
				        .depth(rs.getInt("depth"))
				        .build();

				productDTO dto = productDTO.builder()
				        .category(category)
				        .productName(rs.getString("productName"))
				        .brandName(rs.getString("brandName"))
				        .stockAmount(rs.getInt("stockAmount"))
				        .capacity(rs.getInt("capacity"))
				        .priceUsd(rs.getBigDecimal("priceUsd"))
				        .priceKrw(rs.getBigDecimal("priceKrw"))
				        .discountRate(rs.getDouble("discountRate"))
				        .thresholdValue(rs.getInt("thresholdValue"))
				        .madeAt(rs.getDate("madeAt") != null
				                ? rs.getDate("madeAt").toLocalDate()
				                : null)
				        .build();

				productList.add(dto);
			}

			if (!hasData) {
				System.out.println("조회 결과 없음");
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return productList;
	}

}
