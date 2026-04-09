package dao;

import db.DBConnection;
import model.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StageDAO {
  public Stage findById(String sId) {
    String sql = "SELECT * FROM stage WHERE s_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, sId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return new Stage(
            Integer.parseInt(rs.getString("s_id")),
            rs.getString("s_id"), // stageName으로 s_id 사용
            rs.getInt("row"),
            rs.getString("column"),
            rs.getInt("f_level")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Stage> findAll() {
    List<Stage> stages = new ArrayList<>();
    String sql = "SELECT * FROM stage ORDER BY f_level,`row`, `column`";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        stages.add(new Stage(
            Integer.parseInt(rs.getString("s_id")),
            rs.getString("s_id"),
            rs.getInt("row"),
            rs.getString("column"),
            rs.getInt("f_level")
        ));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stages;
  }

  public List<Stage> findByFloorLevel(int fLevel) {
    List<Stage> stages = new ArrayList<>();
    String sql = "SELECT * FROM stage WHERE f_level = ? ORDER BY `row`, `column`";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, fLevel);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        stages.add(new Stage(
            Integer.parseInt(rs.getString("s_id")),
            rs.getString("s_id"),
            rs.getInt("row"),
            rs.getString("column"),
            rs.getInt("f_level")
        ));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stages;
  }
}
