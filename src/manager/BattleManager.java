package manager;

import model.Card;
import model.Item;
import model.Entity;
import view.GameView;

import java.util.List;
import java.util.stream.Collectors;

public class BattleManager {

    public enum BattleResult {
        WIN,
        LOSE,
        FLEE,
        SCANNER_CAPTURE,  // 혜진이 Scanner에 빨려들어감
        ENEMY_FLED        // 적이 도망감 (캐시 전투)
    }

    private GameView gameView;

    public BattleManager(GameView gameView) {
        this.gameView = gameView;
    }

    /** 전투에서 사용 가능한 카드만 필터링 */
    private List<Card> combatCards(List<Card> cards) {
        return cards.stream()
                .filter(Card::isCombatUsable)
                .collect(Collectors.toList());
    }

    // ============================================
    // 혜진 전투 (Scanner = 방어 카드, 3회 사용 시 흡수 승리)
    // ============================================
    public BattleResult startHyejinBattle(Entity player, Entity enemy, String enemyName,
                                          List<Card> playerCards, List<Item> playerItems,
                                          Card enemyCard) {
        gameView.showBattleStart(enemyName, enemy.getPp());
        int scannerUseCount = 0;

        while (player.getCurrentHealth() > 0 && enemy.getCurrentHealth() > 0) {

            // --- 플레이어 턴 ---
            gameView.showBattleStatus(player, enemy, enemyName);
            List<Card> usableCards = combatCards(playerCards);
            int choice = gameView.showPlayerMenu(usableCards.size(), playerItems.size());

            boolean usedScanner = false;

            switch (choice) {
                case 1: // 카드 사용
                    int cardIdx = gameView.showCardList(usableCards);
                    if (cardIdx <= 0) continue;
                    Card selected = usableCards.get(cardIdx - 1);

                    if ("C001".equals(selected.getCId())) {
                        // Scanner → 방어 모드: 공격하지 않고 이번 턴 적 공격을 막음
                        scannerUseCount++;
                        gameView.showScannerBlock(scannerUseCount);
                        usedScanner = true;

                        if (scannerUseCount >= 3) {
                            gameView.showScannerCapture();
                            return BattleResult.SCANNER_CAPTURE;
                        }
                    } else {
                        // 다른 카드는 일반 공격
                        int damage = selected.use(player, enemy);
                        gameView.showAttackResult("영균", player.getPp(), selected, enemyName, enemy.getPp(), damage);
                    }
                    break;

                case 2: // 아이템 사용
                    int itemIdx = gameView.showItemList(playerItems);
                    if (itemIdx <= 0) continue;
                    Item item = playerItems.remove(itemIdx - 1);
                    item.use(player);
                    gameView.showItemUseResult(item);
                    break;

                case 3: // 도망가기
                    gameView.showFlee();
                    return BattleResult.FLEE;
            }

            // 적 사망 체크
            if (enemy.getCurrentHealth() <= 0) {
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showBattleWin(enemyName, enemy.getPp());
                return BattleResult.WIN;
            }

            // --- 적 턴 ---
            if (usedScanner) {
                // Scanner로 방어 중이면 적 공격을 막음
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showBlockedAttack(enemyName);
            } else {
                int enemyDamage = enemyCard.use(enemy, player);
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showAttackResult(enemyName, enemy.getPp(), enemyCard, "영균", player.getPp(), enemyDamage);
            }

            // 플레이어 사망 체크
            if (player.getCurrentHealth() <= 0) {
                gameView.showBattleLose();
                return BattleResult.LOSE;
            }
        }

        return BattleResult.LOSE;
    }

