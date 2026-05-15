package order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class OrderDAO {
    // 픽업 시스템에 로드할 대기 중인 주문 목록을 불러옵니다.
    // 시뮬레이션 환경을 위해 CurrentTime.curTime을 기준으로 앞뒤 3시간을 계산합니다.
    public List<Order> getPendingOrders(LocalDateTime simulatedNow) throws SystemException {
        List<Order> orderList = new ArrayList<>();
        
        String sql = "SELECT O.orderId, O.reservationId, M.loginId, O.totalAmount, O.orderedAt "
                   + "FROM Orders O "
                   + "JOIN Pickup P ON O.orderId = P.orderId "
                   + "JOIN Member M ON O.memberId = M.memberId "
                   + "WHERE O.orderState = 'PICKUP_RESERVED' "
                   + "AND P.pickedUpAt IS NULL "
                   + "AND P.pickupAvailableAt BETWEEN ? AND ?";
                   
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // 현재(시뮬레이션) 시간 기준으로 -3시간, +3시간 바인딩
            pstmt.setTimestamp(1, Timestamp.valueOf(simulatedNow.minusHours(3)));
            pstmt.setTimestamp(2, Timestamp.valueOf(simulatedNow.plusHours(3)));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    Order order = new Order();
                    // order.setOrderId(rs.getInt("orderId")); // Order.java에 orderId 필드 추가 필수!
                    order.setFlightResNum(rs.getInt("reservationId"));
                    order.setLoginId(rs.getString("loginId"));
                    order.setTotalPrice(rs.getBigDecimal("totalAmount"));
                    order.setOrderedAt(rs.getTimestamp("orderedAt").toLocalDateTime());
                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return orderList;
    }
}