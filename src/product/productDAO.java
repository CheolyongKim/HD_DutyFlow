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

        String sql =
            "select * from product " +
            "join category using(categoryId) " +
            "join brand using (brandId) " +
            "join Event using (productId)";

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
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return productList;
    }
	
}
