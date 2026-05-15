package pickup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import member.Grade;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;
// DBUtil 등 필요한 임포트 유지

public class PickUpDAO {

	public List<PickUpDTO> getAllPickUp(String passportNum, int flightResNum) {
		List<PickUpDTO> pickUpList = new ArrayList<>();
		
		String sql = ""
		        + "SELECT P.pickUpAvailableAt, M.passportNumber, B.reservationCode, F.departureAt\n"
		        + "FROM PickUp P \n"
		        + "JOIN Orders O ON P.orderId = O.orderId\n"
		        + "JOIN Member M ON O.memberId = M.memberId\n"
		        + "JOIN FlightBook B ON O.reservationId = B.reservationId AND M.memberId = B.memberId\n"
		        + "JOIN Flight F ON B.flightId = F.flightId\n"
		        + "WHERE M.passportNumber = ? AND B.reservationId = ?";
		
		try (Connection conn = OracleConnection.getConnection(); // 팀의 DB 연결 클래스명에 맞게 수정
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

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
					// 데이터가 없으면 DataNotFoundException 던짐
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("일치하는 예약/여권 정보가 없습니다."));
				}
			}
		} catch (SQLException e) {
			// SQL 예외는 SystemException으로 래핑하여 던짐
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		
		return pickUpList;
	}

	public AppendQueueDTO getAppendingInfo(String passportNum, int flightResNum) {
		AppendQueueDTO aqdto = null;
		
		String sql = ""
		        + "SELECT F.flightCode, F.departureAt, F.isDelayed, M.memberId, M.grade, M.name \n"
		        + "FROM Flight F \n"
		        + "JOIN FlightBook B ON F.flightId = B.flightId \n"
		        + "JOIN Member M ON B.memberId = M.memberId \n"
		        + "WHERE M.passportNumber = ? AND B.reservationId = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					aqdto = AppendQueueDTO.builder()
					        .flightCode(rs.getString("flightCode"))
					        .departureAt(rs.getObject("departureAt", LocalDateTime.class))
					        .isDelayed(rs.getInt("isDelayed"))
					        .memberId(rs.getInt("memberId"))
					        .grade(Grade.valueOf(rs.getString("grade").toUpperCase()))
					        .name(rs.getString("name"))
					        .build();
				} else {
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("큐 삽입용 회원 정보를 찾을 수 없습니다."));
				}
			}
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		
		return aqdto;
	}
}