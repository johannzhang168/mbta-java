public class TrainThread extends Thread{
    private MBTA mbta;
    private String name;
    private Log log;

    TrainThread(MBTA mbta, String name, Log log){
        this.mbta = mbta; this.log = log; this.name = name;
    }

    public void run(){
        mbta.passengerLock.lock(); //acquire the passenger lock to begin with
        while(!mbta.isMbtaEmpty()){ //continue to run the train while the mbta still has passengers
            try{
                //allow the passengers to board and deboard the train by releasing the passenger lock
                mbta.passengerCondition.signalAll();
                mbta.passengerLock.unlock();
                sleep(10);
                mbta.trainLock.lock(); //acquire the station lock in order to move
                int position = mbta.getTrainPos(name);
                if(position == 0){
                    mbta.setTrainDir(name, 1);

                }else if(position == mbta.getLine(name).size()-1){
                    mbta.setTrainDir(name, -1);
                }
                int direction = mbta.getTrainDir(name);
                String station = mbta.getTrainStation(name);
                String nextStation = mbta.getLine(name).get(position + direction);
                while(mbta.getStationTrain(nextStation) != null){ // check if the next station is occupied
                    try{
                        mbta.trainCondition.await(); //wait if the next station is occupied
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    if(mbta.isMbtaEmpty()){ // if it happens that there no more passengers in the mbta due to the other trains, then release the lock and finish the run.
                        mbta.trainCondition.signalAll();
                        mbta.trainLock.unlock();
                        continue;
                    }
                }
                mbta.passengerLock.lock(); //final step is to acquire the passenger lock so no passengers can board and deboard this train at this time
                mbta.moveStationTrain(station, nextStation, name); //move the train away from the station it was at and to the next station
                log.train_moves(Train.make(name), Station.make(station), Station.make(nextStation)); //log the move of the train
                mbta.setTrainPos(name, position + direction);
                
                mbta.trainCondition.signalAll(); //tell everyone that the movement is done and the lock is being unlocked
                mbta.trainLock.unlock();//release the train lock after it has moved
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        mbta.passengerCondition.signalAll();
        mbta.passengerLock.unlock();
        return;
    }
}
