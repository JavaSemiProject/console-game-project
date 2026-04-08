package manager;

import model.Card;
import model.Item;
import model.Ending;
import model.NPC;
import model.Collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionManager {

    // 해금 상태 목록
    private List<Collection> collections = new ArrayList<>();

    // 실제 객체 맵 (contentId → 객체)
    private Map<Integer, Card> cardMap = new HashMap<>();
    private Map<Integer, Ending> endingMap = new HashMap<>();
    private Map<Integer, Item> itemMap = new HashMap<>();
    private Map<Integer, NPC> bossMap = new HashMap<>();

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
    // 도감 출력: 카드
    // ============================================
    public void printCardCollection() {
        // TODO: View로 출력 위임
        System.out.println("=== 카드 도감 ===");
        for (Collection c : collections) {
            if (!"CARD".equals(c.getCollectionType())) continue;
            Card card = cardMap.get(c.getContentId());
            if (card == null) continue;

            if (c.isUnlocked()) {
                System.out.println("[" + card.getCName() + "] ATK:" + card.getCPower() +
                        " - " + card.getCDesc() +
                        " (해금: " + c.getFirstTry() + "트라이)");
            } else {
                System.out.println("[???] 미해금");
            }
        }
    }

    // ============================================
    // 도감 출력: 엔딩
    // ============================================
    public void printEndingCollection() {
        // TODO: View로 출력 위임
        System.out.println("=== 엔딩 도감 ===");
        for (Collection c : collections) {
            if (!"ENDING".equals(c.getCollectionType())) continue;
            Ending ending = endingMap.get(c.getContentId());
            if (ending == null) continue;

            if (c.isUnlocked()) {
                System.out.println("[" + ending.getEName() + "] " + ending.getEDesc() +
                        " (해금: " + c.getFirstTry() + "트라이)");
            } else {
                System.out.println("[???] 미해금");
            }
        }
    }

    // ============================================
    // 도감 출력: 아이템
    // ============================================
    public void printItemCollection() {
        // TODO: View로 출력 위임
        System.out.println("=== 아이템 도감 ===");
        for (Collection c : collections) {
            if (!"ITEM".equals(c.getCollectionType())) continue;
            Item item = itemMap.get(c.getContentId());
            if (item == null) continue;

            if (c.isUnlocked()) {
                System.out.println("[" + item.getIName() + "] " + item.getIDesc() +
                        " (해금: " + c.getFirstTry() + "트라이)");
            } else {
                System.out.println("[???] 미해금");
            }
        }
    }

    // ============================================
    // 도감 출력: 보스 (공략 여부)
    // ============================================
    public void printBossCollection() {
        // TODO: View로 출력 위임
        System.out.println("=== 보스 도감 ===");
        for (Collection c : collections) {
            if (!"BOSS".equals(c.getCollectionType())) continue;
            NPC boss = bossMap.get(c.getContentId());
            if (boss == null) continue;

            if (c.isUnlocked()) {
                System.out.println("[" + boss.getNName() + "] " + boss.getNDesc() +
                        " (공략: " + c.getFirstTry() + "트라이)");
            } else {
                System.out.println("[???] 미공략");
            }
        }
    }

    // ============================================
    // 전체 도감 출력
    // ============================================
    public void printAll() {
        printCardCollection();
        System.out.println();
        printItemCollection();
        System.out.println();
        printBossCollection();
        System.out.println();
        printEndingCollection();
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

    public String getProgressText() {
        return getUnlockedCount() + " / " + getTotalCount();
    }
}