import java.util.*;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.concurrent.locks.*;;

public class MBTA {

  // Creates an initially empty simulation
  public MBTA() { }

  private Map<String, List<String>> lines = new HashMap<>();
  private Map<String, List<String>> journeys = new HashMap<>();
  private Map<String, Integer> trainPos = new HashMap<>();
  private Map<String, Integer> passengerPos = new HashMap<>();
  private Map<String, Integer> trainDirs = new HashMap<>();
  private Map<String, String> stationTrains = new HashMap<>();
  private Map<String, List<String>> trainPassengers = new HashMap<>();

  public Lock passengerLock = new ReentrantLock();
  public Condition passengerCondition = passengerLock.newCondition();

  public Lock trainLock = new ReentrantLock();
  public Condition trainCondition = trainLock.newCondition();

  private class Config{
    private Map<String, List<String>> lines = new HashMap<>();
    private Map<String, List<String>> trips = new HashMap<>();
  }

  public boolean isMbtaEmpty(){
    System.out.println(passengerPos.size());
    return passengerPos.isEmpty();
  }

  public List<String> allLines(){
    List<String> lineList = new ArrayList<>();
    for(String line : lines.keySet()){
      lineList.add(line);
    }
    return lineList;
  }

  public List<String> allJourneys(){
    List<String> journeyList = new ArrayList<>();
    for(String journey : journeys.keySet()){
      journeyList.add(journey);
    }
    return journeyList;
  }

  public synchronized void addPassenger(String trainName, String passName){
    if(trainPassengers.containsKey(trainName) && Passenger.passengerExists(passName)){
      trainPassengers.get(trainName).add(passName);
      return;
    }
    throw new UnsupportedOperationException("the train did not exist, so we cannot add a passenger, or the passenger we are trying to add does not exist");
  }

  public synchronized void removePassenger(String trainName, String passName){
    if(trainPassengers.containsKey(trainName) && Passenger.passengerExists(passName) && trainPassengers.get(trainName).contains(passName) && passengerPos.containsKey(passName)){
      trainPassengers.get(trainName).remove(passName);
      return;
    }
    throw new UnsupportedOperationException("the train did not exist, so we cannot remove a passenger, or the passenger we are trying to remove does not exist in the list");
  }

  public synchronized void removePassengerFromWholeMbta(String passName){
    System.out.println("removing passenger from whole MBTA");
    passengerPos.remove(passName);
  }
  // Adds a new transit line with given name and stations
  public void addLine(String name, List<String> stations) {
    lines.put(name, stations);
    Train.make(name);
    setTrainDir(name, 1);
    setTrainPos(name, 0);
    trainPassengers.put(name, new ArrayList<>());

    for(int i = 0; i < stations.size(); i++){
      Station.make(stations.get(i));
      stationTrains.put(stations.get(i), null);
    }
  }

  // Adds a new planned journey to the simulation
  public void addJourney(String name, List<String> stations) {
    journeys.put(name, stations);
    setPassengerPos(name, 0);
    Passenger.make(name);
  }

  public List<String> getJourney(String passName){
    if(journeys.containsKey(passName)){
      return journeys.get(passName);
    }
    throw new UnsupportedOperationException("This passenger does not exist");
  }

  public List<String> getLine(String name){
    if(lines.containsKey(name)){
      return lines.get(name);
    }
    throw new UnsupportedOperationException("this train line does not exist");
  }

  public Integer getTrainPos(String train){
    if(trainPos.containsKey(train)){
      return trainPos.get(train);
    }
    throw new UnsupportedOperationException("The train did not exist, so we cannot get the position.");
  }

  public synchronized void setTrainPos(String train, Integer position){
    trainPos.put(train, position);
  }

  public int getTrainDir(String train){
    if(trainDirs.containsKey(train)){
      return trainDirs.get(train);
    }
    throw new UnsupportedOperationException("Train did not exist, so we cannot get the direction.");
  }

  public synchronized void setTrainDir(String train, Integer direction){
    trainDirs.put(train, direction);
  }

  public String getStationTrain(String station){
    if(stationTrains.containsKey(station)){
      return stationTrains.get(station);
    }
    throw new UnsupportedOperationException("the station does not exist");
  }

  public synchronized void setStationTrain(String station, String train){
    if(!stationTrains.containsKey(station) || !allLines().contains(train)){
      System.out.println("station or train did not exist");
      return;
    }
    if(stationTrains.containsKey(station) && getStationTrain(station) != null){
      System.out.println("station is occupied");
      return;
    }
    stationTrains.put(station, train);

  }

  public String getTrainStation(String train){
    return lines.get(train).get(getTrainPos(train));
  }

  public synchronized void moveStationTrain(String initialStation, String destinationStation, String train){
    releaseStation(initialStation);
    stationTrains.put(destinationStation, train);
  }

  public synchronized void releaseStation(String station){
    stationTrains.put(station, null);
  }

  public int getPassengerPos(String passName){
    if(passengerPos.containsKey(passName)){
      return passengerPos.get(passName);
    }
    throw new UnsupportedOperationException("the passenger does not exist, so we cannot get the passengerPos");
  }

  public synchronized void setPassengerPos(String passName, Integer position){
    passengerPos.put(passName, position);
  }

  // Return normally if initial simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkStart() {
    for(String trainN : lines.keySet()){
      if(trainPos.get(trainN) != 0){
        throw new UnsupportedOperationException("train not at start");
      }
    }
    for(String passengerN : journeys.keySet()){
      if(passengerPos.get(passengerN) != 0){
        throw new UnsupportedOperationException("passenger not at start");
      }
    }
  }

  // Return normally if final simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkEnd() {
    System.out.println(isMbtaEmpty());
    if(!isMbtaEmpty()){
      throw new UnsupportedOperationException("someone didnt get off the train at the end of the simulation");
    }
  }

  // reset to an empty simulation
  public void reset() {
    lines.clear(); journeys.clear();
    Passenger.clear();
    Train.clear();
    Station.clear();
    trainPos.clear();
    passengerPos.clear();
    trainDirs.clear();
    stationTrains.clear();
    trainPassengers.clear();
  }

  // adds simulation configuration from a file
  public void loadConfig(String filename){
    try{
      String jsonContent = new String(Files.readAllBytes(Paths.get(filename)));
      Gson gson = new Gson();
      Config c = new Config();
      c = gson.fromJson(jsonContent, Config.class);

      for(Map.Entry<String, List<String>> line : c.lines.entrySet()){
        addLine(line.getKey(), line.getValue());
      }
      for(Map.Entry<String, List<String>> trip : c.trips.entrySet()){
        addJourney(trip.getKey(), trip.getValue());
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
