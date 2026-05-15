package member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import common.Grade;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

import common.OracleConnection;

public class MemberDAO {

	public Member findById(int memberId) {
	    String sql = "SELECT memberId, loginId, name, grade FROM Member WHERE memberId = ?";
	    // (수정) 처음에는 null로 초기화
	    Member member = null;

	    try (Connection conn = OracleConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, memberId);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                member = new Member(); // (수정) 결과가 있을 때만 객체 생성
	                member.setMemberId(rs.getInt("memberId"));
	                member.setLoginId(rs.getString("loginId"));
	                member.setName(rs.getString("name"));
	                
	                String gradeStr = rs.getString("grade");
	                if (gradeStr != null) {
	                    member.setGrade(Grade.valueOf(gradeStr.toUpperCase()));
	                }
	            }
	        }
	    } catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION);
	    }
	    return member; // 결과가 없으면 null 리턴
	}
	
}
