package manager;

import dao.CardDAO;
import dao.ItemDAO;
import dao.SaveDAO;
import dao.StageDAO;
import manager.BattleManager.BattleResult;
import manager.EventManager.EventResult;
import manager.StageManager.ExploreResult;
import model.*;
import view.GameView;
import view.MainMenuView;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameManager {

    public enum GameState {
        MAIN_MENU,
        PROLOGUE,
        TUTORIAL,       // 1층 설산 (혜진, 선혁)
        FLOOR_2,        // 2층 숲   (미주)
        FLOOR_3,        // 3층 들판 (솔민)
        FLOOR_4,        // 4층 사막 (제석)
        FLOOR_5,        // 5층 강물 (수지)
        FLOOR_6,        // 6층 심해 (봉민)
        FLOOR_7,        // 7층 화산 (민중)
        HIDDEN_MAP,     // 히든맵 - 힙 영역 (쓰레기촌)
        FINAL_FLOOR,    // 8층 딸깍이
        ENDING,
        GAME_OVER
    }

    private GameState state;
    private String endingType;

    // 게임 플래그
    private boolean hasSemicolon;
    private boolean hasBracket;
    private boolean hyejinRoute;

    // 플레이어
    private Hero hero;
    private List<Card> playerCards = new ArrayList<>();
    private List<Item> playerItems = new ArrayList<>();

    // 매니저
    private Scanner scanner;
    private BattleManager battleManager;
    private EventManager eventManager;
    private StoryManager storyManager;
    private CollectionManager collectionManager;
    private StageManager stageManager;
    private SaveManager saveManager;
    // 뷰
    private GameView gameView;
    private MainMenuView menuView;

    public GameManager(Scanner scanner, GameView gameView, MainMenuView menuView) {
        this.scanner = scanner;
        this.gameView = gameView;
        this.menuView = menuView;

        this.battleManager = new BattleManager(gameView);
        this.eventManager = new EventManager();
        this.storyManager = new StoryManager();
        this.collectionManager = new CollectionManager(gameView);
        this.stageManager = new StageManager();
        this.saveManager = new SaveManager(stageManager, new StageDAO(), new SaveDAO());
        this.state = GameState.MAIN_MENU;

    }

    // ============================================
    // 게임 데이터 초기화 (하드코딩)
    // ============================================
    private void initGameData() {
        initGameData(true);  // 새 게임은 세이브 생성
    }

    private void initGameData(boolean createSave) {
        hero = new Hero("H001", 100, 5, 10, 0, "C001");

        playerCards.clear();
        playerCards.add(Card.createAttackCard("C001", 1, "Scanner", "SCAN_INPUT", 0,
                "사용자 입력을 읽어들이는 기본 도구.", null, null, 0));

        playerItems.clear();
        for (int i = 0; i < 3; i++) {
            playerItems.add(createPotion());
        }

        hasSemicolon = false;
        hasBracket = false;
        hyejinRoute = false;
        stageManager.buildFloorChain(8);

        if (createSave) {   // ← 이어하기 시엔 실행 안 됨
            Save newSave = saveManager.createNewSave(2);
            if (newSave != null) {
                stageManager.setCurrentTryNum(newSave.getTryNum());
                System.out.println("[GameManager] tryNum 설정: " + newSave.getTryNum());
                System.out.println("[GameManager] 새 게임 세이브 생성: " + newSave.getSaveStatus());
            } else {
                System.out.println("[GameManager] 세이브 생성 실패 - tryNum 미설정");
            }
        }
    }

    // ============================================
    // 헬퍼: 오브젝트 생성
    // ============================================
    private NPC createBoss(String id, String name, int hp, int atkMin, int atkMax) {
        return new NPC(id, name, name + " 보스", hp, true, atkMin, atkMax, 1, null);
    }

    private Card createEnemyCard(String name, int power) {
        return Card.createAttackCard("E_" + name, 0, name + "의 공격",
                "ENEMY_ATK", power, name + "의 공격", null, null, 0);
    }

    private Item createPotion() {
        return Item.createItem("I001", "포션", 30, 0, 0,
                "HP를 30 회복하는 기본 포션.", null, null, 0, 0);
    }

    private void awardCard(String id, int pp, String name, String method, int power, String desc) {
        playerCards.add(Card.createAttackCard(id, pp, name, method, power, desc, null, null, 0));
        gameView.showAcquire(name);
    }

    /**
     * 랜덤 NPC 조우 처리. 승리 시 회복, 패배 시 GAME_OVER 설정.
     * @return 전투 결과
     */
    private BattleResult handleNpcEncounter(ExploreResult result) {
        String npcId = result.getEventId();
        NPC npc = stageManager.getNpcById(npcId);
        if (npc == null) return BattleResult.WIN;

        Card npcCard = createEnemyCard(npc.getNName(), npc.getPowerMax());
        BattleResult br = battleManager.startBattle(
                hero, npc, npc.getNName(), playerCards, playerItems, npcCard);

        if (br == BattleResult.WIN) {
            restAfterBattle();
            if (npc.getCId() != null) {
                Card drop = new CardDAO().findById(npc.getCId());
                if (drop != null) { playerCards.add(drop); gameView.showAcquire(drop.getCName()); }
            }
            if (npc.getIId() != null) {
                Item dropItem = new ItemDAO().findById(npc.getIId());
                if (dropItem != null) { playerItems.add(dropItem); gameView.showAcquire(dropItem.getIName()); }
            }
        } else if (br == BattleResult.LOSE) {
            state = GameState.GAME_OVER;
        }
        return br;
    }

    private void restAfterBattle() {
        hero.heal(20);
        playerItems.add(createPotion());
    }

    /**
     * 보스전 루프. 도망 시 맵 탐색으로 복귀 후 재도전.
     * @return true=승리, false=패배(GAME_OVER 설정됨)
     */
    private boolean bossBattleLoop(int floorLevel, String prefix, String startTag, String winTag,
                                   String bossId, String bossName, int hp, int atkMin, int atkMax, int cardPower,
                                   model.Stage lastPos) {

        while (true) {
            showDialogue(prefix, startTag);
            NPC boss = createBoss(bossId, bossName, hp, atkMin, atkMax);
            Card bossCard = createEnemyCard(bossName, cardPower);
            BattleResult result = battleManager.startBattle(
                    hero, boss, bossName, playerCards, playerItems, bossCard);

            if (result == BattleResult.WIN) {
                showDialogue(prefix, winTag);
                return true;
            }
            if (result == BattleResult.LOSE) {
                state = GameState.GAME_OVER;
                return false;
            }
            // FLEE → 직전 위치로 복귀 후 다시 출구 찾기
            gameView.showMessage("도망쳤다... 다시 출구를 찾아야 한다.");
            gameView.waitForEnter();
            ExploreResult fleeResult = stageManager.exploreFloor(floorLevel, gameView, lastPos, playerCards, playerItems);
            if (fleeResult != null) lastPos = fleeResult.getLastPos();
        }
    }

    // ============================================
    // 메인 게임 루프
    // ============================================
    public void run() {
        storyManager.loadAll("resource/story");

        while (state != GameState.GAME_OVER) {
            eventManager.updateState(hasSemicolon, hasBracket);

            switch (state) {
                case MAIN_MENU:   showMainMenu();   break;
                case PROLOGUE:    playPrologue();    break;
                case TUTORIAL:    playTutorial();    break;
                case FLOOR_2:     playFloor2();      break;
                case FLOOR_3:     playFloor3();      break;
                case FLOOR_4:     playFloor4();      break;
                case FLOOR_5:     playFloor5();      break;
                case FLOOR_6:     playFloor6();      break;
                case FLOOR_7:     playFloor7();      break;
                case HIDDEN_MAP:  playHiddenMap();   break;
                case FINAL_FLOOR: playFinalFloor();  break;
                case ENDING:      playEnding();      break;
            }
        }
        scanner.close();
    }

    // ============================================
    // 메인 메뉴
    // ============================================
    private void showMainMenu() {
        int choice = menuView.showMainMenu();
        switch (choice) {
            case 1:
                initGameData();
                state = GameState.PROLOGUE;
                break;
            case 2:
                /* TODO: SaveManager.load() */
                // 이어하기: loadLatestSave()를 먼저 호출한 뒤 initGameData(false)로 세이브 생성 억제
                Save loaded = saveManager.loadLatestSave();
                if (loaded != null) {
                    initGameData(false);  // 세이브 생성 없이 초기화만
                    stageManager.setCurrentTryNum(loaded.getTryNum());
                    GameState savedState = getStateFromSaveId(loaded.getLId());
                    gameView.showMessage("[이어하기] " + loaded.getSaveStatus());
                    gameView.waitForEnter();
                    state = savedState;
                } else {
                    menuView.showNoSaveData();
                }
                break;
            case 3: collectionManager.showCollectionMenu(); break;
            case 4: state = GameState.GAME_OVER; break;
        }
    }

    // s_id로 해당 층 GameState 반환
    private GameState getStateFromSaveId(String sId) {
        if (sId == null) return GameState.PROLOGUE;

        // s_id 앞자리 숫자가 층 번호 (예: "2_a1" → 2층)
        try {
            int floorLevel = Integer.parseInt(sId.split("_")[0]);
            switch (floorLevel) {
                case 2: return GameState.FLOOR_2;
                case 3: return GameState.FLOOR_3;
                case 4: return GameState.FLOOR_4;
                case 5: return GameState.FLOOR_5;
                case 6: return GameState.FLOOR_6;
                case 7: return GameState.FLOOR_7;
                default: return GameState.PROLOGUE;
            }
        } catch (Exception e) {
            return GameState.PROLOGUE;
        }
    }

    // ============================================
    // 프롤로그
    // ============================================
    private void playPrologue() {
        showDialogue("prologue", "story");
        state = GameState.TUTORIAL;
    }

    // ============================================
    // 1층: 설산 (튜토리얼)
    // ============================================
    private void playTutorial() {
        showDialogue("floor1", "story");

        // 혜진 전투
        NPC hyejin = new NPC("N001", "혜진",
                "같이 코드 세계에 갇힌 동료. Math.random 카드를 가지고 있다.",
                50, false, 5, 8, 1, "C002");
        Card hyejinCard = createEnemyCard("혜진", 12);

        BattleResult result = battleManager.startHyejinBattle(
                hero, hyejin, "혜진", playerCards, playerItems, hyejinCard);

        if (result == BattleResult.FLEE) {
            showDialogue("floor1", "ending_fall");
            triggerEnding("FALL");
            return;
        }
        if (result == BattleResult.LOSE) {
            state = GameState.GAME_OVER;
            return;
        }

        // 승리 또는 Scanner 흡수: 혜진 사라짐 + Math.random 카드 획득
        showDialogue("floor1", "hyejin_disappear");
        playerCards.removeIf(c -> "C001".equals(c.getCId())); // Scanner 비활성화
        awardCard("C002", 0, "Math.random", "RANDOM_ATK", 25,
                "랜덤 주사위를 굴려 공격한다.");
        hero.heal(20);

        state = GameState.FLOOR_2;
    }

    // ============================================
    // 2층: 숲 (미주)
    // ============================================
    private void playFloor2() {
        showDialogue("floor2", "story");

        model.Stage resumePos = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(2, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.COMMENT_BRANCH.equals(eid)) {
                showDialogue("floor2", "comment_branch");
                eventManager.setCommentBranchDone(true);
                result.getCurrentPos().consume();
            } else if (EventManager.SEMICOLON_FIND.equals(eid)) {
                showDialogue("floor2", "semicolon_find");
                hasSemicolon = true;
                Card semicolonCard = new CardDAO().findById("c3");
                if (semicolonCard != null) {
                    semicolonCard.setCombatUsable(false);
                    playerCards.add(semicolonCard);
                }
                gameView.showAcquire("; (세미콜론)");
                result.getCurrentPos().consume();
            }
            resumePos = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos;

        // 미주 보스전
        if (!bossBattleLoop(2, "floor2", "battle_boss_start", "battle_boss_win",
                "B002", "미주", 70, 8, 12, 15, lastPos)) return;

        awardCard("C004", 0, "Arrays.sort", "SORT_SLASH", 30,
                "배열을 정렬하며 적을 베어낸다.");
        restAfterBattle();

        state = GameState.FLOOR_3;
    }

    // ============================================
    // 3층: 들판 (솔민)
    // ============================================
    private void playFloor3() {
        showDialogue("floor3", "story");

        model.Stage resumePos3 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(3, gameView, resumePos3, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos3 = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.SEMICOLON_DOOR.equals(eid)) {
                EventResult doorResult = eventManager.trigger(EventManager.SEMICOLON_DOOR);
                if (doorResult == EventResult.ENDING_SHORTCUT) {
                    showDialogue("floor3", "semicolon_door_open");
                    triggerEnding("SHORTCUT");
                    return;
                } else if (doorResult == EventResult.DOOR_LOCKED) {
                    showDialogue("floor3", "semicolon_door_locked");
                }
                result.getCurrentPos().consume();
            }
            resumePos3 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos3;

        // 솔민 보스전
        if (!bossBattleLoop(3, "floor3", "battle_boss_start", "battle_boss_win",
                "B003", "솔민", 85, 10, 14, 18, lastPos)) return;

        awardCard("C005", 0, "String.split", "SPLIT_CUT", 20,
                "문자열을 분할하며 적을 가른다.");
        restAfterBattle();

        state = GameState.FLOOR_4;
    }

    // ============================================
    // 4층: 사막 (제석)
    // ============================================
    private void playFloor4() {
        showDialogue("floor4", "story");

        model.Stage resumePos = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(4, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.SUSPECT_SUNHYUK.equals(eid)) {
                EventResult suspectResult = eventManager.trigger(eid);
                if (suspectResult == EventResult.START_BATTLE) {
                    showDialogue("floor4", "suspect_sunhyuk");

                    NPC sunhyuk = new NPC("N002", "선혁",
                            "같이 코드 세계에 갇힌 동료.", 60, false, 8, 12, 0, null);
                    Card sunhyukCard = createEnemyCard("선혁", 14);
                    BattleResult pvpResult = battleManager.startBattle(
                            hero, sunhyuk, "선혁", playerCards, playerItems, sunhyukCard);

                    if (pvpResult == BattleResult.LOSE) {
                        showDialogue("floor4", "betrayal_lose");
                        triggerEnding("BETRAYAL");
                        return;
                    }
                    showDialogue("floor4", "betrayal_win");
                }
                result.getCurrentPos().consume();
            }
            resumePos = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos;

        // 제석 보스전
        if (!bossBattleLoop(4, "floor4", "battle_boss_start", "battle_boss_win",
                "B004", "제석", 100, 12, 16, 20, lastPos)) return;

        awardCard("C006", 1, "try-catch", "CATCH_HEAL", 35,
                "예외를 잡아 안정을 되찾는다.");
        restAfterBattle();

        state = GameState.FLOOR_5;
    }

    // ============================================
    // 5층: 강물 (수지)
    // ============================================
    private void playFloor5() {
        showDialogue("floor5", "story");

        model.Stage resumePos5 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(5, gameView, resumePos5, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos5 = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }
            resumePos5 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos5;

        // 수지 보스전
        if (!bossBattleLoop(5, "floor5", "battle_boss_start", "battle_boss_win",
                "B005", "수지", 110, 14, 18, 22, lastPos)) return;

        awardCard("C009", 1, "StringBuilder", "BUILD_STRIKE", 35,
                "문자열을 조합해 강력한 일격을 날린다.");
        restAfterBattle();

        state = GameState.FLOOR_6;
    }

    // ============================================
    // 6층: 심해 (봉민)
    // ============================================
    private void playFloor6() {
        showDialogue("floor6", "story");

        model.Stage resumePos6 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(6, gameView, resumePos6, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos6 = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.CACHE_BATTLE.equals(eid)) {
                // TODO: 캐시 전투 처리
                result.getCurrentPos().consume();
            }
            resumePos6 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos6;

        // 봉민 보스전
        if (!bossBattleLoop(6, "floor6", "battle_boss_start", "battle_boss_win",
                "B006", "봉민", 130, 16, 20, 25, lastPos)) return;

        awardCard("C007", 0, "Collections", "COLLECTION_POWER", 40,
                "컬렉션 프레임워크의 강력한 힘.");
        restAfterBattle();

        state = GameState.FLOOR_7;
    }

    // ============================================
    // 7층: 화산 (민중)
    // ============================================
    private void playFloor7() {
        showDialogue("floor7", "story");

        model.Stage resumePos = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(7, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) break;

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.HEAP_ENTRY.equals(eid)) {
                EventResult heapResult = eventManager.trigger(eid);
                if (heapResult == EventResult.SHOW_DIALOGUE) {
                    showDialogue("floor7", "heap_entry");
                    result.getCurrentPos().consume();
                    state = GameState.HIDDEN_MAP;
                    return;
                }
            }
            resumePos = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos;

        // 힙 미진입 시 바로 보스전
        if (fightFloor7Boss(lastPos)) {
            state = GameState.FINAL_FLOOR;
        }
    }

    /** 7층 보스전 (히든맵 전후 공용). 승리 시 true 반환. */
    private boolean fightFloor7Boss(model.Stage lastPos) {
        String startTag = hyejinRoute ? "battle_boss_start_with_hyejin" : "battle_boss_start_without_hyejin";
        String winTag = hyejinRoute ? "battle_boss_win_with_hyejin" : "battle_boss_win_without_hyejin";

        while (true) {
            showDialogue("floor7", startTag);
            NPC minjung = createBoss("B007", "민중", 150, 18, 24);
            Card minjungCard = createEnemyCard("민중", 28);
            BattleResult result = battleManager.startBattle(
                    hero, minjung, "민중", playerCards, playerItems, minjungCard);

            if (result == BattleResult.WIN) {
                showDialogue("floor7", winTag);
                awardCard("C010", 0, "instanceof", "TYPE_CHECK", 45,
                        "적의 타입을 간파하여 약점을 찾아낸다.");
                hero.heal(30);
                return true;
            }
            if (result == BattleResult.LOSE) {
                state = GameState.GAME_OVER;
                return false;
            }
            // FLEE → 직전 위치로 복귀 후 재도전
            gameView.showMessage("도망쳤다... 다시 출구를 찾아야 한다.");
            gameView.waitForEnter();
            ExploreResult fleeResult7 = stageManager.exploreFloor(7, gameView, lastPos, playerCards, playerItems);
            if (fleeResult7 != null) lastPos = fleeResult7.getLastPos();
        }
    }

    // ============================================
    // 히든맵: 힙 영역 - 쓰레기촌
    // ============================================
    private void playHiddenMap() {
        showDialogue("heap", "story");

        int choice = gameView.showChoice("혜진 루트", "보경 루트");

        if (choice == 1) {
            // 혜진 루트: GC 전투
            hyejinRoute = true;
            showDialogue("heap", "hyejin_route");

            NPC gc = new NPC("M_GC", "GC",
                    "가비지 컬렉터. 더 이상 참조되지 않는 것들을 수거한다.",
                    80, false, 14, 20, 1, null);
            Card gcCard = createEnemyCard("GC", 18);
            BattleResult gcResult = battleManager.startBattle(
                    hero, gc, "GC", playerCards, playerItems, gcCard);

            if (gcResult == BattleResult.LOSE) {
                showDialogue("heap", "gc_battle_lose");
                state = GameState.GAME_OVER;
                return;
            } else if (gcResult == BattleResult.FLEE) {
                showDialogue("heap", "battle_gc_run_away");
            }
            hasBracket = true;
        } else {
            // 보경 루트: 퀴즈
            showDialogue("heap", "bokyung_route");

            gameView.showMessage("\n[ 보경의 퀴즈 ]");
            gameView.showMessage("Q. Java에서 가비지 컬렉션을 강제로 실행하는 메서드는?");
            int quizChoice = gameView.showChoice("System.gc()", "Runtime.collect()", "Memory.free()");

            if (quizChoice == 1) {
                showDialogue("heap", "bokyung_quiz_solve");
                hasBracket = true;
                gameView.showAcquire("} (닫는 괄호)");
            } else {
                showDialogue("heap", "bokyung_quiz_wrong");
            }
        }

        showDialogue("floor7", "heap_escape");

        // 히든맵 후 7층 보스전 (히든맵 경유이므로 출구 직전 위치 없음)
        if (fightFloor7Boss(null)) {
            state = GameState.FINAL_FLOOR;
        }
    }

    // ============================================
    // 8층: 딸깍이 최종전
    // ============================================
    private void playFinalFloor() {
        if (hyejinRoute) {
            showDialogue("floor8", "story_with_hyejin");
        } else {
            showDialogue("floor8", "story_without_hyejin");
        }

        EventResult bracketResult = eventManager.trigger(EventManager.FINAL_BRACKET);
        if (bracketResult == EventResult.ENDING_DEFEAT) {
            triggerEnding("DEFEAT");
            return;
        }

        if (hyejinRoute) {
            showDialogue("floor8", "use_bracket_with_hyejin");
            triggerEnding("HYEJIN");
        } else {
            showDialogue("floor8", "use_bracket_without_hyejin");
            triggerEnding("NO_HYEJIN");
        }
    }

    // ============================================
    // 엔딩 처리
    // ============================================
    private void triggerEnding(String type) {
        this.endingType = type;
        state = GameState.ENDING;
    }

    private void playEnding() {
        String endingName;
        switch (endingType) {
            case "FALL":       endingName = "넘어짐";     break;
            case "SHORTCUT":   endingName = "최단거리";   break;
            case "BETRAYAL":   endingName = "뒷통수";     break;
            case "GC":         endingName = "GC";         break;
            case "DEFEAT":     endingName = "패배";       break;
            case "HYEJIN":     endingName = "혜진";       break;
            case "NO_HYEJIN":  endingName = "혜진 X";     break;
            default:           endingName = endingType;   break;
        }
        gameView.showEndingTitle(endingName);

        // TODO: ending 테이블에서 엔딩 메시지 조회 후 출력
        // TODO: collectionManager.unlockEnding(endingId, currentTry);
        gameView.waitForEnter();

        // 승리 엔딩(SHORTCUT, HYEJIN, NO_HYEJIN)에서 엔딩 크레딧 출력
        if ("SHORTCUT".equals(endingType) || "HYEJIN".equals(endingType)
                || "NO_HYEJIN".equals(endingType)) {
            showDialogue("system", "ending_credit");
        }

        state = GameState.GAME_OVER;
    }

    // ============================================
    // 대사 출력 유틸
    // ============================================
    private void showDialogue(String prefix, String tag) {
        List<String> lines = storyManager.get(prefix, tag);
        gameView.showDialogue(lines);
    }

    // ============================================
    // Getters
    // ============================================
    public GameState getState() { return state; }
    public boolean hasSemicolon() { return hasSemicolon; }
    public boolean hasBracket() { return hasBracket; }
}
