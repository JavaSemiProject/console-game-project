package manager;

import model.Card;
import model.Item;
import model.Entity;

import java.util.List;
import java.util.Scanner;

public class BattleManager {

    // ============================================
    // 전투 결과
    // ============================================
    public enum BattleResult {
        WIN,
        LOSE,
        FLEE
    }

    private Scanner scanner;

    public BattleManager(Scanner scanner) {
        this.scanner = scanner;
    }

    // ============================================
    // 전투 시작
    // ============================================
    public BattleResult startBattle(Entity player, Entity enemy,
                                    List<Card> playerCards, List<Item> playerItems,
                                    Card enemyCard) {
        // TODO: View로 출력 위임
        System.out.println("=== 전투 시작! ===");

        while (player.getCurrentHealth() > 0 && enemy.getCurrentHealth() > 0) {

            // --- 플레이어 턴 ---
            printStatus(player, enemy);
            int choice = showPlayerMenu(playerCards, playerItems);

            switch (choice) {
                case 1: // 카드 사용 (공격)
                    Card selected = selectCard(playerCards);
                    if (selected == null) continue;  // 선택 취소 → 다시 메뉴
                    int damage = selected.use(player, enemy);
                    // TODO: View - 공격 연출
                    break;

                case 2: // 아이템 사용
                    Item item = selectItem(playerItems);
                    if (item == null) continue;
                    int effect = item.use(player);
                    // TODO: View - 아이템 사용 연출
                    break;

                case 3: // 도망가기
                    return BattleResult.FLEE;
            }

            // 적 사망 체크
            if (enemy.getCurrentHealth() <= 0) {
                // TODO: View - 승리 연출
                System.out.println("=== 전투 승리! ===");
                return BattleResult.WIN;
            }

            // --- 적 턴 ---
            enemyTurn(enemy, enemyCard, player);

            // 플레이어 사망 체크
            if (player.getCurrentHealth() <= 0) {
                // TODO: View - 패배 연출
                System.out.println("=== 전투 패배... ===");
                return BattleResult.LOSE;
            }
        }

        return BattleResult.LOSE;
    }

    // ============================================
    // 적 턴: 보유 카드로 공격
    // ============================================
    private void enemyTurn(Entity enemy, Card enemyCard, Entity player) {
        int damage = enemyCard.use(enemy, player);
        // TODO: View - 적 카드 공격 연출
    }

    // ============================================
    // 플레이어 메뉴 출력
    // ============================================
    private int showPlayerMenu(List<Card> cards, List<Item> items) {
        // TODO: View로 출력 위임
        System.out.println("\n행동을 선택하세요:");
        System.out.println("1. 카드 사용 (" + cards.size() + "장)");
        System.out.println("2. 아이템 사용 (" + items.size() + "개)");
        System.out.println("3. 도망가기");

        while (true) {
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": return 1;
                case "2": return 2;
                case "3": return 3;
                default:
                    System.out.println("1~3 중에 선택해주세요.");
            }
        }
    }

    // ============================================
    // 카드 선택
    // ============================================
    private Card selectCard(List<Card> cards) {
        if (cards.isEmpty()) {
            System.out.println("보유한 카드가 없습니다!");
            return null;
        }

        // TODO: View로 출력 위임
        System.out.println("\n사용할 카드를 선택하세요 (0: 취소):");
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            System.out.println((i + 1) + ". " + c.getCName() + " (ATK:" + c.getCPower() + ")");
        }

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx == 0) return null;
                if (idx >= 1 && idx <= cards.size()) {
                    return cards.get(idx - 1);
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("올바른 번호를 입력해주세요.");
        }
    }

    // ============================================
    // 아이템 선택
    // ============================================
    private Item selectItem(List<Item> items) {
        if (items.isEmpty()) {
            System.out.println("보유한 아이템이 없습니다!");
            return null;
        }

        // TODO: View로 출력 위임
        System.out.println("\n사용할 아이템을 선택하세요 (0: 취소):");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            System.out.println((i + 1) + ". " + it.getIName());
        }

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx == 0) return null;
                if (idx >= 1 && idx <= items.size()) {
                    Item used = items.remove(idx - 1);  // 사용 후 제거
                    return used;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("올바른 번호를 입력해주세요.");
        }
    }

    // ============================================
    // 상태 출력
    // ============================================
    private void printStatus(Entity player, Entity enemy) {
        // TODO: View로 출력 위임
        System.out.println("\n[나] HP: " + player.getCurrentHealth() + "/" + player.getHealth());
        System.out.println("[적] HP: " + enemy.getCurrentHealth() + "/" + enemy.getHealth());
    }
}
