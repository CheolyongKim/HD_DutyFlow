package payment;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentDAO {

    // 결제 요청 생성 - PENDING 상태로 저장
    public int insertPendingPayment(PaymentDTO paymentDTO) {
        int paymentId = getNextPaymentId();

        String sql = "INSERT INTO Payment (paymentId, orderId, paymentStatus, requestedAmount, cardNumberMask, requestedAt) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paymentId);
            pstmt.setInt(2, paymentDTO.getOrderId());
            pstmt.setString(3, PaymentStatus.PENDING.name());
            pstmt.setBigDecimal(4, paymentDTO.getAmount());
            pstmt.setString(5, paymentDTO.getCardNumberMask()); // 마스킹 처리된 카드번호
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();

            return paymentId;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // paymentId 시퀀스 조회
    private int getNextPaymentId() {
        String sql = "SELECT payment_seq.NEXTVAL FROM dual";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            throw new SystemException(ErrorCode.DB_CONNECTION);

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}