package regulation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class RegulationDAO {
	
	public RegulationDTO getRegulationByCategoryId(int categoryId) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        
        try {
			conn = OracleConnection.getConnection();
			
			String sql = "SELECT regulationid, categoryid, limitcapacity, establisheddate, overagerate "
	                   + "FROM regulation WHERE categoryid = ?";
	        
			  
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, categoryId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
			    RegulationDTO dto = new RegulationDTO();
				
			    dto.setRegulationId(rs.getInt("regulationid"));
			    dto.setCategoryId(rs.getInt("categoryid"));
			    dto.setLimitCapacity(rs.getInt("limitcapacity"));
			    dto.setEstablishedDate(rs.getDate("establisheddate").toLocalDate());
			    dto.setOverageRate(rs.getInt("overagerate"));
			    
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
}
