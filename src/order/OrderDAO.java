package order;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import common.OrderStatus;
import exception.ErrorCode;
import exception.SystemException;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleTypes;
import order.dto.OrderDTO;
import order.state.CanceledState;
import order.state.NoShowState;
import order.state.PaidState;
import order.state.PendingState;
import order.state.PickedUpState;
import order.state.PickupReservedState;
import order.state.VerifiedState;

public class OrderDAO {

	private final String baseSql = "SELECT o.orderId, o.memberId, o.reservationId, "
			+ "o.exchangeDate, o.orderedAt, o.orderState, o.totalAmount, "
			+ "d.productId, d.quantity, d.discountPrice, d.dollarPrice, " + "p.productName, p.capacity, p.categoryId, "
			+ "b.brandName, " + "c.categoryName " + "FROM orders o " + "JOIN orderdetail d ON o.orderId = d.orderId "
			+ "JOIN product p ON d.productid = p.productid " + "JOIN brand b ON p.brandId = b.brandId "
			+ "JOIN category c ON p.categoryid = c.categoryid ";

	/**
	 * 빌더 패턴을 사용하여 Order 객체 매핑
	 */
	private OrderDTO mapOrder(ResultSet rs) throws SQLException {

		return OrderDTO.builder().orderId(rs.getInt("orderId")).memberId(rs.getInt("memberId"))
				.reservationId(rs.getInt("reservationId"))
				.exchangeDate(rs.getDate("exchangeDate") != null ? rs.getDate("exchangeDate").toLocalDate() : null)
				.orderedAt(rs.getTimestamp("orderedAt") != null ? rs.getTimestamp("orderedAt").toLocalDateTime() : null)
				.orderState(rs.getString("orderState")).totalAmount(rs.getBigDecimal("totalAmount"))
				.productId(rs.getInt("productId")).quantity(rs.getInt("quantity"))
				.discountPrice(rs.getBigDecimal("discountPrice")).dollarPrice(rs.getBigDecimal("dollarPrice"))
				.productName(rs.getString("productName")).brandName(rs.getString("brandName")) // 추가
				.categoryId(rs.getInt("categoryId")).categoryName(rs.getString("categoryName"))
				.capacity(rs.getInt("capacity")).build();
	}

