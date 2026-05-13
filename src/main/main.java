package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import common.OracleConnection;

public class main {
	public static LocalDateTime curTime = LocalDateTime.now();

    public static void main(String[] args) {

    	// DB 연결 테스트입니다. 
        String sql = "SELECT * FROM ShoppingCart";

        try (
            Connection conn = OracleConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        ) {
            while(rs.next()) {
                System.out.println( rs.getInt("productId") + " / " + rs.getInt("memberId") + " / " + rs.getInt("amount"));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}