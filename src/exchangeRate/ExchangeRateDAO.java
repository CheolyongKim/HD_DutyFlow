package exchangeRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import exception.ErrorCode;
import exception.SystemException;

public class ExchangeRateDAO {
	// 최신 환율 조회
    public BigDecimal findLatestRate(Connection conn) {
        String sql = "SELECT exchangeRate FROM ExchangeRate WHERE isLatest = 'Y'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("exchangeRate");
            }

            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 기존 최신 환율의 flag값을 N으로 변경
    public void updateLatestToN(Connection conn) {
        String sql = "UPDATE ExchangeRate SET isLatest = 'N' WHERE isLatest = 'Y'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 오늘 환율 저장
    public void insertTodayRate(Connection conn, BigDecimal exchangeRate) {
        String sql =
            "INSERT INTO ExchangeRate(exchangeDate, exchangeRate, isLatest) "
        	+ "VALUES (TRUNC(SYSDATE), ?, 'Y')"; // 가장 최신 환율이므로 isLastest = Y로 설정

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, exchangeRate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}
