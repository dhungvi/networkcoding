package ch.epfl.lsr.adhoc.reliableLayer;

import java.util.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**This class is used as a timer for the ReliableMultihop. It runs as a Thread after being called from the ReliableMultihop. It controls
 * the timeouts of the Unicast-messages sent by the ReliableMultihop that are stored in its sentMessages HashMap.
 * Because it needs to control the ReliableMultihop, a reference of the ReliableMutlihop is passed to the constructor. Thus the Thread
 * has acces on the public methods of the ReliableMultihop.
 *
 * @ver June 2003
 */

public class CheckMultihop extends FrancThread{
    /** The ReliableMultihop to control. */
    private ReliableMultihop toControl;
    private boolean awake=false;
    /** A reference to the HashMap which has stored the Unicast messages sent. */
    private HashMap sentMessages;
    /** A reference to the LinkedList that indicates the next Unicast-message to resend. */
    private LinkedList timeout;
    /* Max wait time of the synchronized stuck in the run Mehtod */
    private long dream=3000;
    private boolean running;

    /** The constructor receives the reference of the calling ReliableMultihop, so that it's able to access the Methods from that Layer */
    public CheckMultihop(ReliableMultihop RLayer, HashMap sentMessages, LinkedList timeout){
	this.toControl=RLayer;
	this.sentMessages=sentMessages;
	this.timeout=timeout;
        this.running=true;
    }

    /** Set the Thread's running status. If it is set to true, the run Method takes the lock.
     */
    public synchronized void setAwake(boolean awake){
	this.awake=awake;
    }

    /** Method to destroy the Thread without any cleanup.
     */
     public void terminate(){
       running=false;
     }

    /** Returns the actual running status. If false: the run Method is blocked, otherwise it's running.
     *  @return boolean
     */
    public boolean getAwake(){
      return this.awake;
    }
    /** Notifies the run Method to take the lock.
     *
     */
    public synchronized void go(){
	notify();
    }

   /**
   * Method that runs until the parameter running is set to false. It awaits the closest timeout. Then it
   * controls if the appropriate unicast message has to be resent and if it is the case it will call the appropriate method of ReliableMultihop
   * to resend the message. Then it awaits the next timeout.
   *  */
    public void run(){
	long sleepTime=0;
	while (running){
            synchronized ( this ){
              try{
                  while (!awake)  // no message in sentUnicast of ReliableMultihop
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
            if(toControl.isResentNeeded()){
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

        /** Returns the next timeout of a message to acknowledge in Milliseconds.
     *
     * @return Nearest Timeout in Millis. (long)
*/
    private long getNextTimeout(){
	long time=0;
        synchronized ( this ) {
          try {
            while(timeout.size()==0){
              this.awake=false;
              wait();
            }
          }
        catch(InterruptedException exep) {}
        }
        try  {
          synchronized(toControl){
                  Integer nextElement=(Integer)timeout.getFirst();
        	  UnicastDestination dest=(UnicastDestination)sentMessages.get(nextElement);
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
