import java.util.*;

public class Train extends Entity {
  private Train(String name) { super(name); }
  private static Map<String, Train> trainMap = new HashMap<>();

  public static Train make(String name) {
    if(trainMap.containsKey(name)){
      return trainMap.get(name);
    }
    else{
      Train t = new Train(name);
      trainMap.put(name, t);
      return t;
    }
  }
  public static void clear(){
    trainMap.clear();
  }
}
