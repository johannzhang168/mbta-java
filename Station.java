

import java.util.*;

public class Station extends Entity {
  private Station(String name) { super(name); }

  private static Map<String, Station> stationMap = new HashMap<>();
  public static Station make(String name) {
    if(stationMap.containsKey(name)){
      return stationMap.get(name);
    }
    else{
      Station s = new Station(name);
      stationMap.put(name, s);
      return s;
    }
  }

  public static void clear(){
    stationMap.clear();
  }
}
