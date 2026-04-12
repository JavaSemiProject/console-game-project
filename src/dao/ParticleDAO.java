package dao;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ParticleDAO {

    /** pp → {sound_lg, sound_er, sound_en} 캐시 */
    private static final Map<Integer, String[]> cache = new HashMap<>();

    /**
     * pp 값에 해당하는 조사 배열을 반환한다.
     * @return String[3]: [0]=sound_lg(이/가), [1]=sound_er(을/를), [2]=sound_en(은/는)
     *         조회 실패 시 pp==0 기본값 반환
     */
    public static String[] find(int pp) {
        if (cache.containsKey(pp)) return cache.get(pp);

        String sql = "SELECT sound_lg, sound_er, sound_en FROM particle WHERE pp = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String[] particles = {
                    rs.getString("sound_lg"),
                    rs.getString("sound_er"),
                    rs.getString("sound_en")
                };
                cache.put(pp, particles);
                return particles;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 조회 실패 시 기본값 (pp=0: 이/을/은)
        String[] fallback = (pp == 0) ? new String[]{"이", "을", "은"} : new String[]{"가", "를", "는"};
        cache.put(pp, fallback);
        return fallback;
    }

    /** 주격 조사 (이/가) */
    public static String lg(int pp) { return find(pp)[0]; }

    /** 목적격 조사 (을/를) */
    public static String er(int pp) { return find(pp)[1]; }

    /** 보조사 (은/는) */
    public static String en(int pp) { return find(pp)[2]; }
}