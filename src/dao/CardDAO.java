package dao;


import db.DBConnection;
import model.Card;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
import java.util.*;
import model.*;

public class CardDAO {
  public Card findById(String cId) {
    String sql = """
      SELECT c.*, (SELECT MIN(cs.`try`) FROM c_save cs WHERE cs.c_id = c.c_id) as first_try
      FROM card c WHERE c.c_id = ?
    """;
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, cId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return createCardFromResultSet(rs);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Card> findAllByTry(int tryNum) {
    List<Card> cards = new ArrayList<>();
    String sql = """
      SELECT c.*, (SELECT MIN(cs2.`try`) FROM c_save cs2 WHERE cs2.c_id = c.c_id) as first_try
      FROM card c
      JOIN c_save cs ON c.c_id = cs.c_id
      WHERE cs.`try` = ?
    """;
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, tryNum);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        cards.add(createCardFromResultSet(rs));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return cards;
  }

  private Card createCardFromResultSet(ResultSet rs) throws SQLException {
    return Card.createAttackCard(
        rs.getString("c_id"),
        rs.getInt("pp"),
        rs.getString("c_name"),
        rs.getString("c_desc"),
        rs.getInt("c_power"),
        rs.getString("c_desc"),
        rs.getString("c_use_msg"),
        rs.getString("c_img"),
        rs.getInt("first_try")
    );
  }

  public int getTotalCardCount() {
    String sql = "SELECT COUNT(*) FROM card";
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

