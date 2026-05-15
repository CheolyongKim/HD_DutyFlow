package order;

import java.math.BigDecimal;
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
import order.state.PaidState;
import order.state.PendingState;

public class OrderDAO {

	private final String baseSql = "SELECT o.orderId, o.memberId, o.reservationId, o.exchangeDate, o.orderedAt, o.orderState, o.totalAmount, "
			+ "d.productId, d.quantity, d.discountPrice, d.dollarPrice, " + "p.productName, p.capacity, p.categoryId, "
			+ "c.categoryName " + "FROM orders o " + "JOIN orderdetail d ON o.orderId = d.orderId "
			+ "JOIN product p ON d.productid = p.productid " + "JOIN category c ON p.categoryid = c.categoryid ";

	/**
	 * 빌더 패턴을 사용하여 Order 객체 매핑
	 */
	private OrderDTO mapOrder(ResultSet rs) throws SQLException {

		return OrderDTO.builder()
		        .orderId(rs.getInt("orderId"))
		        .memberId(rs.getInt("memberId"))
		        .reservationId(rs.getInt("reservationId"))
		        .exchangeDate(
		                rs.getDate("exchangeDate") != null
		                        ? rs.getDate("exchangeDate").toLocalDate()
		                        : null)
		        .orderedAt(
		                rs.getTimestamp("orderedAt") != null
		                        ? rs.getTimestamp("orderedAt").toLocalDateTime()
		                        : null)
		        .orderState(rs.getString("orderState"))
		        .totalAmount(rs.getBigDecimal("totalAmount"))
		        .productId(rs.getInt("productId"))
		        .quantity(rs.getInt("quantity"))
		        .discountPrice(rs.getBigDecimal("discountPrice"))
		        .dollarPrice(rs.getBigDecimal("dollarPrice"))
		        .productName(rs.getString("productName"))
		        .categoryId(rs.getInt("categoryId"))
		        .categoryName(rs.getString("categoryName"))
		        .capacity(rs.getInt("capacity"))
		        .build();
	}

	/**
	 * 주문 상태 및 최종 결제 금액 업데이트
	 */
	public void update(Order order) {

		String sql = "UPDATE orders " + "SET orderState = ?, totalAmount = ? " + "WHERE orderId = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			String stateName;

			if (order.getState() instanceof PaidState) {
				stateName = "PAID";
			} else {
				stateName = "ORDERED";
			}

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

		String sql = "SELECT orderId, orderState, totalAmount " + "FROM orders " + "WHERE orderId = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, orderId);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {

					int id = rs.getInt("orderId");

					BigDecimal totalAmount = rs.getBigDecimal("totalAmount");

					String stateStr = rs.getString("orderState");

					OrderState currentState = convertStringToState(stateStr);

					Order order = new Order(id, currentState);

					order.setTotalPrice(totalAmount);

					return order;
				}
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return null;
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

	    if (stateStr == null) {
	        return new PendingState();
	    }

	    switch (stateStr.toUpperCase()) {

	    case "ORDERED":
	        return new PendingState();

	    case "PAID":
	        return new PaidState();

	    default:
	        return new PendingState();
	    }
	}
	public int insertOrder(Order order, List<OrderDTO> items) {

		String orderSql = "INSERT INTO Orders "
				+ "(memberId, reservationId, exchangeDate, orderedAt, orderState, totalAmount) " + "VALUES (?, ?, "
				+ "(SELECT exchangeDate FROM ExchangeRate WHERE isLatest='Y'), " + "SYSDATE, 'ORDERED', ?)";

		String detailSql = "INSERT INTO OrderDetail " + "(productId, orderId, quantity, discountPrice, dollarPrice) "
				+ "VALUES (?, ?, ?, ?, ?)";

		Connection conn = null;

		try {
			conn = OracleConnection.getConnection();

			// 트랜잭션 시작
			conn.setAutoCommit(false);

			int orderId = 0;

			// 1. Orders insert
			try (PreparedStatement pstmt = conn.prepareStatement(orderSql, new String[] { "orderId" })) {

				pstmt.setInt(1, order.getMemberId());
				pstmt.setInt(2, order.getReservationId());
				pstmt.setBigDecimal(3, order.getTotalPrice());

				pstmt.executeUpdate();

				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						orderId = rs.getInt(1);
					}
				}
			}

			// 2. OrderDetail insert
			try (PreparedStatement pstmt = conn.prepareStatement(detailSql)) {

				for (OrderDTO item : items) {

					pstmt.setInt(1, item.getProductId());
					pstmt.setInt(2, orderId);
					pstmt.setInt(3, item.getQuantity());

					pstmt.setBigDecimal(4, item.getDiscountPrice() != null ? item.getDiscountPrice() : BigDecimal.ZERO);

					pstmt.setBigDecimal(5, item.getDollarPrice());

					pstmt.addBatch();
				}

				pstmt.executeBatch();
			}

			// 커밋
			conn.commit();

			return orderId;

		} catch (SQLException e) {

			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException rollbackEx) {
				rollbackEx.printStackTrace();
			}

			throw new SystemException(ErrorCode.DB_CONNECTION, e);

		} finally {

			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}