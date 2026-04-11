package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/console_game"
            + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("[DB 연결 실패] 드라이버를 찾을 수 없습니다: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("[DB 연결 실패] " + e.getMessage());
            return null;
        }
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}