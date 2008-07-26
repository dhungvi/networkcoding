package ch.epfl.lsr.adhoc.reliableLayer;

import java.util.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**This class is used as a timer for the ReliableLayer. It runs as a Thread after being called from the ReliableLayer. It awaits
 * the timeouts of the messages sent by the ReliableLayer that are stored in its sentMessages HashMap.
 * Because it needs to control the ReliableLayer, a reference of the ReliableLayer is passed to the constructor. Thus the Thread
 * has acces on the public methods of the ReliableLayer.
 *
 * @ver June 2003
 * @author Leiggener Alain
 */

public class CheckAcknowledge extends FrancThread{
    /** Reference of the ReliableLayer to control */
    private ReliableLayer toControl;
    private boolean awake=false;
    /** Reference of the HashMap that stores the messages sent from the ReliableLayer */
    private HashMap sentMessages;
    /** Refernece of the LinkedList that indicates the next message to resend. */
    private LinkedList timeout;
    /* Max wait time of the synchronized stuck in the run Mehtod */
    private long dream=3000;
    private boolean running;

    /** The Constructor receives the Reference of the calling ReliableLayer, so that it has access to the methods needed.
     *  */
    public CheckAcknowledge(ReliableLayer RLayer, HashMap sentMessages, LinkedList timeout){
	this.toControl=RLayer;
	this.sentMessages=sentMessages;
	this.timeout=timeout;
        this.running=true;
    }

    /** Set the Thread's running status. If it is set to true, the run - Method is able to take the lock.
     *
     */
    public synchronized void setAwake(boolean awake){
	this.awake=awake;
    }

    /** Method to destroy the Thread without any cleanup.
     */
     public void terminate(){
       running=false;
     }

    /** Returns the actual run status. If it is false than the run Method is blocked, otherwise it is running.
     *
     *  @return boolean
     */
    public boolean getAwake(){
      return this.awake;
    }
    /** Notifys the run Method to take the lock.
     *
     */
    public synchronized void go(){
	notify();
    }

  /**
   * Method that runs until the parameter running is set to false. It awaits the closest timeout. Then it
   * controls if the appropriate message has to be resent and if it is the case it will call the appropriate method of the ReliableLayer
   * to resend the message. Then it awaits the next timeout.
   *  */
    public void run(){
	long sleepTime=0;
	while (running){
            synchronized ( this ){
              try{
                  while (!awake)
                    wait(dream);
              }
              catch (InterruptedException e){
                  System.out.println(">- Error in CheckAcknowledge (run()): " +e.getMessage());
              }
            }
            sleepTime=getNextTimeout();
            if(sleepTime>=0){
              try{
                  FrancThread.sleep(sleepTime);
              }
              catch (InterruptedException e) {
                  System.out.println(">- Error in CheckAcknowledge (run()): " +e.getMessage());
              }
            }
            toControl.updateNeighbors();
            if(toControl.isReBroadcastNeeded()){
              toControl.reSendMessage();
            }
            synchronized ( this ){
              if (sentMessages.size()==0){
                  awake=false;
              }
              else
                  awake=true;
            }
          }
    }

    /** Returns the next Timeout of a message to acknowledge in Milliseconds.
     *
     * @return Next Timeout in Millis. (long)
*/
    private long getNextTimeout(){
	long time=0;
        synchronized ( this ) {
          try {
            while(timeout.size()==0){
              //System.out.println("wait in getNextTimeout()");
              this.awake=false;
              wait();
            }
          }
        catch(InterruptedException exep) {}
        }
        try  {
          synchronized(toControl){
                  Integer nextElement=(Integer)timeout.getFirst();
        	  Destinations dest=(Destinations)sentMessages.get(nextElement);
                  if(dest!=null){
                	  time=dest.getTime()-System.currentTimeMillis();
                  }
          }
        }
        catch(NoSuchElementException ex) {
          /* Nothing to do. The next Element has gone during the wait(). time=0 */
        }
	return time;
    }
}
