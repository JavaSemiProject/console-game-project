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
        List<Card> cards = loadInventoryCards(tryNum);
        List<Item> items = loadInventoryItems(tryNum);

        return new Save(
            rs.getString("s_id"),
            tryNum,
            rs.getTimestamp("t_time"),
            cards,
            items
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

  /**
   * 보유 카드/아이템 스냅샷 저장 (DELETE 후 INSERT).
   * c_save/i_save(컬렉션 로그)와는 별개의 인벤토리 스냅샷 테이블 사용.
   */
  public void saveInventory(int tryNum, List<Card> cards, List<Item> items) {
    if (tryNum <= 0) return;
    Connection conn = getConnection();
    if (conn == null) return;
    try (conn) {
      conn.setAutoCommit(false);
      try (PreparedStatement delC = conn.prepareStatement("DELETE FROM c_inventory WHERE `try` = ?");
           PreparedStatement delI = conn.prepareStatement("DELETE FROM i_inventory WHERE `try` = ?")) {
        delC.setInt(1, tryNum); delC.executeUpdate();
        delI.setInt(1, tryNum); delI.executeUpdate();
      }
      if (cards != null && !cards.isEmpty()) {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO c_inventory (c_id, `try`, slot) VALUES (?, ?, ?)")) {
          for (int i = 0; i < cards.size(); i++) {
            ps.setString(1, cards.get(i).getCId());
            ps.setInt(2, tryNum);
            ps.setInt(3, i);
            ps.addBatch();
          }
          ps.executeBatch();
        }
      }
      if (items != null && !items.isEmpty()) {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO i_inventory (i_id, `try`, slot) VALUES (?, ?, ?)")) {
          for (int i = 0; i < items.size(); i++) {
            ps.setString(1, items.get(i).getIId());
            ps.setInt(2, tryNum);
            ps.setInt(3, i);
            ps.addBatch();
          }
          ps.executeBatch();
        }
      }
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<Card> loadInventoryCards(int tryNum) {
    List<Card> out = new ArrayList<>();
    String sql = "SELECT c_id FROM c_inventory WHERE `try` = ? ORDER BY slot";
    Connection conn = getConnection();
    if (conn == null) return out;
    try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, tryNum);
      try (ResultSet rs = ps.executeQuery()) {
        CardDAO cardDAO = new CardDAO();
        while (rs.next()) {
          Card c = cardDAO.findById(rs.getString("c_id"));
          if (c != null) out.add(c);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return out;
  }

  public List<Item> loadInventoryItems(int tryNum) {
    List<Item> out = new ArrayList<>();
    String sql = "SELECT i_id FROM i_inventory WHERE `try` = ? ORDER BY slot";
    Connection conn = getConnection();
    if (conn == null) return out;
    try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, tryNum);
      try (ResultSet rs = ps.executeQuery()) {
        ItemDAO itemDAO = new ItemDAO();
        while (rs.next()) {
          Item it = itemDAO.findById(rs.getString("i_id"));
          if (it != null) out.add(it);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return out;
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
