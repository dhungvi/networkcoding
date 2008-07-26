package ch.epfl.lsr.adhoc.tools;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;


/**
 * This asychronous layer is useful for debugging.
 * It basically does nothing except that it prints to the
 * standard output all the messages sent and received.
 *
 * @author D.Cavin, lsr, 2003
 */

public class DebugLayer extends AsynchronousLayer {

    MessagePool mp;
	Parameters params;
    char msgType;

    public DebugLayer(String name, Parameters params){
		super(name, params);
		this.params = params;
    }

    public void initialize(FrancRuntime runtime) {
		mp = runtime.getMessagePool();  
		String msgName = null;
		try {msgName = params.getString("msgType");}
		catch (ParamDoesNotExistException pdnee) {pdnee.printStackTrace();}		
		msgType = mp.getMessageType(msgName);
    }

    public void startup() {
		start();
    }

    protected void handleMessage(Message msg) {
		//System.out.println("MESSAGE RECEIVED : "+msg);
		//super.handleMessage(msg);
		if(msg.getType() == msgType) {
			DebugMessage debugMessage = (DebugMessage)msg;
			Message in = debugMessage.getMsg(mp);		
			System.out.println("MESSAGE RECEIVED : "+in);		
			super.handleMessage(in);
			mp.freeMessage(msg);
		}
		else {
			throw new RuntimeException("It merds");
		}
    }
  
    public int sendMessage(Message msg) throws SendMessageFailedException {
		System.out.println("MESSAGE SENT : "+msg);
		//return super.sendMessage(msg);
		DebugMessage debugMessage = (DebugMessage)mp.getMessage(msgType);	 
		debugMessage.setMsg(msg);
		debugMessage.setDstNode(msg.getDstNode());
		debugMessage.setTTL(msg.getTTL());	
		int len = super.sendMessage(debugMessage);
		mp.freeMessage(msg);
		return len;
    }
}

