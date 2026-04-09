package dao;

import db.DBConnection;
import model.Item;
import model.Save;
import model.Card;
import java.sql.*;
import java.util.List;


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

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        int tryNum = rs.getInt("try");
        List<Card> cards = new CardDAO().findAllByTry(tryNum);
        List<Item> items = new ItemDAO().findAllByTry(tryNum);

        return new Save(
            null,                         // lId
            tryNum,                         // tryNum
            rs.getTimestamp("time"), // time
            cards,                          // cards
            items                           // items
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int createSave() {
    String sql = "INSERT INTO save (`time`) VALUES (CURRENT_TIMESTAMP)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.executeUpdate();
      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }
}
