package ch.epfl.lsr.adhoc.comessages;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import java.util.*;

/**
 * This layer will manage the causal order for all the messages 
 * which will be at the top of this layer.
 * <p>  
 * @author Alexandre Kozma
 */


public class Co extends AsynchronousLayer  {


	Parameters params;
    /** A reference to FRANC runtime */
    FrancRuntime runtime;
    /** A reference to the asyncronous interface used for the communications. */
    private Dispatcher disp = null;
    /** Type of service to be used for messages */
    private char msgTypeCo;
    /** Vector containing all the Dsm historics  */
    private Vector out = new Vector();
    /** Vector containing all Dsm of message passed on the upper layer */
    private Vector passed = new Vector();
    /** A reference to the node identitiy */
    private long nodeId;
    /** Vector containing WaitBuffer object.*/
    private Vector waitBuffer = new Vector();
    /** LinkedList to create a delay for the buffer out.*/
    private LinkedList delayedDsmList = new LinkedList();
    /** Sequence number of the CoMessages */
    int seqnb;
    /** To create one time delay for the out buffer*/
    boolean delay;
    
    /**
     * The constructor creates sets the sequence number of the message to zero.
     * <p>
     * Set "delay" to false to create a delay for the buffer "out"
     * <p>
     * All the network stuff is done in either in the method initialize() or in the
     * method startup().
     */
    public Co(String name, Parameters params) {
	super(name,params); 
	this.params = params;
	int seqnb = 0;
	delay = false;
    }
    
