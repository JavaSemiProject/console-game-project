package model;

public class Stage {
private int stageId;
private String stageName;
private int row;
private String column;
private int fLevel;

public Stage(int stageId, String stageName, int row, String column, int fLevel){
  this.stageId = stageId;
  this.stageName = stageName;
  this.row = row;
  this.column = column;
  this.fLevel = fLevel;
}

public int getStageId() {return stageId;}
public String getStageName(){return stageName;}
public void setStageName(String StageName){this.stageName = StageName;}
public String getPosition() {return row + "-" + column;}
public int getRow(){return row;}
public String getColumn(){return column;}
public int getFlevel(){return fLevel;}

  @Override
  public String toString(){
    return String.format("Stage{id=%d, name = '%s'}", stageId, stageName);
  }

}





