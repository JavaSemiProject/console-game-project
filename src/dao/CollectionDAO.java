package dao;

import db.DBConnection;
import model.CollectionEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionDAO {

    // ============================================
    // 획득/조우 로그 (각 시점마다 INSERT)
    // ============================================
    public void logCard(String cId, int tryNum) {
        log("INSERT INTO c_save (c_id, try, c_count) VALUES (?, ?, 1)", cId, tryNum);
    }

    public void logItem(String iId, int tryNum) {
        log("INSERT INTO i_save (i_id, try, i_count) VALUES (?, ?, 1)", iId, tryNum);
    }

    public void logNpc(String nId, int tryNum) {
        log("INSERT INTO n_save (n_id, try, n_count) VALUES (?, ?, 1)", nId, tryNum);
    }

    public void saveEnding(String eId, int tryNum) {
        log("INSERT INTO e_save (e_id, try, e_count) VALUES (?, ?, 1)", eId, tryNum);
    }

    private void log(String sql, String id, int tryNum) {
        if (tryNum < 0) return;
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, tryNum);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // 도감 목록 조회 (전체 콘텐츠 + 발견 여부)
    // ============================================
    public List<CollectionEntry> getCardCollection() {
        String sql = """
            SELECT c.c_id, c.c_name,
                   COALESCE(MIN(cs.try), -1) AS first_try,
                   COUNT(cs.c_id) AS total_count
            FROM card c
            LEFT JOIN c_save cs ON c.c_id = cs.c_id
            GROUP BY c.c_id, c.c_name
            ORDER BY CAST(SUBSTRING(c.c_id, 2) AS UNSIGNED)
            """;
        return queryEntries(sql);
    }

    public List<CollectionEntry> getItemCollection() {
        String sql = """
            SELECT i.i_id, i.i_name,
                   COALESCE(MIN(is2.try), -1) AS first_try,
                   COUNT(is2.i_id) AS total_count
            FROM item i
            LEFT JOIN i_save is2 ON i.i_id = is2.i_id
            GROUP BY i.i_id, i.i_name
            ORDER BY CAST(SUBSTRING(i.i_id, 2) AS UNSIGNED)
            """;
        return queryEntries(sql);
    }

    /** 보스만 조회 (is_boss = 1) */
    public List<CollectionEntry> getBossCollection() {
        String sql = """
            SELECT n.n_id, n.n_name,
                   COALESCE(MIN(ns.try), -1) AS first_try,
                   COUNT(ns.n_id) AS total_count
            FROM npc n
            LEFT JOIN n_save ns ON n.n_id = ns.n_id
            WHERE n.is_boss = 1
            GROUP BY n.n_id, n.n_name
            ORDER BY CAST(SUBSTRING(n.n_id, 2) AS UNSIGNED)
            """;
        return queryEntries(sql);
    }

    public List<CollectionEntry> getEndingCollection() {
        String sql = """
            SELECT e.e_id, e.e_name,
                   COALESCE(MIN(es.try), -1) AS first_try,
                   COUNT(es.e_id) AS total_count
            FROM ending e
            LEFT JOIN e_save es ON e.e_id = es.e_id
            GROUP BY e.e_id, e.e_name
            ORDER BY CAST(SUBSTRING(e.e_id, 2) AS UNSIGNED)
            """;
        return queryEntries(sql);
    }

    private List<CollectionEntry> queryEntries(String sql) {
        List<CollectionEntry> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return list;
        try (conn; PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CollectionEntry(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getInt(4)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================
    // 상세 조회 (name, desc, img) — img 없는 경우 null
    // ============================================
    /** @return String[]{name, desc, img} */
    public String[] getCardDetail(String cId) {
        return queryDetail("SELECT c_name, c_desc, c_img FROM card WHERE c_id = ?", cId, 3);
    }

    /** @return String[]{name, desc, img} */
    public String[] getItemDetail(String iId) {
        return queryDetail("SELECT i_name, i_desc, i_img FROM item WHERE i_id = ?", iId, 3);
    }

    /** @return String[]{name, desc} (NPC has no img column) */
    public String[] getNpcDetail(String nId) {
        return queryDetail("SELECT n_name, n_desc FROM npc WHERE n_id = ?", nId, 2);
    }

    /** @return String[]{name, desc, img} */
    public String[] getEndingDetail(String eId) {
        return queryDetail("SELECT e_name, e_desc, e_img FROM ending WHERE e_id = ?", eId, 3);
    }

    private String[] queryDetail(String sql, String id, int cols) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;
        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String[] result = new String[cols];
                    for (int i = 0; i < cols; i++) result[i] = rs.getString(i + 1);
                    return result;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================
    // 엔딩 이미지 (기존 호환)
    // ============================================
    public String findEImg(String eId) {
        String[] detail = getEndingDetail(eId);
        return (detail != null) ? detail[2] : null;
    }

    // ============================================
    // 진행률 헬퍼
    // ============================================
    public int getDiscoveredCount(List<CollectionEntry> entries) {
        int c = 0;
        for (CollectionEntry e : entries) if (e.isFound()) c++;
        return c;
    }
}
