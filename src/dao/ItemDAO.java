package dao;

import db.DBConnection;
import model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
public Item findById(String iId){
  String sql = "SELECT * FROM item WHERE i_id = ?";
  try (Connection conn = DBConnection.getConnection();
       PreparedStatement pstmt = conn.prepareStatement(sql)){
    pstmt.setString(1, iId);
    ResultSet rs = pstmt.executeQuery();

    if(rs.next()){
      return createItemFromResultSet(rs);
    }
  }catch (SQLException e) {
    e.printStackTrace();
  }
  return null;
}

public List<Item> findAllByTry(int tryNum) {
  List<Item> items = new ArrayList<>();
  String sql = """
      SELECT i.* FROM item i
      JOIN i_save isv ON i.i_id = isv.i_id
      WHERE isv.`try`= ?
      """;
  try (Connection conn = DBConnection.getConnection();
       PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, tryNum);
    ResultSet rs = pstmt.executeQuery();

    while (rs.next()) {
      Item item = createItemFromResultSet(rs);
      items.add(item);
    }
  } catch (SQLException e) {
    e.printStackTrace();
  }
  return items;
}
  private Item createItemFromResultSet(ResultSet rs) throws SQLException {
    return Item.createItem(
        rs.getString("i_id"),
        rs.getString("i_name"),
        rs.getInt("heal"),
        rs.getInt("power"),
        rs.getInt("hp"),
        rs.getString("i_desc"),
        rs.getString("i_use_msg"),
        rs.getString("i_img"),
        rs.getInt("try"),
        rs.getInt("pp")
    );
  }
  public int getTotalItemCount() {
    String sql = "SELECT COUNT(*) FROM item";
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

