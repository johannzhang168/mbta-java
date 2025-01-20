import java.util.*;

public class MoveEvent implements Event {
  public final Train t; public final Station s1, s2;
  public MoveEvent(Train t, Station s1, Station s2) {
    this.t = t; this.s1 = s1; this.s2 = s2;
  }
  public boolean equals(Object o) {
    if (o instanceof MoveEvent e) {
      return t.equals(e.t) && s1.equals(e.s1) && s2.equals(e.s2);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(t, s1, s2);
  }
  public String toString() {
    return "Train " + t + " moves from " + s1 + " to " + s2;
  }
  public List<String> toStringList() {
    return List.of(t.toString(), s1.toString(), s2.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    List<String> lines = mbta.getLine(t.toString());

    int trainPos = mbta.getTrainPos(t.toString());

    if(!lines.get(trainPos).equals(s1.toString())){
      System.out.println(s1.toString());
      System.out.println(lines.get(trainPos));
      throw new UnsupportedOperationException("The train is not starting at the correct position");
    }

    if(trainPos == 0){
      mbta.setTrainDir(t.toString(), 1);
    }else if(trainPos == lines.size() - 1){
      mbta.setTrainDir(t.toString(), -1);
    }
    int direction = mbta.getTrainDir(t.toString());
    if(!lines.get(trainPos + direction).equals(s2.toString())){
      throw new UnsupportedOperationException("The train is not at the correct ending position");
    }
    if(mbta.getStationTrain(s2.toString()) != null){
      throw new UnsupportedOperationException("There is a train at destination station");
    }

    mbta.moveStationTrain(s1.toString(), s2.toString(), t.toString());
    mbta.setTrainPos(t.toString(), trainPos + direction);
    
  }

}
