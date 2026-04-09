package manager;

import manager.BattleManager.BattleResult;
import manager.EventManager.EventResult;
import model.Card;
import model.Item;
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

    // 플레이어 인벤토리 (TODO: Hero 객체로 통합 가능)
    private List<Card> playerCards = new ArrayList<>();
    private List<Item> playerItems = new ArrayList<>();

    // 매니저
    private Scanner scanner;
    private BattleManager battleManager;
    private EventManager eventManager;
    private StoryManager storyManager;
    private CollectionManager collectionManager;
    // TODO: private StageManager stageManager;
    // TODO: private SaveManager saveManager;

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

        this.state = GameState.MAIN_MENU;
        this.hasSemicolon = false;
        this.hasBracket = false;
        this.hyejinRoute = false;
    }

    // ============================================
    // 메인 게임 루프
    // ============================================
    public void run() {
        storyManager.loadAll("resources/story");

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
            case 1: state = GameState.PROLOGUE; break;
            case 2: /* TODO: SaveManager.load() */ break;
            case 3: collectionManager.showCollectionMenu(); break;
            case 4: state = GameState.GAME_OVER; break;
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

        // TODO: DAO에서 카드 로드 후 추가
        // playerCards.add(scannerCard);

        // TODO: Hero, 혜진 Entity 생성 후 전투 연결
        // BattleResult result = battleManager.startBattle(hero, hyejin, "혜진", playerCards, playerItems, hyejinCard);
        // if (result == BattleResult.FLEE) {
        //     EventResult eventResult = eventManager.trigger(EventManager.TUTORIAL_FLEE);
        //     if (eventResult == EventResult.ENDING_FALL) {
        //         showDialogue("floor1", "ending_fall");
        //         triggerEnding("FALL");
        //         return;
        //     }
        // }

        showDialogue("floor1", "hyejin_disappear");
        showDialogue("floor1", "avalanche");

        state = GameState.FLOOR_2;
    }

    // ============================================
    // 2층: 숲 (미주)
    // ============================================
    private void playFloor2() {
        showDialogue("floor2", "story");

        // TODO: StageManager - 5x5 맵 탐색 루프

        EventResult commentResult = eventManager.trigger(EventManager.COMMENT_BRANCH);
        if (commentResult == EventResult.SHOW_DIALOGUE) {
            showDialogue("floor2", "comment_branch");
        }

        EventResult semicolonResult = eventManager.trigger(EventManager.SEMICOLON_FIND);
        if (semicolonResult == EventResult.GET_CARD) {
            showDialogue("floor2", "semicolon_find");
            hasSemicolon = true;
        }

        showDialogue("floor2", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, miju, "미주", playerCards, playerItems, mijuCard);
        showDialogue("floor2", "battle_boss_win");

        showDialogue("floor2", "collapse");
        state = GameState.FLOOR_3;
    }

    // ============================================
    // 3층: 들판 (솔민)
    // ============================================
    private void playFloor3() {
        showDialogue("floor3", "story");

        EventResult doorResult = eventManager.trigger(EventManager.SEMICOLON_DOOR);
        if (doorResult == EventResult.ENDING_SHORTCUT) {
            showDialogue("floor3", "semicolon_door_open");
            triggerEnding("SHORTCUT");
            return;
        } else if (doorResult == EventResult.DOOR_LOCKED) {
            showDialogue("floor3", "semicolon_door_locked");
        }

        EventResult robotResult = eventManager.trigger(EventManager.INTERPRETER_ROBOT);
        if (robotResult == EventResult.SHOW_DIALOGUE) {
            showDialogue("floor3", "interpreter_robot");
        }

        showDialogue("floor3", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, solmin, "솔민", playerCards, playerItems, solminCard);
        showDialogue("floor3", "battle_boss_win");

        showDialogue("floor3", "collapse");
        state = GameState.FLOOR_4;
    }

    // ============================================
    // 4층: 사막 (제석)
    // ============================================
    private void playFloor4() {
        showDialogue("floor4", "story");

        showDialogue("floor4", "suspect_sunhyuk");
        int choice = gameView.showChoice("선혁을 공격한다", "선혁을 믿는다");

        if (choice == 1) {
            EventResult suspectResult = eventManager.trigger(EventManager.SUSPECT_SUNHYUK);
            // TODO: BattleResult result = battleManager.startBattle(hero, sunhyuk, "선혁", ...);
            // if (result == BattleResult.LOSE) {
            //     showDialogue("floor4", "betrayal_lose");
            //     triggerEnding("BETRAYAL");
            //     return;
            // }
            showDialogue("floor4", "betrayal_win");
        } else {
            showDialogue("floor4", "trust_sunhyuk");
        }

        showDialogue("floor4", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, jeseok, "제석", playerCards, playerItems, jeseokCard);
        showDialogue("floor4", "battle_boss_win");

        showDialogue("floor4", "collapse");
        state = GameState.FLOOR_5;
    }

    // ============================================
    // 5층: 강물 (수지)
    // ============================================
    private void playFloor5() {
        showDialogue("floor5", "story");

        showDialogue("floor5", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, suji, "수지", playerCards, playerItems, sujiCard);
        showDialogue("floor5", "battle_boss_win");

        showDialogue("floor5", "collapse");
        state = GameState.FLOOR_6;
    }

    // ============================================
    // 6층: 심해 (봉민)
    // ============================================
    private void playFloor6() {
        showDialogue("floor6", "story");

        EventResult cacheResult = eventManager.trigger(EventManager.CACHE_BATTLE);
        if (cacheResult == EventResult.START_BATTLE) {
            showDialogue("floor6", "cache_battle");
            // TODO: 캐시 전투
        }

        showDialogue("floor6", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, bongmin, "봉민", playerCards, playerItems, bongminCard);
        showDialogue("floor6", "battle_boss_win");

        showDialogue("floor6", "collapse");
        state = GameState.FLOOR_7;
    }

    // ============================================
    // 7층: 화산 (민중)
    // ============================================
    private void playFloor7() {
        showDialogue("floor7", "story");

        showDialogue("floor7", "battle_boss_start");
        // TODO: battleManager.startBattle(hero, minjung, "민중", playerCards, playerItems, minjungCard);
        showDialogue("floor7", "battle_boss_win");

        EventResult heapResult = eventManager.trigger(EventManager.HEAP_ENTRY);
        // TODO: 진입 조건 구체화
        // 조건 충족 시:
        //   showDialogue("floor7", "heap_entry");
        //   state = GameState.HIDDEN_MAP;
        //   return;

        showDialogue("floor7", "collapse");
        state = GameState.FINAL_FLOOR;
    }

    // ============================================
    // 히든맵: 힙 영역 - 쓰레기촌
    // ============================================
    private void playHiddenMap() {
        showDialogue("hidden", "story");

        eventManager.trigger(EventManager.HYEJIN_REUNION);
        showDialogue("hidden", "hyejin_reunion");

        int choice = gameView.showChoice("혜진 루트", "보경 루트");

        if (choice == 1) {
            hyejinRoute = true;
            showDialogue("hidden", "hyejin_route");
            triggerEnding("GC");
            return;
        }

        showDialogue("hidden", "bokyung_route");
        state = GameState.FINAL_FLOOR;
    }

    // ============================================
    // 8층: 딸깍이 최종전
    // ============================================
    private void playFinalFloor() {
        showDialogue("floor8", "story");

        EventResult bracketResult = eventManager.trigger(EventManager.FINAL_BRACKET);
        if (bracketResult == EventResult.ENDING_DEFEAT) {
            showDialogue("floor8", "no_bracket");
            triggerEnding("DEFEAT");
            return;
        }

        showDialogue("floor8", "use_bracket");
        int choice = gameView.showChoice("혜진 엔딩", "혜진 X 엔딩");

        if (choice == 1) {
            triggerEnding("HYEJIN");
        } else {
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
        // 엔딩 타이틀 표시
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

        // 엔딩 대사 출력
        switch (endingType) {
            case "FALL":       showDialogue("ending", "fall");       break;
            case "SHORTCUT":   showDialogue("ending", "shortcut");   break;
            case "BETRAYAL":   showDialogue("ending", "betrayal");   break;
            case "GC":         showDialogue("ending", "gc");         break;
            case "DEFEAT":     showDialogue("ending", "defeat");     break;
            case "HYEJIN":     showDialogue("ending", "hyejin");     break;
            case "NO_HYEJIN":  showDialogue("ending", "no_hyejin");  break;
        }

        // TODO: collectionManager.unlockEnding(endingId, currentTry);
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
