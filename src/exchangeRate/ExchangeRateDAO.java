package exchangeRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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
            int count = pstmt.executeUpdate();
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
    
    // 테스트용 메서드 / 호출 시 날짜 지정 가능
    public void insertRate(Connection conn, LocalDate exchangeDate, BigDecimal exchangeRate) {
        String sql =
            "INSERT INTO ExchangeRate(exchangeDate, exchangeRate, isLatest) " +
            "VALUES (?, ?, 'Y')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(exchangeDate));
            pstmt.setBigDecimal(2, exchangeRate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}
