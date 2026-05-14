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
import exception.ErrorCode;
import exception.SystemException;
import pickup.dto.RealPickUpDTO;
import product.dto.productDTO;

public class PickUpDAO {

	public List<RealPickUpDTO> getAllRealPickUp() throws SystemException{
		List<RealPickUpDTO> realPickUpList = new ArrayList<RealPickUpDTO>();
		
		String sql = "";
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
				System.out.println("조회 결과 없음");
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		
		return realPickUpList;
	}
	
}
