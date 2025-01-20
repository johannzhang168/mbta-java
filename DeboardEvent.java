import java.util.*;

public class DeboardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public DeboardEvent(Passenger p, Train t, Station s) {
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    if (o instanceof DeboardEvent e) {
      return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(p, t, s);
  }
  public String toString() {
    return "Passenger " + p + " deboards " + t + " at " + s;
  }
  public List<String> toStringList() {
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    String trainStation = mbta.getLine(t.toString()).get(mbta.getTrainPos(t.toString()));
    List<String> passJourney = mbta.getJourney(p.toString());
    int passengerPos = mbta.getPassengerPos(p.toString());

    if(!trainStation.equals(s.toString())){
      throw new UnsupportedOperationException("the train station and the deboard station are not the same");
    }
    if(!trainStation.equals(passJourney.get(passengerPos + 1))){
      System.out.println(t.toString()+ " " + trainStation);
      System.out.println(p.toString()+ " " +passJourney.get(passengerPos));
      throw new UnsupportedOperationException("The train station does not match the passenger station");
    }
    mbta.setPassengerPos(p.toString(), passengerPos + 1);
    mbta.removePassenger(t.toString(), p.toString());
    System.out.println("pAssenger Pos: " +p.toString() + " " + mbta.getPassengerPos(p.toString()));
    if(mbta.getPassengerPos(p.toString()) >= passJourney.size() - 1){
      mbta.removePassengerFromWholeMbta(p.toString());
    }
  }
}
