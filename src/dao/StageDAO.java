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
        // 1. stageId, 2. stageName, 3. row, 4. column, 5. fLevel, 6. s_type, 7. s_prob
        return new Stage(
            rs.getString("s_id"),           // stageId
            rs.getString("s_id"),           // stageName (s_id 그대로)
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),         // s_type 필드명 맞추기
            rs.getDouble("s_prob")          // s_prob (prob이 아니라 s_prob)
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Stage> findAll() {
    List<Stage> stages = new ArrayList<>();
    String sql = "SELECT * FROM stage ORDER BY f_level, `s_row`, `s_column`";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        stages.add(new Stage(
            rs.getString("s_id"),
            rs.getString("s_id"),
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),
            rs.getDouble("s_prob")
        ));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stages;
  }

  public Stage findStartByFloor(int floorLevel) {
    String sql = "SELECT * FROM stage WHERE f_level = ? AND s_type = 'start' LIMIT 1";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, floorLevel);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return new Stage(
            rs.getString("s_id"),
            rs.getString("s_id"),
            rs.getInt("s_row"),
            rs.getString("s_column"),
            rs.getInt("f_level"),
            rs.getString("s_type"),
            rs.getDouble("s_prob")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }


}
