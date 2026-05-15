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
		        + "SELECT F.flightCode, F.departureAt, F.isDelayed, M.memberId, M.grade, M.name \n" // <-- M.name 추가
		        + "FROM Flight F \n"
		        + "JOIN FlightBook B ON F.flightId = B.flightId \n"
		        + "JOIN Member M ON B.memberId = M.memberId \n"
		        + "WHERE M.passportNumber = ? AND B.reservationId = ?";	
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

		     // ResultSet 안에서 값을 꺼낼 때 name 추가
		        dto = AppendQueueDTO.builder()
		                .flightCode(rs.getString("flightCode"))
		                .departureAt(rs.getObject("departureAt", LocalDateTime.class))
		                .isDelayed(rs.getInt("isDelayed"))
		                .memberId(rs.getInt("memberId"))
		                .grade(Grade.valueOf(rs.getString("grade").toUpperCase()))
		                .name(rs.getString("name")) // <-- 추가된 부분!
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

	public List<PickUpDTO> getAllPickUp(String passportNum, int flightResNum) throws SystemException {
		List<PickUpDTO> pickUpList = new ArrayList<PickUpDTO>();
		
		// 1. SQL 끝에 있던 세미콜론(;) 제거 완료
		// PickUpDAO.java 의 sql 문자열을 아래 코드로 덮어씌워주세요!

		String sql = ""
		        + "SELECT P.pickUpAvailableAt, M.passportNumber, B.reservationCode, F.departureAt\n"
		        + "FROM PickUp P \n"
		        + "JOIN Orders O ON P.orderId = O.orderId\n"
		        + "JOIN Member M ON O.memberId = M.memberId\n"
		        + "JOIN FlightBook B ON O.reservationId = B.reservationId AND M.memberId = B.memberId\n"
		        + "JOIN Flight F ON B.flightId = F.flightId\n"
		        + "WHERE M.passportNumber = ? AND B.reservationId = ?";
				
		System.out.println("sql = " + sql);
		
		// 2. Connection과 PreparedStatement만 먼저 열기
		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// 3. 쿼리 실행(executeQuery) 전에 파라미터 세팅을 가장 먼저 수행!
			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			// 4. 파라미터가 모두 채워진 pstmt로 ResultSet을 열기 (중첩 try-with-resources)
			try (ResultSet rs = pstmt.executeQuery()) {
				boolean hasData = false;

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
			} // ResultSet 자동 반납

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		} // PreparedStatement, Connection 자동 반납
		
		return pickUpList;
	}
	
}