    /**
     * Initializes the Co layer.
     * <p>
     * This layer initializes network resources, such as the dispatcher.
     * It further obtains the type of messages used by this layer
     * (configuration parameter 'msgType').
     *
     */
    public void initialize(FrancRuntime runtime) {
	this.runtime = runtime;
	nodeId = runtime.getNodeID();
	
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
	} catch(ParamDoesNotExistException pdnee) {
	    pdnee.printStackTrace();
	    throw new RuntimeException("Could not read configuration parameter 'msgType' " +
				       "for causal order layer: " + pdnee.getMessage());
	}
	if(msgName == null) {
	    throw new RuntimeException("Could not read configuration parameter 'msgType' " +
				       "for causal order layer");
	}
	try {
	    msgTypeCo = (runtime.getMessagePool()).getMessageType(msgName);
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
    
    public int sendMessage(Message lowerToLayerMsg) throws SendMessageFailedException {
    	CoMessages com = null;	
	try {
	    com = (CoMessages)(runtime.getMessagePool()).getMessage(msgTypeCo);
	    com.setMsgToUpperLayer(lowerToLayerMsg);
	    com.setSeqnb(seqnb);
	    com.setSrcNode(nodeId);
	    com.setDstNode(lowerToLayerMsg.getDstNode());
	    com.setTTL(lowerToLayerMsg.getTTL());
	    Dsm delayed = null;
	    delayed = getDsmFromMsg(com);
	    delayedDsm(delayed);
	    loadFromDelayedDsm();
	    com.setDsmHist(out);
	} catch(Exception ex) {
	    System.out.println("> # Could not create Message: " + ex.getMessage());
	}
	seqnb = (seqnb+1)%Integer.MAX_VALUE;
	return super.sendMessage(com);
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
	    	int seqnb = ((CoMessages)coMsg).getSeqnb();
	    	Message msgToUpperLayer = null; 
		msgToUpperLayer = ((CoMessages)coMsg).getMsgToUpperLayer();
		Vector in = ((CoMessages)coMsg).getDsmHist();
		unloadSource((CoMessages)coMsg);
		unloadBufferIn(in);
		filterInSameNode(in);
	  	checkMsgHist((CoMessages)coMsg,in);
		findDsmWait((CoMessages)coMsg, in, msgToUpperLayer);
	    } else {
		System.out.println("> Unknown message type: " + ((int)coMsg.getType())+ msgTypeCo);
		super.handleMessage(coMsg);
	    }
	}
    }
    
    /**
     * Takes the message and extracts the destination, 
     * the source and the message id and creates of it a Dsm object 
     * which will be put in the buffer "out".
     * @param coMsg 	 A reference to the CoMessages
     */
    void unloadSource(CoMessages coMsg){	
  	long dstId = coMsg.getDstNode();
   	long srcId = coMsg.getSrcNode(); 
   	int  msgId = coMsg.getSeqnb(); 
  	Dsm temp = new Dsm();
   	temp.setParameters(dstId,srcId,msgId);
   	boolean inBufOut = out.contains(temp);
   	if(inBufOut == false){
	    out.addElement(temp);
   	}
   	
    }
    
    /**
     * Extracts the Dsm historics of the message
     * and put them in the buffer "out".
     * @param in 	 A reference to the CoMessages Dsm historic vector 
     */	
    void unloadBufferIn(Vector in){
	int dimIn = in.size();
	if(dimIn != 0){
	    for(int i = 0; i < dimIn; i++){
		Dsm temp = (Dsm)(in.get(i)); 
		boolean inBuf = out.contains(temp);
      		if(inBuf == false){
		    out.addElement(temp);
		}		
	    }
	}
    }
    
    /**
     * Filters all Dsm in the buffer "in" which doesn't have 
     * the same destination as the node in question and 
     * does have the same source in case of broadcast.
     * 
     * 
     * @param in 	 A reference to the CoMessages Dsm historic vector 
     */
    
    void filterInSameNode(Vector in){
	int dimIn = 0;
	dimIn = in.size();
    	if(dimIn != 0){
	    for(int i = dimIn-1; i >=0; i--){
      		Dsm temp = (Dsm)(in.get(i)); 
      		boolean sameNode = temp.filterDestId(nodeId);
      		if(((temp.getDestId()) == 0)&&((temp.getSourceId())!= nodeId)){
		    sameNode = true;
      		}
      		if(sameNode == false){
		    in.remove(i);
		} 
	    }
	}
    }
    
    /**
     * Checks the four cases possible.
     * 1.1 If the "waitBuffer" and "in" size are null the message can be delivered.
     * 1.2 But if the "in" size buffer is not null he has to wait -> put in the WaitBuffer.
     * 2.1 But if the "in" size buffer is null -> deliver the message and checkDsmWaitHist()  
     * 2.2 If the "waitBuffer" and "in" size are not null -> put in the WaitBuffer.
     * @param coMsg 	 A reference to the CoMessages
     * @param in 	 A reference to the CoMessages Dsm historic vector 
     */
    
    void findDsmWait(CoMessages coMsg, Vector in, Message msgToUpperLayer){
	Dsm dsmOk = getDsmFromMsg(coMsg);
 	int dimWaitBuffer = waitBuffer.size();
	if(dimWaitBuffer==0){
	    if(in.size()==0){
		passed(dsmOk,msgToUpperLayer);
	    }
	    else{	    	
		WaitBuffer temp = prepareWait(dsmOk,in, msgToUpperLayer);
		waitBuffer.add(0,temp);
	    }
	}
	else{
	    if(in.size()==0){
		passed(dsmOk,msgToUpperLayer);
		checkDsmWaitHist(waitBuffer.size(),dsmOk);	
	    }
	    else{
		WaitBuffer temp = prepareWait(dsmOk,in, msgToUpperLayer);
		waitBuffer.add(0,temp);
	    }
	}
    }
    
     /**
      * Checks recursilvely if it contains the Dsm
      * of the message in the DsmWait historic buffer of the object WaitBuffer.
      * If it's true remove the Dsm from the buffer.
      * Check also if the DsmWait historic is empty, if true send the message
      * to the upper layer and put the Dsm of the passed message in the buffer
      * passed and call again the method with this Dsm in parameter. 
      * @param dim 	 A reference to the WaitBuffer size.
      * @param dsmOk 	 A reference to the Dsm of a message passed to the upper layer. 
      */
    void checkDsmWaitHist(int dim, Dsm dsmOk){
    	for(int i = dim-1; i >=0; i--){
	    WaitBuffer waitTemp = (WaitBuffer)waitBuffer.get(i);
	    Vector dsmHistTemp = waitTemp.getDsmWaitHist();    
	    if(dsmHistTemp.contains(dsmOk)){
		int pos = dsmHistTemp.indexOf(dsmOk);
		dsmHistTemp.remove(pos);
		int dimDsmHistTemp = dsmHistTemp.size();	
		if(dimDsmHistTemp == 0){
		    Dsm dsmPassed = (Dsm)(waitTemp.getDsm());
		    Message mesg = (Message)(waitTemp.getMsgWait());		    	
		    passed(dsmPassed, mesg);
		    waitBuffer.remove(i);
		    checkDsmWaitHist(waitBuffer.size(),dsmPassed);
		} 
	    }		
	}
    }
    
    /**
     * Filters the buffer "in" with the Dsm already passed.
     * @param coMsg 	 A reference to the CoMessages
     * @param in 	 A reference to the CoMessages Dsm historic vector 
     */
    void checkMsgHist(Message coMsg, Vector in){
	int dimIn = 0;
	dimIn = in.size();
	if(dimIn != 0){
	    int dimPassed = passed.size();
	    if(dimPassed != 0){
		for(int i = dimIn-1; i >= 0; i--){
		    Dsm temp = (Dsm)(in.get(i)); 
		    boolean sort = passed.contains(temp);
		    if(sort == true){
		    	System.out.println("remove passed");
		    	in.remove(i);
		    }
		}	
	    }	
	}
    }
    
    
    /**
     * Returns the destination, source and message id.
     * @param coMsg 	 A reference to the CoMessages
     * @return A Dsm object, with the destination, source and message id
     * 		   from the message.
     */
    Dsm getDsmFromMsg(CoMessages coMsg){
	Dsm temp = new Dsm();
	long dstId = coMsg.getDstNode();
   	long srcId = coMsg.getSrcNode(); 
   	int msgId = coMsg.getSeqnb();
   	temp.setParameters(dstId,srcId,msgId);
   	return temp;
    }
    
    /**
     * Returns the destination, source and message id.
     * @param Msg 	 A reference to the Message
     * @return A Dsm object, with the destination, source and message id
     * 		   from the message.
     */
    Dsm getDsmFromMsg(Message Msg){
	Dsm temp = new Dsm();
	long dstId = Msg.getDstNode();
   	long srcId = Msg.getSrcNode(); 
   	int msgId = ((CoMessages)Msg).getSeqnb();
   	temp.setParameters(dstId,srcId,msgId);
   	return temp;
    }
    
    
    /**
     * Returns the Dsm,Dsm historic and data of the message.
     * @param dsmWait 	 A reference to the source, destination and 
     * 					 message id from the message in question.
     * @param dstHistWait 	 A reference to the history of all the Dsm.
     * @param msgWait 	 A reference to message in question.
     * @return A WaitBuffer object,with the Dsm, Dsm historic of the 
     * 		   message and the message in question.
     */
    WaitBuffer prepareWait(Dsm dsmWait,Vector dstHistWait, Message msgWait){
	WaitBuffer waitToBeHandel = new WaitBuffer();
	waitToBeHandel.setDsm(dsmWait);
	waitToBeHandel.setDsmWaitHist(dstHistWait);
	waitToBeHandel.setMsgWait(msgWait);
	return waitToBeHandel;
	
    }
    
    /**
     * Delives the message to the upper layer and put
     * the Dsm of the message into the passed message buffer. 
     * @param dsm 	     A reference to the source, destination and 
     * 					 message id from the message in question.
     * @param msgWait 	 A reference to message in question.
     */
    void passed(Dsm dsm,Message mesg){
    	passed.addElement(dsm);
	super.handleMessage(mesg);
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
    
    /**
     * Add the Dsm of the message (before going out) in a linkedList to create a delay -1
     */
    void delayedDsm(Dsm dsm){
    delayedDsmList.addFirst(dsm);		
    }
    
    
    /**
     * Creates a delay in the "out" buffer for the outgoing messages
     */
    void loadFromDelayedDsm(){
    	if(delay == true){
	    Dsm temp = null;
	    temp = (Dsm)(delayedDsmList.getLast());
	    boolean inBufOut = out.contains(temp);
	    if(inBufOut == false){
   		out.addElement(temp);
	    } 
	    delayedDsmList.removeLast();
    	}
    	delay = true;
    }
}

