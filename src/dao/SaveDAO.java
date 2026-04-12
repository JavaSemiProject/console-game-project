package dao;

import db.DBConnection;
import model.Item;
import model.Save;
import model.Card;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static db.DBConnection.getConnection;


public class SaveDAO {
  public Save findLatest() {
    String sql = """
            SELECT s.*, GROUP_CONCAT(cs.c_id) as card_ids, 
                   GROUP_CONCAT(isv.i_id) as item_ids
            FROM save s
            LEFT JOIN c_save cs ON s.`try` = cs.`try`
            LEFT JOIN i_save isv ON s.`try` = isv.`try`
            GROUP BY s.`try`
            ORDER BY s.`try` DESC
            LIMIT 1
            """;

    Connection conn = getConnection();
    if (conn == null) return null;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        int tryNum = rs.getInt("try");
        List<Card> cards = new CardDAO().findAllByTry(tryNum);
        List<Item> items = new ItemDAO().findAllByTry(tryNum);

        return new Save(
            rs.getString("s_id"),
            rs.getInt("try"),
            rs.getTimestamp("t_time"),
            new ArrayList<>(),
            new ArrayList<>()
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int createSave() {
    String sql = "INSERT INTO save (`t_time`) VALUES (CURRENT_TIMESTAMP)";
    Connection conn = getConnection();
    if (conn == null) return -1;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) return rs.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  // 필요 시 업데이트용 (세이브 포인트 이동 시 s_id 업데이트)
  public boolean updateStage(String stageId, int tryNum) {
    String sql = "UPDATE save SET s_id = ? WHERE `try` = ?";
    Connection conn = getConnection();
    if (conn == null) return false;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, stageId);
      pstmt.setInt(2, tryNum);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public int getLatestTryNum() {
    String sql = "SELECT MAX(`try`) as latest FROM save";
    Connection conn = getConnection();
    if (conn == null) return -1;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
      if (rs.next()) {
        int latest = rs.getInt("latest");
        return latest > 0 ? latest : -1;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  public String getLatestStage(int tryNum) {
    String sql = "SELECT s_id FROM save WHERE `try` = ?";
    Connection conn = getConnection();
    if (conn == null) return null;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, tryNum);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) return rs.getString("s_id");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
