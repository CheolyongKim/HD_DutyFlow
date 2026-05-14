package pickup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import common.OracleConnection;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import pickup.dto.RealPickUpDTO;
import product.dto.productDTO;

public class PickUpDAO {

	public List<RealPickUpDTO> getAllRealPickUp(String passportNum, int flightResNum) throws SystemException{
		List<RealPickUpDTO> realPickUpList = new ArrayList<RealPickUpDTO>();
		
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

			while (rs.next()) {
				hasData = true;

				RealPickUpDTO dto = RealPickUpDTO.builder()
				        .pickupAvailableAt(rs.getObject("pickupAvailableAt", LocalDateTime.class))
				        .passportNumber(rs.getString("passportNumber"))
				        .reservationCode(rs.getString("reservationCode"))
				        .departureAt(rs.getObject("departureAt", LocalDateTime.class))
				        .build();

				realPickUpList.add(dto);
			}

			if (!hasData) {
				throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("데이터 조회 결과 없음"));
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		
		return realPickUpList;
	}
	
}
