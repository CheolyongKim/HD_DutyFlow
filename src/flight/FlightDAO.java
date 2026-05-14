package flight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class FlightDAO {
	
	// 항공편 정보 조회 (항공편 코드)
	public FlightDTO getFlightByCode(String flightCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = OracleConnection.getConnection();
			String sql = "SELECT flightId, flightCode, departureAt, isDelayed " +
                    	 "FROM Flight WHERE flightCode = ?";
			  
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, flightCode);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				FlightDTO dto = new FlightDTO();
				
				dto.setFlightId(rs.getInt("flightId"));
                dto.setFlightCode(rs.getString("flightCode"));
                dto.setDepartureAt(rs.getTimestamp("departureAt").toLocalDateTime());
                dto.setIsDelayed(rs.getInt("isDelayed"));

                return dto;
			}
			
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
			
		} finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                throw new SystemException(ErrorCode.DB_CONNECTION, e);
            }
        }
		return null;
	}
	
	// 탑승 예약 정보 조회 (회원ID + 항공편코드)
	public FlightBookDTO getBookByMemberAndFlight(int memberId, String flightCode) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = OracleConnection.getConnection();
			String sql = "SELECT b.reservationId, b.memberId, b.flightId, b.reservationCode " +
                         "FROM FlightBook b " +
                         "JOIN Flight f ON b.flightId = f.flightId " +
                         "WHERE b.memberId = ? AND f.flightCode = ?";
			  
			pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, flightCode);

            rs = pstmt.executeQuery();
			
			if(rs.next()) {
				FlightBookDTO dto = new FlightBookDTO();
				
				dto.setReservationId(rs.getInt("reservationId"));
                dto.setMemberId(rs.getInt("memberId"));
                dto.setFlightId(rs.getInt("flightId"));
                dto.setReservationCode(rs.getString("reservationCode"));

                return dto;
			}
			
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
			
		} finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                throw new SystemException(ErrorCode.DB_CONNECTION, e);
            }
        }
		
		return null;
	}
	
	// 항공 정보 조회 (비행기 예약 코드)
	public FlightDTO getFlightByReservationCode(String reservationCode) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;

	    try {
	        conn = OracleConnection.getConnection();
	        String sql = "SELECT f.flightId, f.flightCode, f.departureAt, f.isDelayed " +
	                     "FROM Flight f " +
	                     "JOIN FlightBook b ON f.flightId = b.flightId " +
	                     "WHERE b.reservationCode = ?";

	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, reservationCode);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	        	
	        	FlightDTO dto = new FlightDTO();
				
	        	dto.setFlightId(rs.getInt("flightId"));
                dto.setFlightCode(rs.getString("flightCode"));
                dto.setDepartureAt(rs.getTimestamp("departureAt").toLocalDateTime());
                dto.setIsDelayed(rs.getInt("isDelayed"));
                
                return dto;
	        }

	    } catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION, e);

	    } finally {
	        try {
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            throw new SystemException(ErrorCode.DB_CONNECTION, e);
	        }
	    }

	    return null;
	}
	
	// 지연 시각 업데이트 (항공편코드 + 지연 시)
	public void updateDelayedFlight(String flightCode, LocalDateTime newDepartureAt) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;

	    try {
	        conn = OracleConnection.getConnection();
	        String sql = "UPDATE Flight SET departureAt = ?, isDelayed = 1 WHERE flightCode = ?";

	        pstmt = conn.prepareStatement(sql);
	        pstmt.setTimestamp(1, Timestamp.valueOf(newDepartureAt)); 
	        pstmt.setString(2, flightCode);                           
	        pstmt.executeUpdate();

	    } catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION, e);

	    } finally {
	        try {
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            throw new SystemException(ErrorCode.DB_CONNECTION, e);
	        }
	    }
	}

}
