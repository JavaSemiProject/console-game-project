package manager;

import dao.SaveDAO;
import dao.StageDAO;
import model.Card;
import model.Floor;
import model.Item;
import model.NPC;
import model.Stage;
import view.GameView;
import dao.NPCDAO;

import java.util.ArrayList;
import java.util.List;

public class StageManager {

  /** 맵 탐색 결과 */
  public static class ExploreResult {
    public enum Type { EXIT, EVENT, NPC_ENCOUNTER }

    private final Type type;
    private final String eventId;   // EVENT일 때만 유효
    private final Stage lastPos;    // 출구/이벤트 직전 위치 (도망 복귀용)
    private final Stage currentPos; // 이벤트 발생 위치 (탐색 재개용)

    public ExploreResult(Type type, String eventId, Stage lastPos, Stage currentPos) {
      this.type = type;
      this.eventId = eventId;
      this.lastPos = lastPos;
      this.currentPos = currentPos;
    }

    public Type getType() { return type; }
    public String getEventId() { return eventId; }
    public Stage getLastPos() { return lastPos; }
    public Stage getCurrentPos() { return currentPos; }
  }

  private List<Stage> stageList = new ArrayList<>();
  private Floor currentFloor;
  private NPCDAO npcDAO = new NPCDAO();

  // StageDAO에서 DB 읽어오기
  public StageManager() {
    StageDAO stageDAO = new StageDAO();
    this.stageList = new ArrayList<>(stageDAO.findAll());
  }


  private int currentTryNum = -1;
  private SaveDAO saveDAO = new SaveDAO();

  // 층별 보스 n_id 매핑
  private String getBossIdForFloor(int floorLevel) {
    switch (floorLevel) {
      case 2: return "n4";  // 미주
      case 3: return "n5";  // 솔민
      case 4: return "n6";  // 제석
      case 5: return "n7";  // 수지
      case 6: return "n8";  // 봉민
      case 7: return "n9";  // 민중
      default: return null;
    }
  }

  public NPC getBossForFloor(int floorLevel) {
    String bossId = getBossIdForFloor(floorLevel);
    if (bossId == null) return null;
    return npcDAO.findById(bossId);
  }


  // 테스트 전용 -DB 대체로 임시 사용 -> DB 연동되면 삭제 OR 주석
  /*public void generateStages() {
    String[] columns = {"a", "b", "c", "d", "e"};
    int idCounter = 1;

    for (int f = 1; f <= 8; f++) {
      for (int r = 1; r <= 5; r++) {
        for (String c : columns) {
          // 이름 규칙: column_row (예: a_1, b_2...)
          String stageName = c + "_" + r;
          stageList.add(new Stage(idCounter++, stageName, r, c, f));
        }
      }
    }
    System.out.println("시스템: 200개의 스테이지 객체가 생성되었습니다.");
  }
*/

  /** s_type → EventManager 상수 매핑 */
  private String mapTypeToEventId(String sType) {
    switch (sType) {
      case "event_comment":   return EventManager.COMMENT_BRANCH;
      case "event_semicolon": return EventManager.SEMICOLON_FIND;
      case "event_door":      return EventManager.SEMICOLON_DOOR;
      case "event_betrayal":  return EventManager.SUSPECT_SUNHYUK;
      case "event_cache":     return EventManager.CACHE_BATTLE;
      case "event_heap":      return EventManager.HEAP_ENTRY;
      default: return null;
    }
  }

  /** 확률 체크가 필요한 타입인지 */
  private boolean needsProbCheck(String sType) {
    return sType != null && (sType.equals("npc_i") || sType.startsWith("event_"));
  }

  /** NPC ID로 NPC 조회 */
  public NPC getNpcById(String nId) {
    return npcDAO.findById(nId);
  }
/*
  // 테스트 전용-2
  public void printFullMap() {
    Floor tempFloor = currentFloor;
    String[] columns = {"a", "b", "c", "d", "e"};

    while (tempFloor != null) {
      System.out.println("\n[" + tempFloor.getFloorLevel() + "층]");
      System.out.println("  1\t2\t3\t4\t5"); // 상단 숫자 라벨

      for (String col : columns) {
        System.out.print(col + " "); // 좌측 알파벳 라벨
        for (int row = 1; row <= 5; row++) {
          Stage s = getStageAt(row, col, tempFloor);
          if (s != null) {
            System.out.print(s.getStageName() + "\t");
          } else {
            System.out.print(" \t");
          }
        }
        System.out.println();
      }
      tempFloor = tempFloor.getNextFloor();
    }
  }
*/


