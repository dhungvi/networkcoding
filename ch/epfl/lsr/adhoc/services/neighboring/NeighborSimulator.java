package ch.epfl.lsr.adhoc.services.neighboring;

import java.util.*;
import java.net.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
/**
 * Title:
 * Description:  Thread to simulate virtual Neighbors. Can be get each time by the appropriate Method.
 *
 * @author Leiggener Alain
 * @version 1.0
 */

public class NeighborSimulator extends FrancThread {
  private final long SLOT_TIME;
  /* Stores the Nodes of the virtual Neighborhood */
  private Vector virtual;
  /* the last removed virtual Neighbor */
  private Long removedElement;
  /* Indicates if the Test is running in a changing Neighborhod or not */
  private boolean changeNeighbors=false;

  public NeighborSimulator(long slot, int maxNeighbors, boolean changeNeighbors) {
    this.SLOT_TIME=slot;
    this.changeNeighbors=changeNeighbors;
    virtual=new Vector();
    initializeNeighbors(maxNeighbors);
  }

  private void initializeNeighbors(int max){
    Long sourceNode;
    for(int i=0; i<max; i++){
      sourceNode=new Long(proposeSourceID());
      if(virtual.contains(sourceNode)) // it's always possible in this proposal algorithm
        i--;
      else{
        virtual.add(sourceNode);
        System.out.println("New Virtual Node: "+sourceNode.longValue());
      }
    }
  }

  public void run(){
    while(true){
      updateNeighbors();
      try{
        FrancThread.sleep(SLOT_TIME);
      }
       catch(InterruptedException ex){
        System.out.println(ex.getMessage());
       }
    }
  }

  private synchronized void updateNeighbors(){
    if(changeNeighbors){
      //counter++;
      if(virtual.size()!=0){
        removedElement=(Long)virtual.remove(0);
      }
      else
        System.out.println("No virtual Neighbors");
    }
  }

  /* Returns the vector with the actual virtual Neighborsourcenodes */
  protected synchronized Vector getVirtualNeighbors(){
    return virtual;
  }

  private long proposeSourceID(){
     byte[] adr = null;
      try {
        adr = InetAddress.getLocalHost().getAddress();
      } catch(Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
      return    (System.currentTimeMillis() & 0xFFFFFFFF) +
                ((adr[0] & 0xFF) << 56) +
                ((adr[1] & 0xFF) << 48) +
                ((adr[2] & 0xFF) << 40) +
                ((adr[3] & 0xFF) << 32);
  }
}
