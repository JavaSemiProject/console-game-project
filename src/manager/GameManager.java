package manager;

import dao.CardDAO;
import dao.CollectionDAO;
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
    private model.Stage heapReturnPos; // 히든맵 탈출 후 7층 복귀 위치
    private boolean running = true;    // false 시 프로그램 종료

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

        hasSemicolon = false;
        hasBracket = false;
        hyejinRoute = false;
        stageManager.buildFloorChain(8);

        if (createSave) {   // ← 이어하기 시엔 실행 안 됨
            Save newSave = saveManager.createNewSave(2, playerCards, playerItems);
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
    private NPC createBoss(String id, String name, int hp, int atkMin, int atkMax, int pp) {
        return new NPC(id, name, name + " 보스", hp, true, atkMin, atkMax, pp, null);
    }

    private Card createEnemyCard(String name) {
        return Card.createAttackCard("E_" + name, 0, name + "의 공격",
                "ENEMY_ATK", 0, name + "의 공격", null, null, 0);
    }

    // ============================================
    // 도감 로깅 헬퍼
    // ============================================
    private final dao.CollectionDAO collectionDAO = new dao.CollectionDAO();

    private void logCard(String cId) {
        int t = stageManager.getCurrentTryNum();
        if (t > 0) collectionDAO.logCard(cId, t);
    }

    private void logItem(String iId) {
        int t = stageManager.getCurrentTryNum();
        if (t > 0) collectionDAO.logItem(iId, t);
    }

    private void logNpc(String nId) {
        int t = stageManager.getCurrentTryNum();
        if (t > 0) collectionDAO.logNpc(nId, t);
    }

    // ============================================
    // 헬퍼: 카드 획득 (DB에서 로드 + 도감 로그)
    // ============================================
    private void awardCardFromDB(String cardId) {
        Card card = new CardDAO().findById(cardId);
        if (card != null) {
            playerCards.add(card);
            gameView.showAcquire(card.getCName());
            logCard(cardId);
        }
    }

    /**
     * 랜덤 NPC 조우 처리. 승리 시 회복, 패배 시 GAME_OVER 설정.
     * @return 전투 결과
     */
    private BattleResult handleNpcEncounter(ExploreResult result) {
        String npcId = result.getEventId();
        NPC npc = stageManager.getNpcById(npcId);
        if (npc == null) return BattleResult.WIN;

        logNpc(npcId);  // 조우 로그

        Card npcCard = createEnemyCard(npc.getNName());
        BattleResult br = battleManager.startBattle(
                hero, npc, npc.getNName(), playerCards, playerItems, npcCard);

        if (br == BattleResult.WIN) {
            if (npc.getCId() != null) {
                Card drop = new CardDAO().findById(npc.getCId());
                if (drop != null) {
                    playerCards.add(drop);
                    gameView.showAcquire(drop.getCName());
                    logCard(npc.getCId());
                }
            }
            if (npc.getIId() != null) {
                Item dropItem = new ItemDAO().findById(npc.getIId());
                if (dropItem != null) {
                    playerItems.add(dropItem);
                    gameView.showAcquire(dropItem.getIName());
                    logItem(npc.getIId());
                }
            }
        } else if (br == BattleResult.LOSE) {
            state = GameState.GAME_OVER;
        }
        return br;
    }

    private void fullHealOnFloorStart() {
        hero.setCurrentHealth(hero.getHealth());
    }

    private void updateSaveFloor(int floorLevel) {
        saveManager.updateSaveToFloor(floorLevel, stageManager.getCurrentTryNum(), playerCards, playerItems);
    }

    /**
     * 보스전 루프. 도망 시 맵 탐색으로 복귀 후 재도전.
     * @return true=승리, false=패배(GAME_OVER 설정됨)
     */
    private boolean bossBattleLoop(int floorLevel, String prefix, String startTag, String winTag,
                                   String bossId, String bossName, int hp, int atkMin, int atkMax,
                                   int bossPp, model.Stage lastPos, String nDbId) {

        boolean loggedNpc = false;
        while (true) {
            showDialogue(prefix, startTag);
            if (!loggedNpc) { logNpc(nDbId); loggedNpc = true; }
            NPC boss = createBoss(bossId, bossName, hp, atkMin, atkMax, bossPp);
            Card bossCard = createEnemyCard(bossName);
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

        while (running) {
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
                case GAME_OVER:
                    triggerEnding("LOSE");
                    break;
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
                    // 보유 카드/아이템 복원 (스냅샷이 비어있으면 초기 상태 유지)
                    if (loaded.getCards() != null && !loaded.getCards().isEmpty()) {
                        playerCards.clear();
                        playerCards.addAll(loaded.getCards());
                    }
                    if (loaded.getItems() != null && !loaded.getItems().isEmpty()) {
                        playerItems.clear();
                        playerItems.addAll(loaded.getItems());
                    }
                    GameState savedState = getStateFromSaveId(loaded.getLId());
                    gameView.showMessage("[이어하기] " + loaded.getSaveStatus());
                    gameView.waitForEnter();
                    state = savedState;
                } else {
                    menuView.showNoSaveData();
                }
                break;
            case 3: collectionManager.showCollectionMenu(); break;
            case 4: running = false; break;
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
                50, false, 5, 8, 0, "C002"); // 혜진: 진=받침ㄴ → pp=0
        Card hyejinCard = createEnemyCard("혜진");

        logNpc("n2");  // 혜진 조우 로그
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
        awardCardFromDB("c2");

        state = GameState.FLOOR_2;
    }

    // ============================================
    // 2층: 숲 (미주)
    // ============================================
    private void playFloor2() {
        fullHealOnFloorStart();
        updateSaveFloor(2);
        showDialogue("floor2", "story");

        model.Stage resumePos = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(2, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos = result.getLastPos();
                break;
            }

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
                    logCard("c3");
                }
                gameView.showAcquire("; (세미콜론)");
                result.getCurrentPos().consume();
            }
            resumePos = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos;

        // 미주 보스전 (미주: 주=모음 → pp=1)
        if (!bossBattleLoop(2, "floor2", "battle_boss_start", "battle_boss_win",
                "B002", "미주", 70, 8, 12, 1, lastPos, "n4")) return;

        awardCardFromDB("c4");

        state = GameState.FLOOR_3;
    }

    // ============================================
    // 3층: 들판 (솔민)
    // ============================================
    private void playFloor3() {
        fullHealOnFloorStart();
        updateSaveFloor(3);
        showDialogue("floor3", "story");

        model.Stage resumePos3 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(3, gameView, resumePos3, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos3 = result.getLastPos();
                break;
            }

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
            } else if (EventManager.INTERPRETER_ROBOT.equals(eid)) {
                showDialogue("floor3", "interpreter_robot");
                eventManager.setInterpreterRobotDone(true);
                result.getCurrentPos().consume();
            }
            resumePos3 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos3;

        // 솔민 보스전 (솔민: 민=받침ㄴ → pp=0)
        if (!bossBattleLoop(3, "floor3", "battle_boss_start", "battle_boss_win",
                "B003", "솔민", 85, 10, 14, 0, lastPos, "n5")) return;

        awardCardFromDB("c5");

        state = GameState.FLOOR_4;
    }

    // ============================================
    // 4층: 사막 (제석)
    // ============================================
    private void playFloor4() {
        fullHealOnFloorStart();
        updateSaveFloor(4);
        showDialogue("floor4", "story");

        model.Stage resumePos = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(4, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos = result.getLastPos();
                break;
            }

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

                    logNpc("n3");  // 선혁 조우 로그
                    NPC sunhyuk = new NPC("N002", "선혁",
                            "같이 코드 세계에 갇힌 동료.", 60, false, 8, 12, 0, null);
                    Card sunhyukCard = createEnemyCard("선혁");
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

        // 제석 보스전 (제석: 석=받침ㄱ → pp=0)
        if (!bossBattleLoop(4, "floor4", "battle_boss_start", "battle_boss_win",
                "B004", "제석", 100, 12, 16, 0, lastPos, "n6")) return;

        awardCardFromDB("c6");

        state = GameState.FLOOR_5;
    }

    // ============================================
    // 5층: 강물 (수지)
    // ============================================
    private void playFloor5() {
        fullHealOnFloorStart();
        updateSaveFloor(5);
        showDialogue("floor5", "story");

        model.Stage resumePos5 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(5, gameView, resumePos5, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos5 = result.getLastPos();
                break;
            }

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos5 = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }
            resumePos5 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos5;

        // 수지 보스전 (수지: 지=모음 → pp=1)
        if (!bossBattleLoop(5, "floor5", "battle_boss_start", "battle_boss_win",
                "B005", "수지", 110, 14, 18, 1, lastPos, "n7")) return;

        awardCardFromDB("c7");

        state = GameState.FLOOR_6;
    }

    // ============================================
    // 6층: 심해 (봉민)
    // ============================================
    private void playFloor6() {
        fullHealOnFloorStart();
        updateSaveFloor(6);
        showDialogue("floor6", "story");

        model.Stage resumePos6 = null;
        while (true) {
            ExploreResult result = stageManager.exploreFloor(6, gameView, resumePos6, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos6 = result.getLastPos();
                break;
            }

            if (result.getType() == ExploreResult.Type.NPC_ENCOUNTER) {
                BattleResult br = handleNpcEncounter(result);
                if (br == BattleResult.LOSE) return;
                resumePos6 = (br == BattleResult.FLEE) ? result.getLastPos() : result.getCurrentPos();
                continue;
            }

            String eid = result.getEventId();
            if (EventManager.CACHE_BATTLE.equals(eid)) {
                showDialogue("floor6", "cache_battle");
                NPC cache = stageManager.getNpcById("n12");
                logNpc("n12");  // 캐시 조우 로그
                Card cacheCard = createEnemyCard(cache.getNName());
                BattleResult cacheResult = battleManager.startCacheBattle(
                        hero, cache, cache.getNName(), playerCards, playerItems, cacheCard);

                if (cacheResult == BattleResult.LOSE) {
                    state = GameState.GAME_OVER;
                    return;
                } else if (cacheResult == BattleResult.WIN) {
                    showDialogue("floor6", "cache_battle_win");
                    Item drop = new ItemDAO().findById("i6");
                    if (drop != null) {
                        playerItems.add(drop);
                        gameView.showAcquire(drop.getIName());
                        logItem("i6");
                    }
                } else {
                    // ENEMY_FLED or FLEE
                    showDialogue("floor6", "cache_battle_lose");
                }
                result.getCurrentPos().consume();
            }
            resumePos6 = result.getCurrentPos();
        }

        model.Stage lastPos = resumePos6;

        // 봉민 보스전 (봉민: 민=받침ㄴ → pp=0)
        if (!bossBattleLoop(6, "floor6", "battle_boss_start", "battle_boss_win",
                "B006", "봉민", 130, 16, 20, 0, lastPos, "n8")) return;

        awardCardFromDB("c8");

        state = GameState.FLOOR_7;
    }

    // ============================================
    // 7층: 화산 (민중)
    // ============================================
    private void playFloor7() {
        if (heapReturnPos == null) {
            // 최초 진입
            fullHealOnFloorStart();
            updateSaveFloor(7);
            showDialogue("floor7", "story");
        }

        model.Stage resumePos = heapReturnPos;
        heapReturnPos = null; // 복귀 위치 초기화

        while (true) {
            ExploreResult result = stageManager.exploreFloor(7, gameView, resumePos, playerCards, playerItems);
            if (result == null || result.getType() == ExploreResult.Type.EXIT) {
                if (result != null) resumePos = result.getLastPos();
                break;
            }

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
                    heapReturnPos = result.getCurrentPos(); // 히든맵 탈출 후 복귀 위치 저장
                    result.getCurrentPos().consume();       // 힙 진입 타일 비활성화
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

        boolean loggedNpc = false;
        while (true) {
            showDialogue("floor7", startTag);
            if (!loggedNpc) { logNpc("n9"); loggedNpc = true; }
            NPC minjung = createBoss("B007", "민중", 150, 18, 24, 0); // 민중: 중=받침ㄱ → pp=0
            Card minjungCard = createEnemyCard("민중");
            BattleResult result = battleManager.startBattle(
                    hero, minjung, "민중", playerCards, playerItems, minjungCard);

            if (result == BattleResult.WIN) {
                showDialogue("floor7", winTag);
                awardCardFromDB("c9");
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

        int choice = gameView.showChoice("String Pool", "Old Generation");

        if (choice == 1) {
            // 혜진 루트: GC 전투 (GC는 죽지 않음 → 도망가야만 탈출 가능)
            hyejinRoute = true;
            showDialogue("heap", "hyejin_route");

            logNpc("n13");  // GC 조우 로그
            NPC gc = new NPC("M_GC", "GC",
                    "가비지 컬렉터. 더 이상 참조되지 않는 것들을 수거한다.",
                    80, false, 14, 20, 1, null);
            gc.setMinHealth(1); // HP 1 아래로 떨어지지 않음
            Card gcCard = createEnemyCard("GC");
            BattleResult gcResult = battleManager.startBattle(
                    hero, gc, "GC", playerCards, playerItems, gcCard);

            if (gcResult == BattleResult.LOSE) {
                showDialogue("heap", "gc_battle_lose");
                state = GameState.GAME_OVER;
                return;
            }
            // FLEE(도망) 또는 WIN(사실상 불가능) 모두 탈출 성공으로 처리
            showDialogue("heap", "battle_gc_run_away");
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
        fullHealOnFloorStart();

        // 히든맵 탈출 → 7층 탐색으로 복귀 (heapReturnPos에서 재개)
        state = GameState.FLOOR_7;
    }

    // ============================================
    // 8층: 딸깍이 최종전
    // ============================================
    private void playFinalFloor() {
        fullHealOnFloorStart();
        updateSaveFloor(8);
        String ddalkkagi_art = String.join("\n", storyManager.get("ascii_art", "ddalkkagi_image"));
        if (hyejinRoute) {
            showDialogue("floor8", "story_with_hyejin1");
            gameView.showAsciiArt(ddalkkagi_art);
            showDialogueContinue("floor8", "story_with_hyejin2");
        } else {
            showDialogue("floor8", "story_without_hyejin1");
            gameView.showAsciiArt(ddalkkagi_art);
            showDialogueContinue("floor8", "story_without_hyejin2");
        }

        logNpc("n11");  // 딸깍이 조우 로그
        // 딸깍이: HP 9999 / minHealth=1 (물리적으로 처치 불가), 강력한 공격
        NPC ddalkkagi = createBoss("B_FINAL", "딸깍이", 9999, 50, 80, 0);
        ddalkkagi.setMinHealth(1);
        Card ddalkagiCard = createEnemyCard("딸깍이");

        BattleResult result = battleManager.startFinalBattle(
                hero, ddalkkagi, "딸깍이", playerCards, playerItems, ddalkagiCard, hasBracket);

        if (result == BattleResult.BRACKET_WIN) {
            if (hyejinRoute) {
                showDialogue("floor8", "use_bracket_with_hyejin");
                triggerEnding("HYEJIN");
            } else {
                showDialogue("floor8", "use_bracket_without_hyejin");
                triggerEnding("NO_HYEJIN");
            }
        } else {
            // LOSE (브라켓 없이 사망 또는 브라켓 미사용)
            triggerEnding("DEFEAT");
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
        String endingEId = switch (endingType) {
            case "LOSE" -> {
                endingName = "바이트코드가 된 영균";
                yield "e1";
            }
            case "FALL" -> {
                endingName = "미안해 영균아";
                yield "e2";
            }
            case "SHORTCUT" -> {
                endingName = "당기시오";
                yield "e3";
            }
            case "BETRAYAL" -> {
                endingName = "엘리트 이선혁";
                yield "e4";
            }
            case "DEFEAT" -> {
                endingName = "수업시작";
                yield "e5";
            }
            case "GC" -> {
                endingName = "GC에게 수거된 영균";
                yield "e6";
            }
            case "NO_HYEJIN" -> {
                endingName = "살려줘 영균아";
                yield "e7";
            }
            case "HYEJIN" -> {
                endingName = "아 Tlqkf 꿈";
                yield "e8";
            }
            default -> {
                endingName = endingType;
                yield null;
            }
        };
        gameView.showEndingTitle(endingName);
        if (endingEId != null) {
            String img = new dao.CollectionDAO().findEImg(endingEId);
            gameView.showEndingImage(img);
        }
        gameView.waitForEnter();

        // 엔딩 DB 저장
        int tryNum = stageManager.getCurrentTryNum();
        if (endingEId != null && tryNum != -1) {
            new CollectionDAO().saveEnding(endingEId, tryNum);
        }

        // 승리 엔딩(SHORTCUT, HYEJIN, NO_HYEJIN)에서 엔딩 크레딧 출력
        if ("SHORTCUT".equals(endingType) || "HYEJIN".equals(endingType)
                || "NO_HYEJIN".equals(endingType)) {
            showDialogue("system", "ending_credit");
        }

        state = GameState.MAIN_MENU;
    }

    // ============================================
    // 대사 출력 유틸
    // ============================================
    private void showDialogue(String prefix, String tag) {
        List<String> lines = storyManager.get(prefix, tag);
        gameView.showDialogue(lines);
    }

    /** clearScreen 없이 현재 화면에 이어서 대사 출력 (아스키아트 직후 사용) */
    private void showDialogueContinue(String prefix, String tag) {
        List<String> lines = storyManager.get(prefix, tag);
        gameView.showDialogueContinue(lines);
    }

    // ============================================
    // Getters
    // ============================================
    public GameState getState() { return state; }
    public boolean hasSemicolon() { return hasSemicolon; }
    public boolean hasBracket() { return hasBracket; }
}
