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

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        int tryNum = rs.getInt("try");
        List<Card> cards = new CardDAO().findAllByTry(tryNum);
        List<Item> items = new ItemDAO().findAllByTry(tryNum);

        return new Save(
            rs.getString("t_id"),      // 1. String t_id
            rs.getString("s_id"),      // 2. String lId  ← 이게 null이면 안 됨
            rs.getInt("try"),          // 3. int tryNum
            rs.getTimestamp("t_time"), // 4. Timestamp
            new ArrayList<>(),
            new ArrayList<>()
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int createSave(String startStageId) {
    String sql = "INSERT INTO save (t_id, s_id, t_time) VALUES (?, ?, CURRENT_TIMESTAMP)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setString(1, "player1");
      pstmt.setString(2, startStageId);
      pstmt.executeUpdate();
      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  // 필요 시 업데이트용 (세이브 포인트 이동 시 s_id 업데이트)
  public boolean updateStage(String stageId, int tryNum) {
    String sql = "UPDATE save SET s_id = ? WHERE `try` = ?";

    try (Connection conn = getConnection();
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
    try (Connection conn = getConnection();
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
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, tryNum);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("s_id");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  public boolean deleteAllSaves() {
    String sql = "DELETE FROM save";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      int deleted = pstmt.executeUpdate();
      System.out.println("[SaveDAO] 삭제된 세이브: " + deleted + "개");
      return deleted > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
  // 관련 테이블도 함께 삭제 (필요시)
  public boolean deleteAllGameData() {
    String[] tables = {"c_save", "i_save", "save"};  // 카드/아이템/세이브
    try (Connection conn = getConnection()) {
      for (String table : tables) {
        String sql = "DELETE FROM " + table;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.executeUpdate();
        }
      }
      System.out.println("[SaveDAO] 모든 게임 데이터 삭제 완료");
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}

