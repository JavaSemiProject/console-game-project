package manager;

import model.Card;
import model.Item;
import model.Ending;
import model.NPC;
import model.Collection;
import view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionManager {

    private List<Collection> collections = new ArrayList<>();

    private Map<Integer, Card> cardMap = new HashMap<>();
    private Map<Integer, Ending> endingMap = new HashMap<>();
    private Map<Integer, Item> itemMap = new HashMap<>();
    private Map<Integer, NPC> bossMap = new HashMap<>();

    private GameView gameView;

    public CollectionManager(GameView gameView) {
        this.gameView = gameView;
    }

    // ============================================
    // 데이터 등록 (DAO 로드 후 호출)
    // ============================================
    public void registerCards(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            cardMap.put(i + 1, cards.get(i));
        }
    }

    public void registerEndings(List<Ending> endings) {
        for (int i = 0; i < endings.size(); i++) {
            endingMap.put(i + 1, endings.get(i));
        }
    }

    public void registerItems(List<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            itemMap.put(i + 1, items.get(i));
        }
    }

    public void registerBosses(List<NPC> bosses) {
        for (int i = 0; i < bosses.size(); i++) {
            bossMap.put(i + 1, bosses.get(i));
        }
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    // ============================================
    // 해금
    // ============================================
    public void unlock(String type, int contentId, int currentTry) {
        for (Collection c : collections) {
            if (c.getCollectionType().equals(type) && c.getContentId() == contentId) {
                if (!c.isUnlocked()) {
                    c.unlock(currentTry);
                }
                return;
            }
        }
    }

    public void unlockCard(int contentId, int currentTry) {
        unlock("CARD", contentId, currentTry);
    }

    public void unlockEnding(int contentId, int currentTry) {
        unlock("ENDING", contentId, currentTry);
    }

    public void unlockItem(int contentId, int currentTry) {
        unlock("ITEM", contentId, currentTry);
    }

    public void unlockBoss(int contentId, int currentTry) {
        unlock("BOSS", contentId, currentTry);
    }

    // ============================================
    // 도감 출력
    // ============================================
    public void printCardCollection() {
        gameView.showCollectionHeader("카드 도감");
        for (Collection c : collections) {
            if (!"CARD".equals(c.getCollectionType())) continue;
            Card card = cardMap.get(c.getContentId());
            if (card == null) continue;

            String detail = "ATK:" + card.getCPower() + " - " + card.getCDesc()
                    + " (해금: " + c.getFirstTry() + "트라이)";
            gameView.showCollectionEntry(card.getCName(), detail, c.isUnlocked());
        }
    }

    public void printEndingCollection() {
        gameView.showCollectionHeader("엔딩 도감");
        for (Collection c : collections) {
            if (!"ENDING".equals(c.getCollectionType())) continue;
            Ending ending = endingMap.get(c.getContentId());
            if (ending == null) continue;

            String detail = ending.getEDesc() + " (해금: " + c.getFirstTry() + "트라이)";
            gameView.showCollectionEntry(ending.getEName(), detail, c.isUnlocked());
        }
    }

    public void printItemCollection() {
        gameView.showCollectionHeader("아이템 도감");
        for (Collection c : collections) {
            if (!"ITEM".equals(c.getCollectionType())) continue;
            Item item = itemMap.get(c.getContentId());
            if (item == null) continue;

            String detail = item.getIDesc() + " (해금: " + c.getFirstTry() + "트라이)";
            gameView.showCollectionEntry(item.getIName(), detail, c.isUnlocked());
        }
    }

    public void printBossCollection() {
        gameView.showCollectionHeader("보스 도감");
        for (Collection c : collections) {
            if (!"BOSS".equals(c.getCollectionType())) continue;
            NPC boss = bossMap.get(c.getContentId());
            if (boss == null) continue;

            String detail = boss.getNDesc() + " (공략: " + c.getFirstTry() + "트라이)";
            gameView.showCollectionEntry(boss.getNName(), detail, c.isUnlocked());
        }
    }

    // ============================================
    // 도감 메뉴 (View 연동)
    // ============================================
    public void showCollectionMenu() {
        while (true) {
            int menu = gameView.showCollectionMenu();
            switch (menu) {
                case 1: gameView.clearScreen(); printCardCollection(); break;
                case 2: gameView.clearScreen(); printItemCollection(); break;
                case 3: gameView.clearScreen(); printBossCollection(); break;
                case 4: gameView.clearScreen(); printEndingCollection(); break;
                case 5: return;
            }
            gameView.showProgress(getUnlockedCount(), getTotalCount());
            gameView.waitForEnter();
        }
    }

    // ============================================
    // 진행률 조회
    // ============================================
    public int getUnlockedCount() {
        int count = 0;
        for (Collection c : collections) {
            if (c.isUnlocked()) count++;
        }
        return count;
    }

    public int getTotalCount() {
        return collections.size();
    }
}