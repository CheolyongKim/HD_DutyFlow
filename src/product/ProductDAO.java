package product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import product.dto.ProductDTO;
import common.Currency;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class ProductDAO {
  
    String baseSql =
            "SELECT p.productId, p.productName, c.categoryId, c.categoryName, c.depth, " +
            "b.brandName, " +
            "NVL(st.totalAmount, 0) AS stockAmount, " +
            "p.capacity, p.priceUsd, p.priceKrw, " +
            "NVL(e.discountRate, 0) AS discountRate, " +
            "CASE WHEN e.productId IS NULL THEN 'N' ELSE 'Y' END AS hasEvent, " +
            "p.thresholdValue, " +
            "(p.priceKrw * (1 - NVL(e.discountRate, 0) / 100)) AS finalPriceKrw, " +
            "(p.priceUsd * (1 - NVL(e.discountRate, 0) / 100)) AS finalPriceUsd " +
            "FROM product p " +
            "JOIN category c ON p.categoryId = c.categoryId " +
            "JOIN brand b ON p.brandId = b.brandId " +
            "LEFT JOIN event e ON p.productId = e.productId " +
            "LEFT JOIN ( " +
            "    SELECT productId, SUM(amount) AS totalAmount " +
            "    FROM stock " +
            "    GROUP BY productId " +
            ") st ON p.productId = st.productId ";

    /**
     * productDto에 데이터 넣는 기능 공통 메서드화
     */
    private ProductDTO mapProduct(ResultSet rs) throws SQLException {

        Category category = Category.builder()
                .categoryId(rs.getInt("categoryId"))
                .categoryName(rs.getString("categoryName"))
                .depth(rs.getInt("depth"))
                .build();

        return ProductDTO.builder()
                .category(category)
                .productName(rs.getString("productName"))
                .brandName(rs.getString("brandName"))
                .capacity(rs.getInt("capacity"))
                .priceUsd(rs.getBigDecimal("priceUsd"))
                .priceKrw(rs.getBigDecimal("priceKrw"))
                .discountRate(rs.getDouble("discountRate"))
                .hasEvent("Y".equalsIgnoreCase(rs.getString("hasEvent")))
                .finalPriceKrw(rs.getBigDecimal("finalPriceKrw"))
                .finalPriceUsd(rs.getBigDecimal("finalPriceUsd"))
                .thresholdValue(rs.getInt("thresholdValue"))
                .build();
    }

    /**
     * 모든 상품 조회
     */
    public List<ProductDTO> getAllProducts() throws SystemException {

        String sql = baseSql;

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<ProductDTO> productList = new ArrayList<>();

            while (rs.next()) {
                productList.add(mapProduct(rs));
            }

            return productList;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 해당하는 카테고리의 상품 조회
     */
    public List<ProductDTO> getProductsByCategory(Category category) throws SystemException {

        String sql = baseSql + "WHERE c.categoryName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getCategoryName());

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ProductDTO> productList = new ArrayList<>();

                while (rs.next()) {
                    productList.add(mapProduct(rs));
                }

                return productList;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 특정 하나의 상품을 출력하는 메서드
     */
    public ProductDTO getProductsByProductName(String productName) throws SystemException {

        String sql = baseSql + "WHERE p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 금액 범위 지정한 상품 list 보기
     */
    public List<ProductDTO> getProductsFilterByPrice(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Currency currency
    ) throws SystemException {

        String priceExpr = currency.equals(Currency.USD)
                ? "p.priceUsd"
                : "p.priceKrw";

        String finalPriceExpr =
                "(" + priceExpr + " * (1 - NVL(e.discountRate, 0) / 100))";

        String sql = baseSql
                + "WHERE " + finalPriceExpr + " BETWEEN ? AND ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, minPrice);
            pstmt.setBigDecimal(2, maxPrice);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ProductDTO> productList = new ArrayList<>();

                while (rs.next()) {
                    productList.add(mapProduct(rs));
                }

                return productList;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
  
  	// 상품 원화 가격 업데이트 
	public void updateAllPriceKrw(Connection conn, BigDecimal exchangeRate) {
	    String sql = "UPDATE Product SET priceKrw = ROUND(priceUsd * ?, 0)";

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setBigDecimal(1, exchangeRate);
	        pstmt.executeUpdate();

	    } catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION, e);
	    }
	}
}