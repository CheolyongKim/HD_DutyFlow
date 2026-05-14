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
			
			String sql = "SELECT regulation_id, category_id, limit_capacity, established_date, overage_rate "
	                   + "FROM regulation WHERE category_id = ?";
	        
			  
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, categoryId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
			    RegulationDTO dto = new RegulationDTO();
				
			    dto.setRegulationId(rs.getInt("regulation_id"));
			    dto.setCategoryId(rs.getInt("category_id"));
			    dto.setLimitCapacity(rs.getInt("limit_capacity"));
			    dto.setEstablishedDate(rs.getDate("established_date").toLocalDate());
			    dto.setOverageRate(rs.getInt("overage_rate"));
			    
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
