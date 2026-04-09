package manager;

import manager.BattleManager.BattleResult;
import manager.EventManager.EventResult;
import model.Card;
import model.Item;

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
    // TODO: private StageManager stageManager;
    // TODO: private SaveManager saveManager;

    public GameManager() {
        this.scanner = new Scanner(System.in);
        this.battleManager = new BattleManager(scanner);
        this.eventManager = new EventManager();
        this.storyManager = new StoryManager();

        this.state = GameState.MAIN_MENU;
        this.hasSemicolon = false;
        this.hasBracket = false;
        this.hyejinRoute = false;
    }

    // ============================================
    // 메인 게임 루프
    // ============================================
    public void run() {
        // 스토리 파일 로드
        storyManager.loadAll("resources/story");

        while (state != GameState.GAME_OVER) {
            // EventManager에 현재 상태 동기화
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
        // TODO: MainMenuView로 출력 위임
        System.out.println("1. 새 게임");
        System.out.println("2. 이어하기");
        System.out.println("3. 도감");
        System.out.println("4. 종료");

        String input = scanner.nextLine().trim();
        switch (input) {
            case "1": state = GameState.PROLOGUE; break;
            case "2": /* TODO: SaveManager.load() */ break;
            case "3": /* TODO: CollectionManager.show() */ break;
            case "4": state = GameState.GAME_OVER; break;
        }
    }

    // ============================================
    // 프롤로그
    // 영균이 딸깍이에게 과제 시킴 → 모니터에 빨려들어감
    // ============================================
    private void playPrologue() {
        showDialogue("prologue", "story");
        state = GameState.TUTORIAL;
    }

    // ============================================
    // 1층: 설산 (튜토리얼)
    // 혜진, 선혁 합류 → Scanner 획득 → 혜진 VS 영균 연습전투
    // → 혜진 소멸(힙 영역으로) → 눈사태 → 다음 층
    // ============================================
    private void playTutorial() {
        showDialogue("floor1", "story");

        // Scanner 카드 획득
        // TODO: DAO에서 카드 로드 후 추가
        // playerCards.add(scannerCard);

        // 혜진 VS 영균 (튜토리얼 전투)
        // TODO: Hero, 혜진 Entity 생성
        // BattleResult result = battleManager.startBattle(hero, hyejin, playerCards, playerItems, hyejinCard);
        // if (result == BattleResult.FLEE) {
        //     EventResult eventResult = eventManager.trigger(EventManager.TUTORIAL_FLEE);
        //     if (eventResult == EventResult.ENDING_FALL) {
        //         showDialogue("floor1", "ending_fall");
        //         triggerEnding("FALL");
        //         return;
        //     }
        // }

        // 혜진 소멸 → random 카드 획득
        showDialogue("floor1", "hyejin_disappear");

        // 눈사태 → 다음 층으로
        showDialogue("floor1", "avalanche");

        state = GameState.FLOOR_2;
    }

    // ============================================
    // 2층: 숲 (미주)
    // 나뭇가지 주석 이벤트, [ ; ] 카드 습득, 미주 보스전
    // ============================================
    private void playFloor2() {
        showDialogue("floor2", "story");

        // TODO: StageManager - 5x5 맵 탐색 루프
        // 맵 탐색 중 이벤트 노드 도달 시:

        // 나뭇가지 주석 이벤트
        EventResult commentResult = eventManager.trigger(EventManager.COMMENT_BRANCH);
        if (commentResult == EventResult.SHOW_DIALOGUE) {
            showDialogue("floor2", "comment_branch");
        }

        // [ ; ] 카드 발견 (같은 구역 3번 방문 시)
        // eventManager.setVisitCount(visitCount);
        EventResult semicolonResult = eventManager.trigger(EventManager.SEMICOLON_FIND);
        if (semicolonResult == EventResult.GET_CARD) {
            showDialogue("floor2", "semicolon_find");
            hasSemicolon = true;
        }

        // 미주 보스전
        showDialogue("floor2", "battle_boss_start");
        // TODO: BattleManager - 미주 전투
        // BattleResult result = battleManager.startBattle(hero, miju, playerCards, playerItems, mijuCard);
        showDialogue("floor2", "battle_boss_win");

        // 바닥 붕괴 → 다음 층
        showDialogue("floor2", "collapse");

        state = GameState.FLOOR_3;
    }

    // ============================================
    // 3층: 들판 (솔민)
    // 세미콜론 문 → 최단거리 엔딩 분기, 솔민 보스전
    // ============================================
    private void playFloor3() {
        showDialogue("floor3", "story");

        // 세미콜론 문 이벤트
        EventResult doorResult = eventManager.trigger(EventManager.SEMICOLON_DOOR);
        if (doorResult == EventResult.ENDING_SHORTCUT) {
            showDialogue("floor3", "semicolon_door_open");
            triggerEnding("SHORTCUT");
            return;
        } else if (doorResult == EventResult.DOOR_LOCKED) {
            showDialogue("floor3", "semicolon_door_locked");
        }

        // 인터프리터/JIT 로봇 목격
        EventResult robotResult = eventManager.trigger(EventManager.INTERPRETER_ROBOT);
        if (robotResult == EventResult.SHOW_DIALOGUE) {
            showDialogue("floor3", "interpreter_robot");
        }

        // 솔민 보스전
        showDialogue("floor3", "battle_boss_start");
        // TODO: BattleManager - 솔민 전투
        showDialogue("floor3", "battle_boss_win");

        // 구덩이 추락 → 다음 층
        showDialogue("floor3", "collapse");

        state = GameState.FLOOR_4;
    }

    // ============================================
    // 4층: 사막 (제석)
    // 선혁 VS 영균 → 선혁 뒷통수 엔딩 분기, 제석 보스전
    // ============================================
    private void playFloor4() {
        showDialogue("floor4", "story");

        // 선혁 의심 이벤트 → 선택지
        showDialogue("floor4", "suspect_sunhyuk");
        System.out.println("1. 선혁을 공격한다");
        System.out.println("2. 선혁을 믿는다");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            // 선혁 VS 영균 전투
            EventResult suspectResult = eventManager.trigger(EventManager.SUSPECT_SUNHYUK);
            // TODO: BattleManager - 선혁 VS 영균
            // BattleResult result = battleManager.startBattle(hero, sunhyuk, playerCards, playerItems, sunhyukCard);
            // if (result == BattleResult.LOSE) {
            //     showDialogue("floor4", "betrayal_lose");
            //     triggerEnding("BETRAYAL");
            //     return;
            // }
            showDialogue("floor4", "betrayal_win");  // 승리 → 화해
        } else {
            showDialogue("floor4", "trust_sunhyuk");
        }

        // 제석 보스전
        showDialogue("floor4", "battle_boss_start");
        // TODO: BattleManager - 제석 전투
        showDialogue("floor4", "battle_boss_win");

        // 모래지옥 → 다음 층
        showDialogue("floor4", "collapse");

        state = GameState.FLOOR_5;
    }

    // ============================================
    // 5층: 강물 (수지)
    // ============================================
    private void playFloor5() {
        showDialogue("floor5", "story");

        // 수지 보스전
        showDialogue("floor5", "battle_boss_start");
        // TODO: BattleManager - 수지 전투
        showDialogue("floor5", "battle_boss_win");

        showDialogue("floor5", "collapse");

        state = GameState.FLOOR_6;
    }

    // ============================================
    // 6층: 심해 (봉민)
    // ============================================
    private void playFloor6() {
        showDialogue("floor6", "story");

        // 캐시 전투 이벤트
        EventResult cacheResult = eventManager.trigger(EventManager.CACHE_BATTLE);
        if (cacheResult == EventResult.START_BATTLE) {
            showDialogue("floor6", "cache_battle");
            // TODO: BattleManager - 캐시 전투
        }

        // 봉민 보스전
        showDialogue("floor6", "battle_boss_start");
        // TODO: BattleManager - 봉민 전투
        showDialogue("floor6", "battle_boss_win");

        showDialogue("floor6", "collapse");

        state = GameState.FLOOR_7;
    }

    // ============================================
    // 7층: 화산 (민중)
    // ============================================
    private void playFloor7() {
        showDialogue("floor7", "story");

        // 민중 보스전
        showDialogue("floor7", "battle_boss_start");
        // TODO: BattleManager - 민중 전투
        showDialogue("floor7", "battle_boss_win");

        // 힙 영역 진입 분기
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

        // 혜진 재회
        eventManager.trigger(EventManager.HYEJIN_REUNION);
        showDialogue("hidden", "hyejin_reunion");

        // 루트 선택
        System.out.println("1. 혜진 루트");
        System.out.println("2. 보경 루트");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
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

        // } 카드 체크
        EventResult bracketResult = eventManager.trigger(EventManager.FINAL_BRACKET);
        if (bracketResult == EventResult.ENDING_DEFEAT) {
            showDialogue("floor8", "no_bracket");
            triggerEnding("DEFEAT");
            return;
        }

        // } 카드 사용 → 최종 선택지
        showDialogue("floor8", "use_bracket");
        System.out.println("1. 혜진 엔딩");
        System.out.println("2. 혜진 X 엔딩");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
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
        // 엔딩별 대사 출력
        switch (endingType) {
            case "FALL":       showDialogue("ending", "fall");       break;
            case "SHORTCUT":   showDialogue("ending", "shortcut");   break;
            case "BETRAYAL":   showDialogue("ending", "betrayal");   break;
            case "GC":         showDialogue("ending", "gc");         break;
            case "DEFEAT":     showDialogue("ending", "defeat");     break;
            case "HYEJIN":     showDialogue("ending", "hyejin");     break;
            case "NO_HYEJIN":  showDialogue("ending", "no_hyejin");  break;
        }

        // TODO: Collection - 엔딩 해금 처리
        state = GameState.GAME_OVER;
    }

    // ============================================
    // 대사 출력 유틸
    // ============================================
    private void showDialogue(String prefix, String tag) {
        List<String> lines = storyManager.get(prefix, tag);
        for (String line : lines) {
            // TODO: View - 타이핑 효과로 출력
            System.out.println(line);
        }
        // TODO: View - 입력 대기 (Enter로 다음 진행)
    }

    // ============================================
    // Getters
    // ============================================
    public GameState getState() { return state; }
    public boolean hasSemicolon() { return hasSemicolon; }
    public boolean hasBracket() { return hasBracket; }
}
