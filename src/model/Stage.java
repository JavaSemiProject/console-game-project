package model;


public class Stage {
  private int stageId;
  private String stageName;
  private int row;
  private String column;
  private int fLevel;
  private String eventId;  // null이면 일반 타일, 값이 있으면 이벤트 타일

  public Stage(int stageId, String stageName, int row, String column, int fLevel){
    this.stageId = stageId;
    this.stageName = stageName;
    this.row = row;
    this.column = column;
    this.fLevel = fLevel;
    this.eventId = null;
  }

  public int getStageId() {return stageId;}
  public String getStageName(){return stageName;}
  public void setStageName(String StageName){this.stageName = StageName;}
  public String getPosition() {return row + "-" + column;}
  public int getRow(){return row;}
  public String getColumn(){return column;}
  public int getFlevel(){return fLevel;}
  public String getEventId() {return eventId;}
  public void setEventId(String eventId) {this.eventId = eventId;}

  @Override
  public String toString(){
    return String.format("Stage{id=%d, name = '%s'}", stageId, stageName);
  }

}