  // Floor 구성(8→7→..→1)
  public void buildFloorChain(int topLevel) {
    currentFloor = Floor.buildChain(topLevel);

    // 각 층에 해당 스테이지 추가
    for (Stage stage : stageList) {
      Floor targetFloor = findFloor(stage.getFlevel());
      if (targetFloor != null) {
        targetFloor.addStage(stage);
      }
    }
  }

  // ✅ GameManager에서 tryNum 주입
  public void setCurrentTryNum(int tryNum) {
    this.currentTryNum = tryNum;
  }

  public int getCurrentTryNum() {
    return currentTryNum;
  }


  // 특정 층 탐색
  private Floor findFloor(int level) {
    Floor temp = currentFloor;
    while (temp != null) {
      if (temp.getFloorLevel() == level) return temp;
      temp = temp.getNextFloor();
    }
    return null;
  }

/*
  // 현재 스테이지 조회
  public Stage getCurrentStage() {
    if (currentFloor == null || currentFloor.getStages().isEmpty()) return null;
    return currentFloor.getStages().stream()
        .filter(s -> s.getStageName().equals("start"))
        .findFirst()
        .orElse(currentFloor.getStages().get(0));
  }
*/

  // 다음 층으로 이동
  public Floor moveToNextFloor() {
    if (currentFloor != null && currentFloor.getNextFloor() != null) {
      currentFloor = currentFloor.getNextFloor();
    }
    return currentFloor;
  }

  // 특정 위치 스테이지 조회 (row, column 기반)
  public Stage getStageAt(int row, String column, Floor floor) {
    if (floor == null) return null;
    return floor.getStages().stream()
        .filter(s -> s.getRow() == row && s.getColumn().equals(column))
        .findFirst()
        .orElse(null);
  }

  // 층에서 `start` 타입 스테이지(= 자동 세이브 지점, 보통 a1) 찾기
  public Stage findStageStart(int floorLevel) {
    Floor floor = findFloor(floorLevel);
    if (floor == null) return null;

    return floor.getStages().stream()
        .filter(s -> "start".equals(s.getS_type()) && s.getFlevel() == floorLevel)
        .findFirst()
        .orElse(null);
  }

  // ============================================
  // 맵 탐색 루프 (도착점 a_5 도달 또는 이벤트 타일 도달 시 종료)
  // startPos가 null이면 e_1에서 시작
  // ============================================
  public ExploreResult exploreFloor(int floorLevel, GameView gameView, Stage startPos,
                                    List<Card> cards, List<Item> items) {
    Floor floor = findFloor(floorLevel);
    System.out.println("[DEBUG] exploreFloor(" + floorLevel + ") floor=" + floor
        + " stageCount=" + (floor != null ? floor.getStages().size() : 0));
    if (floor == null) return null;

    Stage currentPos;
    if (startPos != null) {
      currentPos = startPos;
    } else {
      currentPos = floor.getStages().stream()
          .filter(s -> "start".equals(s.getS_type()))
          .findFirst()
          .orElse(getStageAt(1, "e", floor));
    }
    System.out.println("[DEBUG] currentPos=" + (currentPos != null
        ? currentPos.getColumn() + "_" + currentPos.getRow() + " type=" + currentPos.getS_type()
        : "null"));
    if (currentPos == null) return null;

    Stage prevPos = currentPos;
    autoSaveIfStart(currentPos, gameView);

    while (true) {
      gameView.showMap(floor, currentPos);
      String input = gameView.getMovementInput();

      if ("i".equals(input)) {
        gameView.showInventoryView(cards, items);
        continue;
      }

      int nextRow = currentPos.getRow();
      char nextCol = currentPos.getColumn().charAt(0);

      switch (input) {
        case "w": nextRow--; break;
        case "s": nextRow++; break;
        case "a": nextCol--; break;
        case "d": nextCol++; break;
        default: continue;
      }

      Stage next = getStageAt(nextRow, String.valueOf(nextCol), floor);
      if (next == null) {
        gameView.showMapAlert("더 이상 갈 수 없습니다.");
        continue;
      }
      if ("w".equals(next.getS_type())) {
        gameView.showMapAlert("길이 막혀있다.");
        continue;
      }

      prevPos = currentPos;
      currentPos = next;
      autoSaveIfStart(currentPos, gameView);

      // finish 도달 시 종료
      if ("finish".equals(currentPos.getS_type())) {
        gameView.showMessage("\n...");
        gameView.waitForEnter();
        Stage nextFloorStart = findStageStart(floorLevel + 1);
        if (nextFloorStart != null && currentTryNum != -1) {
          boolean saved = saveDAO.updateStage(nextFloorStart.getStageName(), currentTryNum);
          gameView.showMessage(saved
              ? "[AutoSave] ✔ 저장 완료: " + nextFloorStart.getStageName()
              : "[AutoSave] ✘ 저장 실패");
        }
        return new ExploreResult(ExploreResult.Type.EXIT, null, prevPos, currentPos);
      }

      // 이벤트/NPC 타일 처리
      String sType = currentPos.getS_type();
      if (!currentPos.isConsumed() && needsProbCheck(sType)) {
        currentPos.incrementVisit();

        boolean trigger;
        if ("event_semicolon".equals(sType)) {
          // 세미콜론 이벤트: 3회 방문 시 발동
          trigger = currentPos.getVisitCount() >= 3;
        } else if ("event_door".equals(sType) || "event_betrayal".equals(sType) || "event_heap".equals(sType)) {
          // 스토리 이벤트: 항상 발동
          trigger = true;
        } else {
          // 그 외(event_comment, event_cache, npc_i): s_prob 확률 기반
          trigger = Math.random() * 100 < currentPos.getS_prob();
        }

        if (trigger) {
          if ("npc_i".equals(sType)) {
            return new ExploreResult(ExploreResult.Type.NPC_ENCOUNTER,
                currentPos.getNId(), prevPos, currentPos);
          }
          String eventId = mapTypeToEventId(sType);
          if (eventId != null) {
            return new ExploreResult(ExploreResult.Type.EVENT,
                eventId, prevPos, currentPos);
          }
        }
      }
    }
  }

