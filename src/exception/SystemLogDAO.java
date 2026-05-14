package exception;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import common.OracleConnection;

public class SystemLogDAO {

    public void save(String errorCode, String logMessage) {
    	
    	Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = OracleConnection.getConnection();
			
			String sql = "INSERT INTO SystemLog (errorCode, logMessage) VALUES (?, ?)";
			  
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			pstmt.setString(2, logMessage != null ? logMessage : "알 수 없는 오류");
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			System.err.println("[SystemLog] 저장 실패 " + e.getMessage());
			
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                
            }
        }
	}
		
}