package manager;

import model.Card;
import model.Item;
import model.Entity;
import view.GameView;

import java.util.List;

public class BattleManager {

    public enum BattleResult {
        WIN,
        LOSE,
        FLEE
    }

    private GameView gameView;

    public BattleManager(GameView gameView) {
        this.gameView = gameView;
    }

    // ============================================
    // 전투 시작
    // ============================================
    public BattleResult startBattle(Entity player, Entity enemy, String enemyName,
                                    List<Card> playerCards, List<Item> playerItems,
                                    Card enemyCard) {
        gameView.showBattleStart(enemyName);

        while (player.getCurrentHealth() > 0 && enemy.getCurrentHealth() > 0) {

            // --- 플레이어 턴 ---
            gameView.showBattleStatus(player, enemy, enemyName);
            int choice = gameView.showPlayerMenu(playerCards.size(), playerItems.size());

            switch (choice) {
                case 1: // 카드 사용 (공격)
                    int cardIdx = gameView.showCardList(playerCards);
                    if (cardIdx <= 0) continue;  // 취소 또는 빈 목록 → 다시 메뉴
                    Card selected = playerCards.get(cardIdx - 1);
                    int damage = selected.use(player, enemy);
                    gameView.showAttackResult("영균", selected.getCName(), damage);
                    break;

                case 2: // 아이템 사용
                    int itemIdx = gameView.showItemList(playerItems);
                    if (itemIdx <= 0) continue;
                    Item item = playerItems.remove(itemIdx - 1);
                    item.use(player);
                    gameView.showItemUseResult(item.getIName());
                    break;

                case 3: // 도망가기
                    gameView.showFlee();
                    return BattleResult.FLEE;
            }

            // 적 사망 체크
            if (enemy.getCurrentHealth() <= 0) {
                gameView.showBattleStatus(player, enemy, enemyName);
                gameView.showBattleWin(enemyName);
                return BattleResult.WIN;
            }

            // --- 적 턴 (HP바 갱신 후 공격 결과 표시) ---
            int enemyDamage = enemyCard.use(enemy, player);
            gameView.showBattleStatus(player, enemy, enemyName);
            gameView.showAttackResult(enemyName, enemyCard.getCName(), enemyDamage);

            // 플레이어 사망 체크
            if (player.getCurrentHealth() <= 0) {
                gameView.showBattleLose();
                return BattleResult.LOSE;
            }
        }

        return BattleResult.LOSE;
    }
}