package dao;

import db.DBConnection;
import model.Hero;
import model.NPC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NPCDAO {
  private static final String HERO_ID = "H001";  // Hero를 어떤 n_id로 저장할지 정의

  public NPC findById(String nId) {
    String sql = "SELECT * FROM npc WHERE n_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, nId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return new NPC(
            rs.getString("n_id"),
            rs.getString("n_name"),
            rs.getString("n_desc"),
            rs.getInt("hp"),
            rs.getBoolean("is_boss"),
            rs.getInt("power_min"),
            rs.getInt("power_max"),
            rs.getInt("pp"),
            rs.getString("c_id"),
            rs.getString("i_id")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  // HERO를 NPC DAO 안에서 조회 (Hero 테이블 없음, npc 테이블로 관리)
  public Hero findHero() {
    String sql = "SELECT * FROM npc WHERE n_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, HERO_ID);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return new Hero(
            rs.getString("n_id"),
            rs.getInt("hp"),
            rs.getInt("power_min"),
            rs.getInt("power_max"),
            rs.getInt("pp"),
            rs.getString("c_id")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int getTotalNpcCount() {
    String sql = "SELECT COUNT(*) FROM npc";
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
