package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import common.OracleConnection;

public class Application {
	public static LocalDateTime curTime = LocalDateTime.now();

    public static void main(String[] args) {

    	// DB 연결 테스트입니다. 
        String sql = "SELECT * FROM Brand";

        try (
            Connection conn = OracleConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        ) {
            while(rs.next()) {
                System.out.println( rs.getInt("brandId") + " / " + rs.getString("brandName") + " / " + rs.getInt("managerId"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}