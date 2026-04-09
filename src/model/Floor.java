package model;

import java.util.ArrayList;
import java.util.List;

public class Floor {
private int floorLevel;
private List<Stage> stages = new ArrayList<>();
private Floor nextFloor;


public Floor(int floorLevel){
  this.floorLevel = floorLevel;
}

public static Floor buildChain(int topLevel){
  Floor first = new Floor(topLevel);
  Floor current = first;

  for(int i = topLevel - 1; i >=1; i--){
   Floor next = new Floor(i);
   current.setNextFloor(next);
   current = next;
  }

  return first;
}


public boolean isLastFloor() {return nextFloor == null;}
public int getFloorLevel() {return floorLevel;}
public List<Stage> getStages() {return stages;}
public void addStage(Stage stage) {stages.add(stage);}
public Floor getNextFloor() { return nextFloor; }
public void setNextFloor(Floor nextFloor) { this.nextFloor = nextFloor; }

public Stage getStageById(int stageId) {
  return stages.stream()
               .filter(s->s.getStageId() == stageId)
               .findFirst()
               .orElse(null);
}
  @Override
  public String toString() {
    return String.format("Floor{level=%d, stages=%d}", floorLevel, stages.size());
  }
}
