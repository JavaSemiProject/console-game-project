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
    // ✅ stageDAO 대신 stageManager.findStageStart() 사용
    Stage startStage = stageManager.findStageStart(floorLevel);
    if (startStage == null) return null;

    int tryNum = saveDAO.createSave(startStage.getStageName());
    if (tryNum == -1) return null;

    System.out.println("[SaveManager] 새 게임 저장: "
        + startStage.getStageName()
        + " / tryNum=" + tryNum
        + " / saved=true");

    return new Save(
        "player1",                                 // 1. String t_id
        startStage.getStageName(),                 // 2. String lId  ✅ 순서 교체
        tryNum,                                    // 3. int tryNum  ✅ 순서 교체
        new Timestamp(System.currentTimeMillis()), // 4. Timestamp t_time
        new ArrayList<>(),                         // 5. List<Card>
        new ArrayList<>()                          // 6. List<Item>
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

  public boolean resetAllSaves() {
    boolean deleted = saveDAO.deleteAllGameData();  // 또는 deleteAllSaves()
    if (deleted) {
      System.out.println("[SaveManager] 모든 세이브 데이터 초기화 완료");
    }
    return deleted;
  }
}