  /** startPos 지정, 인벤토리 없이 탐색 (보스 도망 복귀 등 내부용) */
  public ExploreResult exploreFloor(int floorLevel, GameView gameView, Stage startPos) {
    return exploreFloor(floorLevel, gameView, startPos, new ArrayList<>(), new ArrayList<>());
  }

  public Floor getFloor(int level) {
    return findFloor(level);
  }

  private void autoSaveIfStart(Stage stage, GameView gameView) {
    if (!"start".equals(stage.getS_type())) return;
    if (currentTryNum == -1) {
      System.out.println("[AutoSave] tryNum 없음 - 저장 스킵");
      return;
    }

    boolean saved = saveDAO.updateStage(stage.getStageName(), currentTryNum);
    gameView.showMessage(saved
        ? "[AutoSave] ✔ 저장 완료: " + stage.getStageName()
        : "[AutoSave] ✘ 저장 실패");
  }

/*
  // 스테이지 추가
  public void addStage(Stage stage) {
    stageList.add(stage);
  }

  public Floor getCurrentFloor() { return currentFloor; }
  public List<Stage> getStageList() { return stageList; }

*/

/*
  // ==========================================
// [테스트 개발 전용] 화면 인게임 루프
// ==========================================
  public void testGameLoop() {
    java.util.Scanner sc = new java.util.Scanner(System.in);

    // 1. 시작 스테이지 '객체'를 직접 가져와서 변수에 담기 (e_1 지점)
    Stage currentPos = getStageAt(1, "e", currentFloor);

    // ✨ [에러 해결!] alertMsg 변수를 여기서 선언합니다.
    String alertMsg = "";

    while (currentFloor != null && currentPos != null) {
      // 화면 초기화 및 커서 홈 위치 이동
      System.out.print("\033[2J\033[H");
      System.out.flush();

      // [상단 UI]
      System.out.println("╔══════════════════════════════════════╗");
      // System.out.printf("║ %d층 - 현재 위치 : %s_%d                  ║%n",
      //  currentFloor.getFloorLevel(), currentPos.getColumn(), currentPos.getRow());
      System.out.println("╠══════════════════════════════════════╣");
      //System.out.println("║      1   2   3   4   5               ║");
/*
      // [중단 맵 그리기]
      char[] rowLabels = {'a', 'b', 'c', 'd', 'e'};
      for (char r : rowLabels) {
        System.out.print("║  " + r + "  ");
        for (int c = 1; c <= 5; c++) {
          Stage target = getStageAt(c, String.valueOf(r), currentFloor);
          if (target != null && target.equals(currentPos)) {
            System.out.print("●   ");
          } else {
            System.out.print("□   ");
          }
        }
        System.out.println("             ║");
      }
*/
 /*     // [하단 UI]
      System.out.println("╠══════════════════════════════════════╣");
      System.out.println(" 조작: w(상) a(좌) s(하) d(우) + Enter ");

      // ✨ [추가] 화면에 경고 메시지를 보여주는 줄
      if (!alertMsg.isEmpty()) {
        System.out.printf("⚠️ %-33s %n", alertMsg);
        // alertMsg = ""; // 메시지를 한 번만 보여주고 싶다면 주석을 해제하세요.
      } else {
        System.out.println("                                      ");
      }

      //System.out.println(" 목표: a_5 도달 시 다음 층으로 이동!  ");
      System.out.println("╚══════════════════════════════════════╝");
      System.out.print("이동 방향을 입력하세요: ");

      String input = sc.nextLine().toLowerCase();

      // 2. 이동 로직 계산
      int nextX = currentPos.getRow();
      char nextY = currentPos.getColumn().charAt(0);

      // 입력된 키에 따라 next 값을 먼저 계산
      if (input.equals("w")) nextY--;
      else if (input.equals("s")) nextY++;
      else if (input.equals("a")) nextX--;
      else if (input.equals("d")) nextX++;

      // 3. 실제 객체 존재 여부 확인 및 경계선 체크
      // 5x5 범위 (a~e, 1~5) 안에 있는지 확인
      if (nextY >= 'a' && nextY <= 'e' && nextX >= 1 && nextX <= 5) {
        Stage nextStage = getStageAt(nextX, String.valueOf(nextY), currentFloor);
        if (nextStage != null) {
          currentPos = nextStage; // 성공: 실제 위치 갱신
          alertMsg = "";          // 경고 초기화
        }
      } else {
        // ⚠️ 테두리 밖으로 나가려고 했을 때 (나열하신 모든 좌표에 자동 적용)
        if (input.equals("w")) alertMsg = "북쪽 끝입니다. 더 이상 갈 수 없습니다.";
        else if (input.equals("s")) alertMsg = "남쪽 끝입니다. 더 이상 갈 수 없습니다.";
        else if (input.equals("a")) alertMsg = "서쪽 끝입니다. 더 이상 갈 수 없습니다.";
        else if (input.equals("d")) alertMsg = "동쪽 끝입니다. 더 이상 갈 수 없습니다.";
      }

      // 4. [특수 이벤트] 2층 a_5 도달 시 '딸깍이' 조우
      if (currentFloor.getFloorLevel() == 2 &&
          currentPos.getColumn().equals("a") && currentPos.getRow() == 5) {
        System.out.print("\033[2J\033[H");
        String bossMsg = "\n>> '딸깍이'가 당신의 앞을 가로막습니다.\n"
            + ">> \"여기까지 오다니... 하지만 여기서 끝이다.\"\n\n"
            + "━━━━━━━━━━━━━ 테스트 종료 ━━━━━━━━━━━━━\n";
        for (char c : bossMsg.toCharArray()) {
          System.out.print(c);
          try { Thread.sleep(40); } catch (Exception e) {}
        }
        return;
      }

      // 5. 일반적인 층 이동 체크
      if (currentPos.getColumn().equals("a") && currentPos.getRow() == 5) {
        System.out.print("\033[2J\033[H");
        System.out.println(">> 계단을 발견했습니다! 다음 층으로 내려갑니다... <<");
        try { Thread.sleep(1500); } catch (Exception e) {}
        moveToNextFloor();
        if (currentFloor != null) {
          currentPos = getStageAt(1, "e", currentFloor);
          alertMsg = ""; // 층 이동 시 메시지 초기화
        }
      }
    }
  }
  */

  //테스트용 StageManager 메인
  public static void main(String[] args) {
    StageManager manager = new StageManager();

    // 1. 스테이지 데이터 생성 (DB 역할 대체)
    //manager.generateStages();

    // 2. 8층 구조 생성 및 데이터 배분
    manager.buildFloorChain(8);
/*
    // 3. 전체 맵 출력 (5x5 출력)
    manager.printFullMap();
*/
    // 3. 테스트용 인게임 화면 실행!
   // manager.testGameLoop();
  }
}