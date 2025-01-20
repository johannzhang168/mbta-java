import java.util.*;

public class BoardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public BoardEvent(Passenger p, Train t, Station s) {
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    if (o instanceof BoardEvent e) {
      return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(p, t, s);
  }
  public String toString() {
    return "Passenger " + p + " boards " + t + " at " + s;
  }
  public List<String> toStringList() {
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    String trainStation = mbta.getLine(t.toString()).get(mbta.getTrainPos(t.toString()));
    List<String> passJourney = mbta.getJourney(p.toString());
    int passengerPos = mbta.getPassengerPos(p.toString());
    if(!trainStation.equals(s.toString())){
      System.out.println(trainStation);
      System.out.println(s.toString());
      throw new UnsupportedOperationException("the train station and the board station are not the same");
    }
    if(!trainStation.equals(passJourney.get(passengerPos))){
      System.out.println(t.toString() + " " + trainStation);
      System.out.println(p.toString() + " " + passJourney.get(passengerPos));
      throw new UnsupportedOperationException("The train station does not match the passenger station, so they cannot board the train");
    }
    mbta.addPassenger(t.toString(), p.toString());
  }
}
