package ch.epfl.lsr.adhoc.routing.localrouting;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

/**
 * @author David Cavin
 * @version 1.0
 */
public class LocalRouting extends AsynchronousLayer {

    /** The node if of this node. */
    private long myNodeID;
    /** A reference to message pool. */
    private MessagePool mp;

    /** Default constructor */
    public LocalRouting(String name, Parameters params) {
	super(name, params);
    }

    /**
     * @param comLayer The CommunicationsLayer for this node
     */
    public void initialize(FrancRuntime runtime) {
	this.myNodeID = runtime.getNodeID();
	this.mp = runtime.getMessagePool();
    }

    /**
     * Starts the threads for this layer.
     */
    public void startup() {
	start();
    }

    /**
     * @param msg The message to handle
     */
    protected void handleMessage(Message msg){
	if(msg.getDstNode() == myNodeID || msg.getDstNode() == 0) {
	    msg.setTTL(msg.getTTL()-1);
	    super.handleMessage(msg);
	} else {
	    mp.freeMessage(msg);
	}
    }
	
    public int sendMessage(Message msg) throws SendMessageFailedException {
	if(msg.getTTL() <= 0) return 0;
	msg.setNextHop(msg.getDstNode());
	return super.sendMessage(msg);
    }
}
