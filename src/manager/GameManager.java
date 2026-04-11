package manager;

import dao.SaveDAO;
import dao.StageDAO;
import manager.BattleManager.BattleResult;
import manager.EventManager.EventResult;
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
        EXPLORE_FLOOR,
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
    private MainMenuView mainMenuView;
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
        playerCards.add(Card.createAttackCard("C001", 1, "Scanner", "SCAN_INPUT", 10,
                "사용자 입력을 읽어들이는 기본 도구.", null, null, 0));

        playerItems.clear();
        for (int i = 0; i < 3; i++) {
            playerItems.add(createPotion());
        }
        saveManager.resetAllSaves();
        hasSemicolon = false;
        hasBracket = false;
        hyejinRoute = false;
        stageManager.buildFloorChain(8);

        if (createSave) {   // ← 이어하기 시엔 실행 안 됨
            saveManager.resetAllSaves();
            Save newSave = saveManager.createNewSave(1); // ✅ 1층 시작으로 통일
            if (newSave != null) {
                stageManager.setCurrentTryNum(newSave.getTryNum());
                System.out.println("[New Game] 시작 위치: 1_a1 (tryNum=" + newSave.getTryNum() + ")");
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
            StageManager.FloorResult floorResult = stageManager.exploreFloor(7, gameView, lastPos);
            if (floorResult != null) lastPos = floorResult.prevPos;
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
                Save loaded = saveManager.loadLatestSave();
                if (loaded != null && loaded.getLId() != null) {
                    System.out.println("[이어하기] Try #" + loaded.getTryNum()
                        + " | Stage:" + loaded.getLId()
                        + " | " + loaded.getT_time());

                    // ✅ initGameData 호출 없이 필요한 것만 초기화
                    hero = new Hero("H001", 100, 5, 10, 0, "C001");
                    playerCards.clear();
                    playerCards.add(Card.createAttackCard("C001", 1, "Scanner", "SCAN_INPUT", 10,
                        "사용자 입력을 읽어들이는 기본 도구.", null, null, 0));
                    playerItems.clear();
                    for (int i = 0; i < 3; i++) playerItems.add(createPotion());
                    stageManager.buildFloorChain(8);

                    // ✅ tryNum 설정 (DB 삭제 없이)
                    stageManager.setCurrentTryNum(loaded.getTryNum());

                    // ✅ 해당 층 GameState로 전환 (exploreFloor 직접 호출 X)
                    state = getStateFromSaveId(loaded.getLId());

                } else {
                    menuView.showNoSaveData();
                }
                break;
            case 3: collectionManager.showCollectionMenu(); break;
            case 4: state = GameState.GAME_OVER; break;
        }
    }
    // Stage ID에서 층 번호 추출 ("2_a1" → 2)
    private int extractFloorFromStage(String stageId) {
        if (stageId == null || stageId.isEmpty()) return 1;
        return Integer.parseInt(stageId.substring(0, 1));
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

        BattleResult result = battleManager.startBattle(
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

        // 승리: 혜진 사라짐 + Math.random 카드 획득
        showDialogue("floor1", "hyejin_disappear");
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

        StageManager.FloorResult result = stageManager.exploreFloor(2, gameView);

        // TODO: 맵 셀 이벤트로 이동 (comment_branch, semicolon_find)

        // 미주 보스전
        if (!bossBattleLoop(2, "floor2", "battle_boss_start", "battle_boss_win",
                "B002", "미주", 70, 8, 12, 15, result.prevPos)) return;

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

        // 세미콜론 문
        EventResult doorResult = eventManager.trigger(EventManager.SEMICOLON_DOOR);
        if (doorResult == EventResult.ENDING_SHORTCUT) {
            showDialogue("floor3", "semicolon_door_open");
            triggerEnding("SHORTCUT");
            return;
        } else if (doorResult == EventResult.DOOR_LOCKED) {
            showDialogue("floor3", "semicolon_door_locked");
        }

        StageManager.FloorResult result = stageManager.exploreFloor(3, gameView);
        // TODO: 맵 셀 이벤트로 이동 (interpreter_robot)

        // 솔민 보스전
        if (!bossBattleLoop(3, "floor3", "battle_boss_start", "battle_boss_win",
                "B003", "솔민", 85, 10, 14, 18, result.prevPos)) return;

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

        StageManager.FloorResult result = stageManager.exploreFloor(4, gameView);

        // 선혁 의심 이벤트 (주석 + 실행엔진 이벤트를 모두 경험한 경우에만 활성화)
        EventResult suspectResult = eventManager.trigger(EventManager.SUSPECT_SUNHYUK);
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

        // 제석 보스전
        if (!bossBattleLoop(4, "floor4", "battle_boss_start", "battle_boss_win",
                "B004", "제석", 100, 12, 16, 20, result.prevPos)) return;

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

        StageManager.FloorResult result = stageManager.exploreFloor(5, gameView);

        // 수지 보스전
        if (!bossBattleLoop(5, "floor5", "battle_boss_start", "battle_boss_win",
                "B005", "수지", 110, 14, 18, 22, result.prevPos)) return;

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

        StageManager.FloorResult result = stageManager.exploreFloor(6, gameView);

        // TODO: 맵 셀 이벤트로 이동 (cache_battle)

        // 봉민 보스전
        if (!bossBattleLoop(6, "floor6", "battle_boss_start", "battle_boss_win",
                "B006", "봉민", 130, 16, 20, 25, result.prevPos)) return;

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

        StageManager.FloorResult result = stageManager.exploreFloor(7, gameView);

        // 힙 영역 진입 체크
        EventResult heapResult = eventManager.trigger(EventManager.HEAP_ENTRY);
        if (heapResult == EventResult.SHOW_DIALOGUE) {
            showDialogue("floor7", "heap_entry");
            state = GameState.HIDDEN_MAP;
            return;
        }

        // 힙 미진입 시 바로 보스전
        if (fightFloor7Boss(result.prevPos)) {
            state = GameState.FINAL_FLOOR;
        }
    }

    /** 7층 보스전 (히든맵 전후 공용). 승리 시 true 반환. */
    private boolean fightFloor7Boss(model.Stage lastPos) {
        String startTag = hyejinRoute ? "battle_boss_start_with_hyejin" : "battle_boss_start_without_heyjin";
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
            StageManager.FloorResult floorResult = stageManager.exploreFloor(7, gameView, lastPos);
            if (floorResult != null) lastPos = floorResult.prevPos;
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
