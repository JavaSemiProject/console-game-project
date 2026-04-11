package model;


public class Stage {
  private String stageId;
  private String stageName;
  private int row;
  private String column;
  private int fLevel;
  private String s_type;
  private Double s_prob;

  public Stage(String stageId, String stageName, int row, String column, int fLevel, String s_type, Double s_prob){
    this.stageId = stageId;
    this.stageName = stageName;
    this.row = row;
    this.column = column;
    this.fLevel = fLevel;
    this.s_type = s_type;
    this.s_prob = s_prob;
  }

  public String getStageId() {return stageId;}
  public String getStageName(){return stageName;}
  public void setStageName(String StageName){this.stageName = StageName;}
  public String getPosition() {return row + "-" + column;}
  public int getRow(){return row;}
  public String getColumn(){return column;}
  public int getFlevel(){return fLevel;}
  public String getS_type() {return s_type;}
  public Double getS_prob() {return s_prob;}
  @Override
  public String toString(){
    return String.format("Stage{id=%s, name='%s'}", stageId, stageName);
  }

}
