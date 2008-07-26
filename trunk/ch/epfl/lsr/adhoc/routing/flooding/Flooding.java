
package ch.epfl.lsr.adhoc.routing.flooding;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;

/**
 * This layer implements the simplest routing alogrithme: flooding.
 * <p>
 * Every message received, that has not previously been received (identified
 * by source node and sequence number), that is not destined for this node
 * and that did not originate from this node,
 * will be retransmitted with a certain probability, if its TTL is bigger than 0.
 * The message is retransmitted, when:
 * <ul>
 * <li>message not already received (srcNode & seqNumber)</li>
 * <li>TTL &gt; 0</li>
 * <li>srcNode not this node</li>
 * <li>dstNode not this node</li>
 * <li>Test for probability ok (random number generator)</li>
 * </ul>
 * <p>
 * A message is only propagated up the stack, if its destination is this node, or
 * if it is a broadcast (destination node number is 0).
 * The message will be propagated up the stack, if:
 * <ul>
 * <li>The message is for this node</li>
 * <li>Or the message is a broadcast</li>
 * </ul>
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public class Flooding extends AsynchronousLayer {

	private Parameters params;

  /** Used to retransmit a message after a random delay */
  private DelayedTransmission dt;
  /** The number of entries for identifying the most recent messages */
  private int bufferLength;
  /** The probability of retransmission (in percent) */
  private int prT;
  /**
   * For previously received messages, a list of their sequence numbers.
   * The variable sourceNodes contains the corresponding source node ids.
   */
  private int [] seqNumbers;
  /**
   * For previously received messages, a list of their source node ids.
   * The variable sourceNodes contains the corresponding sequence numbers.
   */
  private long [] sourceNodes;

  /** 
  	* I changed the random number generation mecanism in order to make
	* it compatible with the Jeode JVM available for the Sharp Zaurus. 
	* The common Random object has been replaced with the call to 
	* Math.random method. Jan 2004 dcavin
	*
   * The random number generator used to decide on retransmission 
  	* private Random rand = new Random();
   */
  
  /** The node if of this node. */
  private long myNodeID;
  /** A reference to message pool. */
  private MessagePool mp;

  /** Default constructor */
  public Flooding(String name, Parameters params) {
      super(name, params);
	  this.params = params;
  }

  /**
   * Initializes the flooding layer.
   * <p>
   * The following configuration parameters are read:
   * <ul>
   * <li><b>layer.Flooding.bufferLength</b> - The number of entries for
   *     identifying the most recently received messages</li>
   * <li><b>layers.Flooding.prT</b> - The probability that a message is retransmitted</li>
   * <li><b>layers.Flooding.delayMay</b> - The maximum delay for retransmitting a message</li>
   * </ul>
   *
   */
  public void initialize(FrancRuntime runtime) {
    try {
      bufferLength = params.getInt("bufferLength");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value bufferLength: " + pdnee.getMessage());
    }
    if(bufferLength < 0)
      throw new RuntimeException("Configuration variable bufferLength cannot be negative");
    try {
      prT = params.getInt("probaRetransmit");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value prT: " + pdnee.getMessage());
    }
    if(prT < 0 || prT > 100)
      throw new RuntimeException("Configuration variable prT must be between 0 and 100 inclusive");
    int delayMax = 0;
    try {
      delayMax = params.getInt("delayMax");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value delayMax: " + pdnee.getMessage());
    }
    if(delayMax < 0)
      throw new RuntimeException("Configuration variable delayMax cannot be negative");

    seqNumbers = new int[bufferLength];
    sourceNodes = new long[bufferLength];
    this.dt = new DelayedTransmission(this, delayMax);
    this.myNodeID = runtime.getNodeID();
    this.mp = runtime.getMessagePool();
  }

  /**
   * Starts the threads for this layer and for retransmitting messages after a
   * certain delay.
   */
  public void startup() {
    start();
    dt.start();
  }

  /**
   * This method decides whether to retransmit a message and whether to propagate
   * it up in the stack.
   * For more details see the class description.
   * <p>
   * @param msg The message to handle
   */
  protected void handleMessage(Message msg){
    int sequenceNumber = msg.getSequenceNumber();
    long srcNode = msg.getSrcNode();
    long dstNode = msg.getDstNode();
    int ttl = msg.getTTL();
    int index = 0;
    boolean found = false;
	
    msg.setTTL(ttl - 1);
    for(int i = 0; i < bufferLength; i++) {
      if((sequenceNumber == seqNumbers[i]) && (srcNode == sourceNodes[i])) {
        // message  already received
        found = true;
        break;
      }
    }
    if(!found) {
      seqNumbers[index] = sequenceNumber;
      sourceNodes[index] = srcNode;
      index++;
      if(index >= bufferLength) {
        System.out.println("Warning: the buffer is full, index will restart at zero");
        index = 0;
      }
      if((ttl > 0) && (srcNode != myNodeID) && (dstNode != myNodeID)) {
        //retransmit the message with probability prT after a delay
		  
		  /**
		   * I replaced the following line by the a call by the function :
			* double Math.random() which has a similar effect and is compatible
			* with the Jeode JVM for the Sharp Zaurus. Jan 2004 dcavin
			* 
        	  if(rand.nextInt(100) < prT) {		  
			*/
		  
        if(Math.round((Math.random()*100.0)) < prT) {
          // to avoid the blocking of the thread, we send the message to the class DelayedTransmission
          // where it'll be delayed for a random time before being transmitted
          msg.createCopy();
          dt.sendMessage(msg);
        }
      }
      //dstNode == 0 : broadcast
      if((dstNode == myNodeID) || (dstNode == 0)) {
        msg.createCopy();
        super.handleMessage(msg);
      }
    }
    mp.freeMessage(msg);
  }
  
	public int sendMessage(Message msg) throws SendMessageFailedException {
		if(msg.getTTL() <= 0) return 0;	
		msg.setNextHop(0);
		return super.sendMessage(msg);
	}

}
