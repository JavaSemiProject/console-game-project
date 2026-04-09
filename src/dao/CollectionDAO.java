package dao;

import db.DBConnection;
import model.Collection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.*;

public class CollectionDAO {
  public List<Collection> findAllByPlayerTry(int tryNum) {
    List<Collection> collections = new ArrayList<>();
    // Collection 테이블은 보류 상태이므로, ending 테이블 기준으로 구현
    String sql = "SELECT * FROM ending WHERE `try` > 0 ORDER BY e_id";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        Collection col = new Collection(
            0, // id (미구현)
            tryNum,
            "ENDING",
            Integer.parseInt(rs.getString("e_id").replace("E", "")),
            true,
            new Timestamp(System.currentTimeMillis()),
            rs.getInt("try")
        );
        collections.add(col);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return collections;
  }

  public void save(Collection collection) {

    if ("ENDING".equals(collection.getCollectionType())) {
      String sql = """
                UPDATE ending 
                SET `try` = ?, e_count = e_count + 1 
                WHERE e_id = ?
                """;
      try (Connection conn = DBConnection.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, collection.getFirstTry());
        pstmt.setString(2, "E" + collection.getContentId());
        pstmt.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public int getTotalEndingCount() {
    String sql = "SELECT COUNT(*) FROM ending";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }
}
