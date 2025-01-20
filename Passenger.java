import java.util.*;

public class Passenger extends Entity {
  private Passenger(String name) { super(name); }
  private static Map<String, Passenger> passengerMap = new HashMap<>();

  public static Passenger make(String name) {
    if(passengerMap.containsKey(name)){
      return passengerMap.get(name);
    }
    else{
      Passenger p = new Passenger(name);
      passengerMap.put(name, p);
      return p;
    }
  }
  // public void setPosition(int pos){
  //   this.position = pos;
  // }
  public static boolean passengerExists(String passName){
    return passengerMap.containsKey(passName);
  }

  public static void clear(){
    passengerMap.clear();
  }
}
