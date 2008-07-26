package ch.epfl.lsr.adhoc.comessages;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import java.util.Vector;
import java.util.LinkedList;

/**
 * This method will take the number of messages put in parameter and 
 * will reverse their order of arrival
 * <p>  
 * @author Alexandre Kozma
 */


public class CoTest extends AsynchronousLayer  {
    
	Parameters params;

    /** A reference to the asyncronous interface used for the communications. */
    private Dispatcher disp = null;
    /** A reference to the message pool (for creating new messages) */
    //private MessagePool mp = null;
    /** Type of service to be used for messages */
    private char msgTypeCo;
    /** Variable of test */
    int x;
    /** Buffer to accumulate the messages arrived in order to return them in the opposite order*/
    private Vector waitMsg = new Vector();
    /** Create the delay -1 by taking the last Dsm in the LinkedList at two elements*/ 
    private LinkedList delayedDsmList = new LinkedList();
    /** Set the number of delay*/
    int nbDelay;
    
    
    
    
    
    public CoTest(String name, Parameters params) {
	super(name, params); 
	this.params = params;
	x = 1;
	waitMsg.removeAllElements(); 
	/* To set the number of delay*/
	nbDelay =4;
	
	
    }
    
    /**
     * Initializes the Co layer.
     * <p>
     * This layer initializes network resources, such as the message pool and the
     * dispatcher. It further obtains the type of messages used by this layer
     * (configuration parameter 'msgType').
     *
     */
    public void initialize(FrancRuntime runtime) {
	// obtain reference to dispatcher
	try {
	    disp = runtime.getDispatcher();;
	} catch(Exception ex) {
	    System.out.println("> # Could not obtain Dispatcher: " + ex.getMessage());
	    throw new RuntimeException(ex.getMessage());
	}
	if(disp == null) {
	    throw new RuntimeException("Dispatcher is null");
	}
	
	String msgName = null;
	try {
	    msgName = params.getString("msgTypeCo");
	} catch(Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Could not read configuration parameter 'msgType' " +
				       "for causal order layer: " + ex.getMessage());
	}
	if(msgName == null) {
	    throw new RuntimeException("Could not read configuration parameter 'msgType' " +
				       "for causal order layer");
	}
	try {
	    msgTypeCo = (runtime.getMessagePool()).getMessageType(msgName);;
	} catch(Exception ex) {
	    throw new RuntimeException("Message type '" + msgName + "' not found");
	}
    }
    
    /**
     * Start the functionality of the Co layer.
     * <p>
     * This method starts the thread.
     */
    public void startup() {
	start();	
    }
    /*
     * This method complets the message and sends it to the lower layer.
     */
    
    public int sendMessage(Message upperLayerMsg) throws SendMessageFailedException {
	return super.sendMessage(upperLayerMsg);
    }
    
    /**
     * This method is called from the super class (AsynchronousLayer) when a new
     * new message was received.
     */
    public void handleMessage(Message coMsg) {	
	if(coMsg == null) {
	    System.out.println("> # message is null");
	} 
	else {
	    if(coMsg.getType() == msgTypeCo) {	
	    	if(waitMsg.size() != nbDelay){
	    		waitMsg.add(0,coMsg);
	    		System.out.println("Delay in buffer message: " + ((CoMessages)coMsg).getSeqnb());
	    	}
	    else{
	    	if((waitMsg.size() == nbDelay)&&(x==1)){
	    		unload();
	    		System.out.println("Send delayed upperlayer message : " + ((CoMessages)coMsg).getSeqnb());
	    		super.handleMessage(coMsg);
	    		x=0;
	    	}
	    	else{
	    			 	System.out.println("envoie des message simplement: " + ((CoMessages)coMsg).getSeqnb());
	    	 super.handleMessage(coMsg);
	    	}	
	    }
	    }
	    else {
		System.out.println("> Unknown message type: " + ((int)coMsg.getType()));
		super.handleMessage(coMsg);
	         } 
	    }
    }
    
    
    
    
    /**
     * This method put the number of message (set in parameter nbDelay) in a buffer 
     * and release them in the reverse order, then continues in the same order.
     */
    void unload(){
    	System.out.println("taille du delayed buffer: "+ waitMsg.size());
    	   		for(int i = 0; i <waitMsg.size(); i++){
	 		Message temp = null;
			temp = (Message)(waitMsg.get(i));
	 		super.handleMessage(temp);
	 		System.out.println("Send delayed upperlayer message : " + ((CoMessages)temp).getSeqnb());
	 		//waitMsg.remove(i);		
    	}
    }
    
    /**
     * This method terminates the application.
     * <p>
     * To add a dialog for exiting the application, or to save options upon exit,
     * one could change the implementation of this method.
     */
    private void exit() {
	System.exit(0);
    }
    
    
    
}