	/**
	 * 주문 상태 및 최종 결제 금액 업데이트
	 */
	public void update(Order order) {
		String sql = "UPDATE orders SET orderState = ?, totalAmount = ? WHERE orderId = ?";
		String stateName = order.getState().name();

		System.out.println("[Debug] DB Update 시도 - OrderId: " + order.getOrderId() + ", StateName: " + stateName);

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, stateName);
			pstmt.setBigDecimal(2, order.getTotalPrice());
			pstmt.setInt(3, order.getOrderId());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}

	/**
	 * 주문번호 + 상품 ID로 단건 조회
	 */
	public OrderDTO findByorderIdAndProductId(int orderId, int productId) {

		String sql = baseSql + "WHERE d.orderId = ? " + "AND d.productId = ?";

		try (Connection conn = OracleConnection.getConnection();

				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, orderId);
			pstmt.setInt(2, productId);

			System.out.println("sql = " + sql);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					return mapOrder(rs);
				}

				return null;
			}

		} catch (SQLException e) {

			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}

	/**
	 * 주문번호로 여러 상품 출력
	 */
	public List<OrderDTO> findByOrderId(int orderId) {
		String sql = baseSql + " WHERE d.orderId = ?";
		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
	 * 상태 변경을 위해 DB에서 단일 Order 도메인 객체를 조회하여 반환
	 */
	public Order findOneOrderByOrderId(int orderId) {

		String sql = "SELECT orderId, memberId, orderState, totalAmount FROM orders WHERE orderId = ?";
		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					BigDecimal totalAmount = rs.getBigDecimal("totalAmount");
					String stateStr = rs.getString("orderState");
					OrderState currentState = convertStringToState(stateStr);
					Order order = new Order(rs.getInt("orderId"), currentState);
					order.setTotalPrice(totalAmount);
					order.setMemberId(rs.getInt("memberId"));
					return order;
				}
			}
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return null;
	}
	
	/**
	 * 브랜드명으로 주문/판매 내역 조회
	 */
	public List<OrderDTO> findOrdersByBrandName(String brandName) {

	    String sql = baseSql +
	            "WHERE b.brandName = ? " +
	            "ORDER BY o.orderedAt DESC";

	    try (Connection conn = OracleConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, brandName);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            List<OrderDTO> orders = new ArrayList<>();

	            while (rs.next()) {
	                orders.add(mapOrder(rs));
	            }

	            return orders;
	        }

	    } catch (SQLException e) {
	    	e.printStackTrace();
	        throw new SystemException(ErrorCode.DB_CONNECTION, e);
	    }
	}

	/**
	 * 전체 주문 목록 조회
	 */
	public List<OrderDTO> findAll() {
		String sql = baseSql + "ORDER BY orderedAt DESC";
		System.out.println("sql=" + sql);

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
		String sql = baseSql + "WHERE o.memberId = ? ORDER BY orderedAt DESC";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

		if (stateStr == null)
			return new PendingState();

		OrderStatus status = OrderStatus.valueOf(stateStr.toUpperCase());

		switch (status) {
		case ORDERED:
			return new PendingState();

		case VERIFIED:
			return new VerifiedState();

		case PAID:
			return new PaidState();

		case PICKUP_RESERVED:
			return new PickupReservedState();

		case PICKED_UP:
			return new PickedUpState();

		case CANCELED:
			return new CanceledState();

		case NO_SHOW:
			return new NoShowState();

		default:
			return new PendingState();
		}
	}

	
	/**
	 * 주문 번호(orderId)를 통해 해당 주문의 항공 예약 코드(reservationCode)를 조회합니다.
	 * 
	 * @param orderId 주문 ID
	 * @return 항공 예약 코드 (없을 경우 null)
	 */
	public String findReservationCodeByOrderId(int orderId) {
		// orders 테이블의 reservationId를 사용하여 flightbook 테이블과 JOIN
		String sql = "SELECT fb.reservationCode " + "FROM orders o "
				+ "JOIN flightbook fb ON o.reservationId = fb.reservationId " + "WHERE o.orderId = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, orderId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String resCode = rs.getString("reservationCode");
					System.out.println("[OrderDAO] 조회된 예약 코드: " + resCode + " (OrderId: " + orderId + ")");
					return resCode;
				}
			}
		} catch (SQLException e) {
			// 기존 ErrorCode 및 SystemException 구조 활용
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		System.out.println("[OrderDAO] 해당 주문에 연결된 예약 코드를 찾을 수 없습니다. (OrderId: " + orderId + ")");
		return null;
	}
	
	public int insertOrder(Order order, List<OrderDTO> items) {
	    String orderSql =
	            "INSERT INTO Orders " +
	            "(orderId, memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) " +
	            "VALUES (order_seq.NEXTVAL, ?, ?, " +
	            "(SELECT exchangeDate FROM ExchangeRate WHERE isLatest='Y'), " +
	            "SYSDATE, 'ORDERED', ?) " +
	            "RETURNING orderId INTO ?";

	    String detailSql =
	            "INSERT INTO OrderDetail " +
	            "(productId, orderId, quantity, discountPrice, dollarPrice) " +
	            "VALUES (?, ?, ?, ?, ?)";

	    Connection conn = null;

	    try {
	        conn = OracleConnection.getConnection();
	        conn.setAutoCommit(false); // 트랜잭션 시작

	        int orderId = -1;

	        // ------------------------------------------------
	        // 1. Orders insert (OraclePreparedStatement 캐스팅 사용)
	        // ------------------------------------------------
	        try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
	            OraclePreparedStatement opstmt = (OraclePreparedStatement) pstmt;

	            opstmt.setInt(1, order.getMemberId());         // 1번째 ? : memberId
	            opstmt.setInt(2, order.getReservationId());    // 2번째 ? : reservationId
	            opstmt.setBigDecimal(3, order.getTotalPrice());   // 3번째 ? : totalAmount

	            opstmt.registerReturnParameter(4, OracleTypes.INTEGER); 

	            opstmt.executeUpdate();

	            try (ResultSet rset = opstmt.getReturnResultSet()) {
	                if (rset.next()) {
	                    orderId = rset.getInt(1);
	                }
	            }
	        }

	        if (orderId == -1) {
	            throw new SQLException("Orders 테이블 insert 후 생성된 orderId를 가져오지 못했습니다.");
	        }

	        // ------------------------------------------------
	        // 2. OrderDetail insert (batch 처리)
	        // ------------------------------------------------
	        try (PreparedStatement pstmt = conn.prepareStatement(detailSql)) {

	            for (OrderDTO item : items) {
	                pstmt.setInt(1, item.getProductId());
	                pstmt.setInt(2, orderId); // 위에서 시퀀스로 발급받은 ID 연동
	                pstmt.setInt(3, item.getQuantity());
	                pstmt.setBigDecimal(4,
	                        item.getDiscountPrice() != null
	                                ? item.getDiscountPrice()
	                                : BigDecimal.ZERO
	                );
	                pstmt.setBigDecimal(5, item.getDollarPrice());

	                pstmt.addBatch();
	            }

	            pstmt.executeBatch();
	        }

	        conn.commit(); // 모든 insert가 성공하면 안전하게 최종 커밋
	        return orderId;

	    } catch (SQLException e) {
	        // 🔥 [중요] 중간에 에러가 터지면 커넥션을 확실하게 롤백하여 DB 데이터 독점을 방지합니다.
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	        throw new SystemException(ErrorCode.DB_CONNECTION, e);
	    } finally {
	        // 커넥션 자원 반납
	        if (conn != null) {
	            try { 
	                conn.close(); 
	            } catch (SQLException e) { 
	                e.printStackTrace(); 
	            }
	        }
	    }
	}
	
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
                    order.setOrderId(rs.getInt("orderId")); 
                    order.setReservationId(rs.getInt("reservationId"));
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
