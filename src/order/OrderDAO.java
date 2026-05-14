package order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import order.dto.OrderDTO;

public class OrderDAO {

    private final String baseSql = 
    		"select * from orders join orderdetail on orders.orderid = orderdetail.orderid\r\n"
    		+ "join product on orderdetail.productid = product.productid ";

    /**
     * 빌더 패턴을 사용하여 Order 객체 매핑
     */
    private OrderDTO mapOrder(ResultSet rs) throws SQLException {
        return OrderDTO.builder()
                .orderId(rs.getInt("orderId"))
                .memberId(rs.getInt("memberId"))
                .reservationId(rs.getInt("reservationId"))
                .exchangeDate(rs.getDate("exchangeDate").toLocalDate())
                .orderedAt(rs.getTimestamp("orderedAt").toLocalDateTime())
                .orderState(convertStringToState(rs.getString("orderState")))
                .totalAmount(rs.getBigDecimal("totalAmount"))

                // OrderDetail
                .productId(rs.getInt("productId"))
                .quantity(rs.getInt("quantity"))
                .discountPrice(rs.getBigDecimal("discountPrice"))
                .dollarPrice(rs.getBigDecimal("dollarPrice"))

                // Product
                .productName(rs.getString("productName"))
                .build();
    }

    /**
     * 주문 상태 업데이트
     */
    public void update(Order order) {
        String sql = "UPDATE orders SET state = ? WHERE flightResNum = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 상태 클래스 명에서 "State"를 제외하고 대문자로 변환 (ex: PaidState -> PAID)
            String stateName = order.getState().getClass().getSimpleName()
                                    .replace("State", "").toUpperCase();
            
            pstmt.setString(1, stateName);
            pstmt.setInt(2, order.getFlightResNum());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 주문번호 + 상품 ID로 단건 조회
     */
    public OrderDTO findByorderIdAndProductId(int orderId, int productId) {
        String sql = baseSql + "WHERE orderdetail.orderId = ? and orderdetail.productId = ?";
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            
            System.out.println("sql = " + sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
                return null;            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 주문번호로 여러 상품 출력
     */
    public List<OrderDTO> findByOrderId(int orderId) {
        String sql = baseSql + "WHERE orderdetail.orderId = ?";
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            
            System.out.println("sql = " + sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<OrderDTO> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    
    /**
     * 전체 주문 목록 조회
     */
    public List<OrderDTO> findAll() {
        String sql = baseSql + "ORDER BY orderedAt DESC";
        System.out.println("sql="+sql);
        
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<OrderDTO> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 특정 사용자의 주문 목록 조회
     */
    
    public List<OrderDTO> findByLoginId(int memberId) {
        String sql = baseSql + "WHERE memberId = ? ORDER BY orderedAt DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<OrderDTO> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * DB의 텍스트 상태값을 구체적인 State 객체로 생성 (팩토리 로직)
     */
    private OrderState convertStringToState(String stateStr) {
        if (stateStr == null) {
            return new PendingState();
        }

        switch (stateStr.toUpperCase()) {
            case "PAID":
                return new PaidState();
            case "CANCEL":
                return new CancelState();
            case "PICKUPREADY":
                return new PickupReadyState();
            case "PICKUPDONE":
                return new PickupDoneState();
            default:
                return new PendingState();
        }
    }
}