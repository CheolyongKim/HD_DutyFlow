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
			
			String sql = "SELECT regulationId, categoryId, limitCapacity, establishedDate, overageRate "
			           + "FROM Regulation WHERE categoryId = ?";
			  
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, categoryId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
			    RegulationDTO dto = new RegulationDTO();
		
			    dto.setRegulationId(rs.getInt("regulationId"));
			    dto.setCategoryId(rs.getInt("categoryId"));
			    dto.setLimitCapacity(rs.getInt("limitCapacity"));
			    dto.setEstablishedDate(rs.getDate("establishedDate").toLocalDate());
			    dto.setOverageRate(rs.getInt("overageRate"));
			    
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

	public RegulationDTO getByCategoryName(String categoryName) {

	    String sql =
	        "SELECT r.regulationId, r.categoryId, r.limitCapacity, r.establishedDate, r.overageRate " +
	        "FROM Regulation r " +
	        "JOIN Category c ON r.categoryId = c.categoryId " +
	        "WHERE c.categoryName = ?";

	    try (Connection conn = OracleConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, categoryName);

	        try (ResultSet rs = pstmt.executeQuery()) {

	            if (rs.next()) {
	                return RegulationDTO.builder()
	                        .regulationId(rs.getInt("regulationId"))
	                        .categoryId(rs.getInt("categoryId"))
	                        .limitCapacity(rs.getInt("limitCapacity"))
	                        .establishedDate(rs.getDate("establishedDate").toLocalDate())
	                        .overageRate(rs.getInt("overageRate"))
	                        .build();
	            }
	        }

	    } catch (Exception e) {
	        throw new RuntimeException("Regulation 조회 실패: " + categoryName, e);
	    }

	    return null;
	}
}