    // ============================================
    // 캐시 전투 (적이 90% 확률로 도망)
    // ============================================
    public BattleResult startCacheBattle(Entity player, Entity enemy, String enemyName,
                                         List<Card> playerCards, List<Item> playerItems,
                                         Card enemyCard) {
        gameView.showBattleStart(enemyName, enemy.getPp());

        while (player.getCurrentHealth() > 0 && enemy.getCurrentHealth() > 0) {

            // --- 플레이어 턴 ---
            gameView.showBattleStatus(player, enemy, enemyName);
            List<Card> usableCards = combatCards(playerCards);
            int choice = gameView.showPlayerMenu(usableCards.size(), playerItems.size());

            switch (choice) {
                case 1:
                    int cardIdx = gameView.showCardList(usableCards);
                    if (cardIdx <= 0) continue;
                    Card selected = usableCards.get(cardIdx - 1);
                    int damage = selected.use(player, enemy);
                    gameView.showAttackResult("영균", player.getPp(), selected, enemyName, enemy.getPp(), damage);
                    break;

                case 2:
                    int itemIdx = gameView.showItemList(playerItems);
                    if (itemIdx <= 0) continue;
                    Item item = playerItems.remove(itemIdx - 1);
                    item.use(player);
                    gameView.showItemUseResult(item);
                    break;

                case 3:
                    gameView.showFlee();
                    return BattleResult.FLEE;
            }

            // 적 사망 체크
            if (enemy.getCurrentHealth() <= 0) {
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showBattleWin(enemyName, enemy.getPp());
                return BattleResult.WIN;
            }

            // --- 적 턴: 90% 확률로 도망 ---
            if (Math.random() < 0.9) {
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showEnemyFled(enemyName, enemy.getPp());
                return BattleResult.ENEMY_FLED;
            }

            int enemyDamage = enemyCard.use(enemy, player);
            gameView.showBattleStatus(player, enemy, enemyName);
            gameView.showAttackResult(enemyName, enemy.getPp(), enemyCard, "영균", player.getPp(), enemyDamage);

            // 플레이어 사망 체크
            if (player.getCurrentHealth() <= 0) {
                gameView.showBattleLose();
                return BattleResult.LOSE;
            }
        }

        return BattleResult.LOSE;
    }

    // ============================================
    // 전투 시작
    // ============================================
    public BattleResult startBattle(Entity player, Entity enemy, String enemyName,
                                    List<Card> playerCards, List<Item> playerItems,
                                    Card enemyCard) {
        gameView.showBattleStart(enemyName, enemy.getPp());

        while (player.getCurrentHealth() > 0 && enemy.getCurrentHealth() > 0) {

            // --- 플레이어 턴 ---
            gameView.showBattleStatus(player, enemy, enemyName);
            List<Card> usableCards = combatCards(playerCards);
            int choice = gameView.showPlayerMenu(usableCards.size(), playerItems.size());

            switch (choice) {
                case 1: // 카드 사용 (공격)
                    int cardIdx = gameView.showCardList(usableCards);
                    if (cardIdx <= 0) continue;  // 취소 또는 빈 목록 → 다시 메뉴
                    Card selected = usableCards.get(cardIdx - 1);
                    int damage = selected.use(player, enemy);
                    gameView.showAttackResult("영균", player.getPp(), selected, enemyName, enemy.getPp(), damage);
                    break;

                case 2: // 아이템 사용
                    int itemIdx = gameView.showItemList(playerItems);
                    if (itemIdx <= 0) continue;
                    Item item = playerItems.remove(itemIdx - 1);
                    item.use(player);
                    gameView.showItemUseResult(item);
                    break;

                case 3: // 도망가기
                    gameView.showFlee();
                    return BattleResult.FLEE;
            }

            // 적 사망 체크
            if (enemy.getCurrentHealth() <= 0) {
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showBattleWin(enemyName, enemy.getPp());
                return BattleResult.WIN;
            }

            // --- 적 턴 (HP바 갱신 후 공격 결과 표시) ---
            int enemyDamage = enemyCard.use(enemy, player);
            gameView.showBattleStatus(player, enemy, enemyName);
            gameView.showAttackResult(enemyName, enemy.getPp(), enemyCard, "영균", player.getPp(), enemyDamage);

            // 플레이어 사망 체크
            if (player.getCurrentHealth() <= 0) {
                gameView.showBattleLose();
                return BattleResult.LOSE;
            }
        }

        return BattleResult.LOSE;
    }
}