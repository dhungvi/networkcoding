package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;

/**
 * This class is a layer introduced between the communications layer and the
 * dispatcher to observe the messages passed through the stack. The information
 * retrieved is packed into an internal message called StatMessage and passed on
 * to the dispatcher. The dispatch in turn passes the message to the StatisticsService
 * which is responsible for the processing of the information retrieved.
 */
 /* ------------------------      -----------------------
 * |      Dispatcher      | ----->|   StatisticsService  |
 * ------------------------       -----------------------
 *            |
 * ------------------------
 * |  StatisticsLayer     |
 * ------------------------
 *            |
 * ------------------------
 * |  CommunicationsLayer |
 * ------------------------
 */
 /**
 * @see StatisticsService
 *
 * @author Reto Krummenacher
 */
public class StatisticsLayer extends AsynchronousLayer {

  //FIELDS
	private Parameters params;
  private char statType;
  private MessagePool mp;

  //CONSTRUCTORS
  public StatisticsLayer(String name, Parameters params){
      super(name, params);
	  this.params = params;
  }

  //METHODS
  public void initialize(FrancRuntime runtime) {
    this.mp = runtime.getMessagePool();
    
    	try {
	String msgType = params.getString("msgType");
	statType = mp.getMessageType(msgType);

    	}catch(ParamDoesNotExistException pdnee) {
    		pdnee.printStackTrace();
    	}
    
  }

  public void startup() {
    start();
  }

  /**
   * Inherited comments
   */
  protected void handleMessage(Message msg) {
    int length = msg.getLength();
    StatMessage smsg = (StatMessage)mp.getMessage(statType);
    smsg.setParameters(msg.getType(),length,true);
    super.handleMessage(msg);
    super.handleMessage(smsg);
  }

  /**
   * Inherited comments
   */
  public int sendMessage(Message msg) throws SendMessageFailedException {
    msg.createCopy();
    int length = super.sendMessage(msg);
    StatMessage smsg = (StatMessage)mp.getMessage(statType);
    smsg.setParameters(msg.getType(),length,false);
    mp.freeMessage(msg);
    super.handleMessage(smsg);
    return length;
  }
}
