package com.example.towerofhanoi;

import java.sql.*;

public class DBConnect {
    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;

    public DBConnect() {
        try
        {
            String url = "jdbc:sqlserver://localhost;database=TowerOfhanoi;user=sa;password=polman";
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
        }catch (Exception e){
            System.out.println("Error saat connect database: "+e);
        }
    }
    public void saveGameRecord(String username, int timeElapsed) {
        String sql = "INSERT INTO game_records (username, time_elapsed) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, timeElapsed);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saat menyimpan rekaman permainan: " + e.getMessage());
        }
    }
    public static void main(String[] args){
        DBConnect connection = new DBConnect();
        System.out.println("Connection berhasil.");
    }
}
