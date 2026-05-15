package admin.airportmanager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import admin.airportmanager.dto.AirportManagerLoginDto;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class AirportManagerDao {
	
	public AirportManagerLoginDto getLoginInfo(int managerId, String password) {
       
        String sql =
            " SELECT mg.managerId, mg.managerName, mg.managerType " 
            		+ " FROM   Manager mg " 
            		+ " JOIN   AirportManager am ON mg.managerId = am.managerId " 
            		+ " WHERE  mg.managerId  = ?  " 
            		+ " AND  mg.password   = ?  " 
            		+ " AND  mg.managerType = 'AIRPORT' ";
 
        try (Connection conn = OracleConnection.getConnection(); 
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, managerId);
            pstmt.setString(2, password);
 
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return AirportManagerLoginDto.builder()
                        .managerId(rs.getInt("managerId"))
                        .managerName(rs.getString("managerName"))
                        .managerType(rs.getString("managerType"))
                        .build();
                }
            }
        } catch (SQLException e) {
        	throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return null; 
    }
}