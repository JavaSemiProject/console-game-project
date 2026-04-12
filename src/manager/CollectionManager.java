package manager;

import dao.CollectionDAO;
import model.CollectionEntry;
import view.GameView;

import java.util.List;

public class CollectionManager {

    private final CollectionDAO collectionDAO = new CollectionDAO();
    private final GameView gameView;

    public CollectionManager(GameView gameView) {
        this.gameView = gameView;
    }

    // ============================================
    // 도감 메뉴
    // ============================================
    public void showCollectionMenu() {
        while (true) {
            int menu = gameView.showCollectionMenu();
            switch (menu) {
                case 1: showCategory("카드 도감",  "CARD");   break;
                case 2: showCategory("아이템 도감", "ITEM");   break;
                case 3: showCategory("보스 도감",  "BOSS");   break;
                case 4: showCategory("엔딩 도감",  "ENDING"); break;
                case 5: return;
            }
        }
    }

    // ============================================
    // 카테고리 목록 → 상세 루프
    // ============================================
    private void showCategory(String title, String type) {
        while (true) {
            List<CollectionEntry> entries = loadEntries(type);
            int idx = gameView.showCollectionList(title, entries);
            if (idx < 0) return;  // 뒤로가기

            CollectionEntry selected = entries.get(idx);
            if (!selected.isFound()) {
                gameView.showCollectionLocked();
                continue;
            }

            String[] detail = loadDetail(type, selected.getId());
            if (detail == null) {
                gameView.showCollectionLocked();
                continue;
            }

            // detail = [name, desc] or [name, desc, img]
            String name = detail[0];
            String desc = detail[1];
            String img  = (detail.length > 2) ? detail[2] : null;
            gameView.showCollectionDetail(name, desc, img);
        }
    }

    // ============================================
    // 진행률 (메인 메뉴 표시용)
    // ============================================
    public int getDiscoveredCount() {
        int total = 0;
        for (String type : new String[]{"CARD", "ITEM", "BOSS", "ENDING"}) {
            for (CollectionEntry e : loadEntries(type)) {
                if (e.isFound()) total++;
            }
        }
        return total;
    }

    public int getTotalCount() {
        int total = 0;
        for (String type : new String[]{"CARD", "ITEM", "BOSS", "ENDING"}) {
            total += loadEntries(type).size();
        }
        return total;
    }

    // ============================================
    // 내부 헬퍼
    // ============================================
    private List<CollectionEntry> loadEntries(String type) {
        return switch (type) {
            case "CARD"   -> collectionDAO.getCardCollection();
            case "ITEM"   -> collectionDAO.getItemCollection();
            case "BOSS"   -> collectionDAO.getBossCollection();
            case "ENDING" -> collectionDAO.getEndingCollection();
            default       -> List.of();
        };
    }

    private String[] loadDetail(String type, String id) {
        return switch (type) {
            case "CARD"   -> collectionDAO.getCardDetail(id);
            case "ITEM"   -> collectionDAO.getItemDetail(id);
            case "BOSS"   -> collectionDAO.getNpcDetail(id);
            case "ENDING" -> collectionDAO.getEndingDetail(id);
            default       -> null;
        };
    }
}
