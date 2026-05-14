package stock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;

public class StockPurchaseDao {

    public int insertPurchaseByProductName(String productName, int amount) throws SystemException {

        String sql =
                "INSERT INTO StockPurchase ( " +
                "    productId, " +
                "    purchaseDate, " +
                "    amount, " +
                "    status " +
                ") " +
                "SELECT " +
                "    productId, " +
                "    SYSDATE, " +
                "    ?, " +
                "    ? " +
                "FROM Product " +
                "WHERE productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setString(2, StockPurchaseStatus.REQUESTED.name());
            pstmt.setString(3, productName);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public StockPurchase findById(int purchaseId) throws SystemException {

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "WHERE purchaseId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, purchaseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToStockPurchase(rs);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return null;
    }

    public List<StockPurchase> findAll() throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "ORDER BY purchaseId DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                purchaseList.add(mapToStockPurchase(rs));
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchase> findByProductName(String productName) throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT sp.purchaseId, sp.productId, sp.purchaseDate, sp.amount, sp.status " +
                "FROM StockPurchase sp " +
                "JOIN Product p ON sp.productId = p.productId " +
                "WHERE p.productName = ? " +
                "ORDER BY sp.purchaseId DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchase(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchase> findByStatus(StockPurchaseStatus status) throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "WHERE status = ? " +
                "ORDER BY purchaseDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchase(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchase> findRequestedPurchases() throws SystemException {
        return findByStatus(StockPurchaseStatus.REQUESTED);
    }

    public List<StockPurchase> findReceivedPurchases() throws SystemException {
        return findByStatus(StockPurchaseStatus.RECEIVED);
    }

    public int updateStatus(int purchaseId, StockPurchaseStatus status) throws SystemException {

        String sql =
                "UPDATE StockPurchase " +
                "SET status = ? " +
                "WHERE purchaseId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, purchaseId);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public List<StockPurchase> findReceivablePurchases() throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "WHERE status = ? " +
                "  AND purchaseDate <= SYSDATE - (3 / 1440) " +
                "ORDER BY purchaseDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, StockPurchaseStatus.REQUESTED.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchase(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public int updateStatusIfCurrentStatus(
            int purchaseId,
            StockPurchaseStatus currentStatus,
            StockPurchaseStatus nextStatus
    ) throws SystemException {

        String sql =
                "UPDATE StockPurchase " +
                "SET status = ? " +
                "WHERE purchaseId = ? " +
                "  AND status = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nextStatus.name());
            pstmt.setInt(2, purchaseId);
            pstmt.setString(3, currentStatus.name());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int cancelPurchase(int purchaseId) throws SystemException {
        return updateStatusIfCurrentStatus(
                purchaseId,
                StockPurchaseStatus.REQUESTED,
                StockPurchaseStatus.CANCELED
        );
    }

    public int markAsReceived(int purchaseId) throws SystemException {
        return updateStatusIfCurrentStatus(
                purchaseId,
                StockPurchaseStatus.REQUESTED,
                StockPurchaseStatus.RECEIVED
        );
    }

    private StockPurchase mapToStockPurchase(ResultSet rs) throws SQLException {
        return StockPurchase.builder()
                .purchaseId(rs.getInt("purchaseId"))
                .productId(rs.getInt("productId"))
                .purchaseDate(rs.getDate("purchaseDate") != null
                        ? rs.getDate("purchaseDate").toLocalDate()
                        : null)
                .amount(rs.getInt("amount"))
                .status(StockPurchaseStatus.valueOf(rs.getString("status")))
                .build();
    }
}