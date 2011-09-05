package ch.epfl.lsr.adhoc.routing.ncode.fast;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.Buffer;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

/**
 * This class is used to delay randomly the transmission of a message.
 * <p>
 * The thread sleeps a random amount of time (uniformely distributed between
 * 0 and delayMax). When it awakes, it sends the next message in the buffer
 * and sleeps again a random amount of time. etc.
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public class DelayedTransmission extends FrancThread{
  /** This boolean variable that indicates if the thread is running */
  private boolean running = true;
  /** The Buffer object used to store messages */
  private Buffer buffer;
  /** An AsynchronousLayer that will be used to send messages */
  private NcodeFast layer;
  /** The maximum delay before retransmitting a message (in miliseconds). */
  private int delayMax;
  
  /** 
  	* I changed the random number generation mecanism in order to make
	* it compatible with the Jeode JVM available for the Sharp Zaurus. 
	* The common Random object has been replaced with the call to 
	* Math.random method. Jan 2004 dcavin
	*
  	* The random number generator used to calculate the actual delay.
  	* private Random rand = new Random();
   */

  /** The constructor method */
  public DelayedTransmission(AsynchronousLayer layer, int delayMax) {
    this.layer = (NcodeFast) layer;
    this.delayMax = delayMax; // the maximum time that a message can be delayed (miliseconds)
    this.buffer = new Buffer();
    setDaemon(true);
  }

  /** The run method to activate the thread */
  public void run() {
	 while (running) {
      // calculate delay
		
	  /**
	   * I replaced the following line by the a call by the function :
		* double Math.random() which has a similar effect and is compatible
		* with the Jeode JVM for the Sharp Zaurus. Jan 2004 dcavin
		* 
     	* int delay = rand.nextInt(delayMax);
		*/
				
		int delay = (int)Math.round(Math.random()*(double)delayMax);
      // sleep(delay)
      try {
        FrancThread.sleep(delay);
      	} catch(InterruptedException ie) {}
      	layer.generateEncoded();
   }
 }

  /** Put a message into the waiting list */
  public void sendMessage(Message msg) {
	  buffer.add(msg);
  }
}
