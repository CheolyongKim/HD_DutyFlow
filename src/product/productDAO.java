package product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import product.dto.productDTO;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class productDAO {

	/***
	 * productDto에 데이터 넣는 기능 공통 메서드화
	 * 
	 * @param rs(DB에서 가져온 결과값 여러개)
	 * @return: ProductDto
	 * @throws SQLException
	 */
	// =========================
	// 공통 매핑 로직 (핵심)
	// =========================
	private productDTO mapProduct(ResultSet rs) throws SQLException {

		Category category = Category.builder().categoryName(rs.getString("categoryName")).depth(rs.getInt("depth"))
				.build();

		return productDTO.builder().category(category).productName(rs.getString("productName"))
				.brandName(rs.getString("brandName")).stockAmount(rs.getInt("stockAmount"))
				.capacity(rs.getInt("capacity")).priceUsd(rs.getBigDecimal("priceUsd"))
				.priceKrw(rs.getBigDecimal("priceKrw")).discountRate(rs.getDouble("discountRate"))
				.thresholdValue(rs.getInt("thresholdValue"))
				.madeAt(rs.getDate("madeAt") != null ? rs.getDate("madeAt").toLocalDate() : null).build();
	}

	/***
	 * 모든 상품 조회
	 * 
	 * @return List<ProductDto>
	 * @throws SystemException
	 * 
	 */
	public List<productDTO> getAllProducts() throws SystemException {

		String sql = "select * from product join category using(categoryId) join brand using(brandId) join event using(productId)";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			List<productDTO> productList = new ArrayList<>();

			while (rs.next()) {
				productList.add(mapProduct(rs));
			}

			return productList;

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}

	/***
	 * 해당하는 카테고리의 상품 조회
	 * 
	 * @return List<ProductDto>
	 * @throws SystemException
	 * 
	 */
	public List<productDTO> getProductsByCategory(Category category) {

		String sql = "select * from product join category using(categoryId) join brand using(brandId) join event using(productId) where category.categoryName = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, category.getCategoryName());
			pstmt.setInt(2, category.getDepth());
			try (ResultSet rs = pstmt.executeQuery()) {
				List<productDTO> productList = new ArrayList<>();
				while (rs.next()) {
					productList.add(mapProduct(rs));
				}
				return productList;
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}

}