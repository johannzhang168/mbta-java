import java.util.*;
public class PassengerThread extends Thread {
    private MBTA mbta;
    private String name;
    private Log log;
    private String onTrain = null;
    private boolean notAtEnd = true;

    PassengerThread(MBTA mbta, String name, Log log){
        this.mbta = mbta;
        this.name = name;
        this.log = log;
    }

    public void run(){
        while(notAtEnd){ //while the passenger is not at the end of their journey
            int pos = mbta.getPassengerPos(name);
            List<String> journeys = mbta.getJourney(name);
            String station = journeys.get(pos);
            String nextStation = "";
            if(journeys.size()-1 <= pos){
                mbta.removePassengerFromWholeMbta(name);
                notAtEnd = false;
                continue; //if the passenger is already at the end of their journey, then we remove them from the whole mbta
            }
            nextStation = journeys.get(pos + 1);

            if(onTrain == null){ //if they are not on a train:
                System.out.println("Not on Train");
                mbta.passengerLock.lock(); //give passenger lock so they can board when train comes
                List<String> trains = new ArrayList<>();
                for(String t : mbta.allLines()){ //go through every train in the lines, then go and get all the possible trains that the passenger could board to get to their final destiation
                    List<String> line = mbta.getLine(t);
                    if(line.contains(station) && line.contains(nextStation)){ //if the train goes to the station that the passenger is at and the train goes to the next station in the passengers journey at some point, add it to the list
                        trains.add(t);
                    }
                }
                while(!trains.contains(mbta.getStationTrain(station))){ //while the trains arriving aren't in the possible trains list, wait and dont board
                    try{
                        mbta.passengerCondition.await();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                onTrain = mbta.getStationTrain(station); //board the train
                mbta.addPassenger(onTrain, name);
                log.passenger_boards(Passenger.make(name), Train.make(onTrain), Station.make(station)); //board log

                mbta.passengerCondition.signalAll(); //release the passenger lock so other passengers can board the train
                mbta.passengerLock.unlock();
            }
            else{ //if they are on a train
                mbta.passengerLock.lock(); // give the passenger the lock so they are allowed to get off the train accordingly
                while(!mbta.getTrainStation(onTrain).equals(nextStation)){ //while the train's station is not equal to the next station in their journey, wait 
                    try{
                        mbta.passengerCondition.await();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                log.passenger_deboards(Passenger.make(name), Train.make(onTrain), Station.make(nextStation)); //when it is the next station, then deboard event
                mbta.removePassenger(onTrain, name); 
                onTrain = null; //else, set the train to null because we are not on a train anymore
                mbta.setPassengerPos(name, pos + 1); //set the passenger position to the next one in their journey
                
                if(pos + 1 >= journeys.size() - 1){
                    mbta.passengerCondition.signalAll(); //also unacquire the lock and finish the run
                    mbta.passengerLock.unlock();
                    mbta.removePassengerFromWholeMbta(name); //if the passenger is at the end, then remove them from the whole mbta
                    notAtEnd = false;
                    continue;
                }
                mbta.passengerCondition.signalAll(); //release the lock because have finished their operation, now waiting for next train to come.
                mbta.passengerLock.unlock();
            }
        }
    }
}
