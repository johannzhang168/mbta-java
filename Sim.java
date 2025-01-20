import java.io.*;
import java.util.*;

public class Sim {

  public static void run_sim(MBTA mbta, Log log) {
    Map<String, Thread> allThreads = new HashMap<>();

    for(String train : mbta.allLines()){
      allThreads.put(train, new TrainThread(mbta, train, log));
    }

    for(String pass : mbta.allJourneys()){
      allThreads.put(pass, new PassengerThread(mbta, pass, log));
    }

    for(Thread th : allThreads.values()){
      th.start();
    }
    try{
      for(Thread th : allThreads.values()){
        th.join();
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: ./sim <config file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig(args[0]);

    Log log = new Log();

    run_sim(mbta, log);

    String s = new LogJson(log).toJson();
    PrintWriter out = new PrintWriter("log.json");
    out.print(s);
    out.close();

    mbta.reset();
    mbta.loadConfig(args[0]);
    Verify.verify(mbta, log);
  }
}
