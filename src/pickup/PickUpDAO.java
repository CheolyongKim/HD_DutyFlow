package pickup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import common.OracleConnection;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import member.Grade;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpDAO {
	
	public AppendQueueDTO getAppendingInfo(String passportNum, int flightResNum) throws SystemException{
		AppendQueueDTO dto = null;
		
		String sql = ""
				+ "SELECT F.flightCode, F.departureAt, F.isDelayed, M.memberId, M.grade"
				+ "FROM Flight F JOIN FlightBook B USING(flightId)"
				+ "				 JOIN Member M USING(memberId)"
				+ "WHERE M.passportNum=? AND B.flightResNum=?;";
		System.out.println("sql = " + sql);
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
		    conn = OracleConnection.getConnection();

		    pstmt = conn.prepareStatement(sql);

		    pstmt.setString(1, passportNum);
		    pstmt.setInt(2, flightResNum);

		    rs = pstmt.executeQuery();

		    boolean hasData = false;

		    if (rs.next()) {
		        hasData = true;

		        dto = AppendQueueDTO.builder()
		                .flightCode(rs.getString("flightCode"))
		                .departureAt(rs.getObject("departureAt", LocalDateTime.class))
		                .isDelayed(rs.getInt("isDelayed"))
		                .memberId(rs.getInt("memberId"))
		                .grade(Grade.fromPriority(rs.getInt("grade")))
		                .build();
		    }
		    if (!hasData) {
		        throw new DataNotFoundException(
		                ErrorCode.DATA_NOT_FOUND,
		                new Exception("데이터 조회 결과 없음")
		        );
		    }
		} catch (SQLException e) {
		    throw new SystemException(ErrorCode.DB_CONNECTION, e);
		} finally {
		    try {
		    		rs.close();
		    		pstmt.close();
		    		conn.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		}
		return dto;
	}

	public List<PickUpDTO> getAllPickUp(String passportNum, int flightResNum) throws SystemException{
		List<PickUpDTO> pickUpList = new ArrayList<PickUpDTO>();
		
		String sql = ""
				+ "SELECT P.pickUpAvailableAt, M.passportNumber, B.reservationCode, F.departureAt"
				+ "FROM PickUp P JOIN Orders O USING(orderId)"
				+ "				 JOIN Member M USING(memberId)"
				+ "				 JOIN FlightBook B USING(memberId)"
				+ "				 JOIN Flight F USING(flightId)"
				+ "WHERE M.passportNumber=? AND B.reservationCode=?;";
		System.out.println("sql = " + sql);
		
		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			boolean hasData = false;
			
			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			while (rs.next()) {
				hasData = true;

				PickUpDTO dto = PickUpDTO.builder()
				        .pickupAvailableAt(rs.getObject("pickupAvailableAt", LocalDateTime.class))
				        .passportNumber(rs.getString("passportNumber"))
				        .reservationCode(rs.getString("reservationCode"))
				        .departureAt(rs.getObject("departureAt", LocalDateTime.class))
				        .build();

				pickUpList.add(dto);
			}

			if (!hasData) {
				throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("데이터 조회 결과 없음"));
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		
		return pickUpList;
	}
	
}
