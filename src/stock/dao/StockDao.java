package stock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import stock.domain.Stock;
import stock.dto.StockProductDto;

public class StockDao {

    public List<StockProductDto> getAllStockByBrandName(String brandName) throws SystemException {

        List<StockProductDto> stockProductList = new ArrayList<>();

        String sql =
                "SELECT " +
                "    p.productName, " +
                "    p.stockAmount, " +
                "    p.capacity, " +
                "    p.priceUsd, " +
                "    p.priceKrw, " +
                "    p.thresholdValue, " +
                "    p.madeAt, " +
                "    b.brandName, " +
                "    c.categoryName, " +
                "    c.depth, " +
                "    s.amount, " +
                "    s.manufacturedDate " +
                "FROM product p " +
                "JOIN stock s ON p.productId = s.productId " +
                "JOIN brand b ON p.brandId = b.brandId " +
                "JOIN category c ON p.categoryId = c.categoryId " +
                "WHERE b.brandName = ?";

        System.out.println("sql = " + sql);

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);

            try (ResultSet rs = pstmt.executeQuery()) {

                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;

                    Category category = Category.builder()
                            .categoryName(rs.getString("categoryName"))
                            .depth(rs.getInt("depth"))
                            .build();

                    StockProductDto dto = StockProductDto.builder()
                            .category(category)
                            .productName(rs.getString("productName"))
                            .brandName(rs.getString("brandName"))
                            .stockAmount(rs.getInt("stockAmount"))
                            .capacity(rs.getInt("capacity"))
                            .priceUsd(rs.getBigDecimal("priceUsd"))
                            .priceKrw(rs.getBigDecimal("priceKrw"))
                            .thresholdValue(rs.getInt("thresholdValue"))
                            .madeAt(rs.getDate("madeAt") != null
                                    ? rs.getDate("madeAt").toLocalDate()
                                    : null)
                            .amount(rs.getInt("amount"))
                            .manufacturedDate(rs.getDate("manufacturedDate") != null
                                    ? rs.getDate("manufacturedDate").toLocalDate()
                                    : null)
                            .build();

                    stockProductList.add(dto);
                }

                if (!hasData) {
                    System.out.println("조회 결과 없음");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return stockProductList;
    }

    public List<Stock> findByProductNameOrderByManufacturedDate(String productName) throws SystemException {

        List<Stock> stockList = new ArrayList<>();

        String sql =
                "SELECT " +
                "    s.stockId, " +
                "    s.productId, " +
                "    s.manufacturedDate, " +
                "    s.amount " +
                "FROM stock s " +
                "JOIN product p ON s.productId = p.productId " +
                "WHERE p.productName = ? " +
                "  AND s.amount > 0 " +
                "ORDER BY s.manufacturedDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Stock stock = Stock.builder()
                            .stockId(rs.getInt("stockId"))
                            .productId(rs.getInt("productId"))
                            .manufacturedDate(rs.getDate("manufacturedDate") != null
                                    ? rs.getDate("manufacturedDate").toLocalDate()
                                    : null)
                            .amount(rs.getInt("amount"))
                            .build();

                    stockList.add(stock);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return stockList;
    }

    public int getTotalAmountByProductName(String productName) throws SystemException {

        String sql =
                "SELECT NVL(SUM(s.amount), 0) AS totalAmount " +
                "FROM stock s " +
                "JOIN product p ON s.productId = p.productId " +
                "WHERE p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalAmount");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return 0;
    }

    public int updateAmount(int stockId, int amount) throws SystemException {

        String sql =
                "UPDATE stock " +
                "SET amount = ? " +
                "WHERE stockId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setInt(2, stockId);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int insertStock(Stock stock) throws SystemException {

        String sql =
                "INSERT INTO stock ( " +
                "    productId, " +
                "    manufacturedDate, " +
                "    amount " +
                ") VALUES ( " +
                "    ?, " +
                "    ?, " +
                "    ? " +
                ")";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock.getProductId());
            pstmt.setDate(2, java.sql.Date.valueOf(stock.getManufacturedDate()));
            pstmt.setInt(3, stock.getAmount());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int findProductIdByProductName(String productName) throws SystemException {

        String sql =
                "SELECT productId " +
                "FROM product " +
                "WHERE productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("productId");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new SystemException(ErrorCode.DB_CONNECTION);
    }
}