package ch.epfl.lsr.adhoc.services.neighboring;

import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Buffer;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import java.util.*;

/**
 * Description: This class serves to simulate a moving neighborhood in the Manetframework. The
 * Neighbortable (got from the neighborhood will be extended by virtual nodes). 
 * As long as a virtual neighbor exist in this table, the ReliableLayer should resend the Message, that of course
 * hasn't been acknowledged by those virtual nodes.
 * So one can simulate different situations. It's also possible to let disappear neighbors. After a configurable timeout
 * one neighbour disappears. This is repeated until no more virtual neighbor exists.
 *
 * There needs to be set some particular variables in the configuration file of FRANC, to being
 * able to use this layer:
 * <lu> 
 * <li>maxVirtualNeighbors: The number of virtual neighbors that are initialized at the start.</li>
 * <li>changeNeighbors: Two possible settings: <br>0: The virtual neighbors do not disappear.
 * <br>1: The virtual neighborhood will change over the time. In fact all neighbors will have been disappeared after a certain time.</li>
 *<li>slot_time: If the changeNeighors option is activated this will be the time intervall by which one virtual
 * neighbor will be deleted.</li>
 * </lu>
 * 
 * Company: EPFL
 * @author Leiggener Alain
 * @version 1.0
 */

public class SimulateNeighborService extends NeighborService {

	private Parameters params;

  /** The slottime that the simulatorthread waits until to generate a new neighbor constellation
   *
   */
  private long SLOT_TIME;
  /* Maximal Number of virutal Nodes */
  private int MAX_NEIGHBORS;
  /* Virtual Neighbors */
  private Vector virtualNeighbors;
  /* simulator Thread. */
  private NeighborSimulator sim;
  /* changing Neighborhod ? */
  private int changeNeighbors;
  
  /** The upper Constructor is called.
   *
   */
  public SimulateNeighborService(Parameters params) {
	  super(params);
	  this.params = params;
  }

  public void initialize(FrancRuntime runtime){
      disp = runtime.getDispatcher();
      mp = runtime.getMessagePool();
      
      neighbors = new NeighborTable();
      buf = new Buffer();
      
      //start (initialize) the sending of hello messages in a seperate thread
      
      try {
	  msgType = mp.getMessageType(params.getString("msgType"));
      }catch(ParamDoesNotExistException pdnee) {
	  pdnee.printStackTrace();
      }
	
	try {
      	interval = params.getInt("HelloInterval");
    	}catch(ParamDoesNotExistException pdnee) {
    	pdnee.printStackTrace();
    	}	
    
    	try {
        entryExp = params.getInt("entryExp");
    	}catch(ParamDoesNotExistException pdnee) {
    	pdnee.printStackTrace();
   	}
    
    
             
        sender = new FrancThread(new Hello(runtime,msgType, interval));
        sender.setDaemon(true);

        //getting messages
        setDaemon(true);
        try {
                int temp = params.getInt("slot_time");
                SLOT_TIME= (long)temp*1000;
        }
        catch(Exception ex) {
                throw new RuntimeException("Error reading the slot_time parameter: " + ex.getMessage());
        }
         try {
                MAX_NEIGHBORS = params.getInt("maxVirtualNeighbors");
        }
        catch(Exception ex) {
                throw new RuntimeException("Error reading the maxVirtualNeighbors parameter: " + ex.getMessage());
        }
         try {
               changeNeighbors  = params.getInt("changeNeighbors");
        }
        catch(Exception ex) {
                throw new RuntimeException("Error reading the changeNeighbors parameter: " + ex.getMessage());
        }
	
        sim=new NeighborSimulator(SLOT_TIME, MAX_NEIGHBORS, changeNeighbors==1);	
  }

  public void startup(){
    if(MAX_NEIGHBORS>0)
      sim.start();
    sender.start();
    start();
  }

   /**
     * That's a method needed to be overwritten by this Simulator class.
     * It adds some virtual Nodes to the Neighborhood.
     * 
     * This method is run by the second thread to keep the neighbor table up to
     * date. The thread reads messages put into the buffer by the dispatcher and
     * adds, updates or deletes entries in the table.
     * <p>
     * The thread waits until a message is put in the buffer or an entry will
     * expire.
     */
    public synchronized void run() {
        long dream = Long.MAX_VALUE;
        HelloMsg newMsg;
        while(true) {
            try {
                wait(dream);
                Date date = new Date();
                long currentTime = date.getTime();
                while (!buf.isEmpty()) {
                    newMsg = (HelloMsg)buf.remove();
                    updateNeighborTable(new NeighborTableEntry(newMsg.getSrcNode(), currentTime, entryExp));
                    mp.freeMessage(newMsg);
                }
                virtualNeighbors=sim.getVirtualNeighbors();
                long source;
                Long sourceObj;
                for(int i=0;i<virtualNeighbors.size(); i++){
                  sourceObj=(Long)virtualNeighbors.elementAt(i);
                  source=sourceObj.longValue();
                  updateNeighborTable(new NeighborTableEntry(source, currentTime, entryExp));
                }
                dream = checkNeighborTable();
              } catch (InterruptedException ie) {
            }
        }
   }
}
