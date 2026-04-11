package manager;

import dao.SaveDAO;
import dao.StageDAO;
import model.Card;
import model.Item;
import model.Save;
import model.Stage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
  private StageManager stageManager;
  private StageDAO stageDAO;
  private SaveDAO saveDAO;

  public SaveManager(StageManager stageManager, StageDAO stageDAO, SaveDAO saveDAO) {
    this.stageManager = stageManager;
    this.stageDAO = stageDAO;
    this.saveDAO = saveDAO;
  }

  /**
   * 새 게임 시작 시 호출
   * - start 스테이지 찾아서 DB INSERT + s_id UPDATE
   */
  public Save createNewSave(int floorLevel) {
    Stage startStage = findStartStage(floorLevel);
    if (startStage == null) {
      System.err.println("[SaveManager] " + floorLevel + "층의 start 스테이지가 없습니다.");
      return null;
    }

    // 1. INSERT → tryNum 발급
    int tryNum = saveDAO.createSave();
    if (tryNum == -1) {
      System.err.println("[SaveManager] 세이브 생성 실패.");
      return null;
    }

    // 2. s_id UPDATE
    boolean saved = saveDAO.updateStage(startStage.getStageName(), tryNum);
    System.out.println("[SaveManager] 새 게임 저장: "
        + startStage.getStageName()
        + " / tryNum=" + tryNum
        + " / saved=" + saved);

    return new Save(
        startStage.getStageName(),
        tryNum,
        new Timestamp(System.currentTimeMillis()),
        new ArrayList<>(),
        new ArrayList<>()
    );
  }

  /**
   * 이어하기: 가장 최근 세이브 불러오기
   */
  public Save loadLatestSave() {
    return saveDAO.findLatest();
  }

  /**
   * 세이브 포인트 업데이트
   * - StageManager에서 start 지점 도달 시 호출
   */
  public boolean updateSaveStage(String stageId, int tryNum) {
    return saveDAO.updateStage(stageId, tryNum);
  }

  /**
   * 층에서 s_type = 'start' 인 스테이지 찾기
   */
  private Stage findStartStage(int floorLevel) {
    List<Stage> stages = stageDAO.findByFloorLevel(floorLevel);
    return stages.stream()
        .filter(s -> "start".equals(s.getS_type()))
        .findFirst()
        .orElse(null);
  }
}