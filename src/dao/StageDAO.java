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
    Connection conn = DBConnection.getConnection();
    if (conn == null) return null;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, sId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        Stage stage = new Stage(
            rs.getString("s_id"),
            rs.getString("s_id"),
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),
            rs.getDouble("s_prob")
        );
        stage.setNId(rs.getString("n_id"));
        return stage;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Stage> findAll() {
    List<Stage> stages = new ArrayList<>();
    String sql = "SELECT * FROM stage ORDER BY f_level, `s_row`, `s_column`";
    Connection conn = DBConnection.getConnection();
    if (conn == null) return stages;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        Stage stage = new Stage(
            rs.getString("s_id"),
            rs.getString("s_id"),
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),
            rs.getDouble("s_prob")
        );
        stage.setNId(rs.getString("n_id"));
        stages.add(stage);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stages;
  }

  public List<Stage> findByFloorLevel(int fLevel) {
    List<Stage> stages = new ArrayList<>();
    String sql = "SELECT * FROM stage WHERE f_level = ? ORDER BY `s_row`, `s_column`";

    Connection conn = DBConnection.getConnection();
    if (conn == null) return stages;
    try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, fLevel);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        Stage stage = new Stage(
            rs.getString("s_id"),
            rs.getString("s_id"),
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),
            rs.getDouble("s_prob")
        );
        stage.setNId(rs.getString("n_id"));
        stages.add(stage);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stages;
  }
}
