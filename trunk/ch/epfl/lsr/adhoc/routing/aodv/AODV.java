package ch.epfl.lsr.adhoc.routing.aodv;

import java.util.*;

import ch.epfl.lsr.adhoc.services.neighboring.NeighborService;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborTable;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

/**
 * This layer implements the AODV routing algorithm.
 * <p>
 * It mainly uses three types of differents messages for maintaining a routing table which contains the paths to known nodes : Route Requests(RREQ), Route Replies(RREP), Route Errors(RERR).
 * <p> 
 * @author Alain Bidiville
 */
public class AODV extends AsynchronousLayer {

	Parameters params;

    // A reference to FRANC runtime
    FrancRuntime runtime;
    /** A reference to the asynchronous interface used for the communications. */
    private Dispatcher disp = null;
    /** A reference to the message pool (for creating new messages) */
    //private MessagePool mp = null;
    /** A reference to the Routing Table (for maintaining routes to nodes)
     *@see NeighborTableEntry
     **/
    private Vector RoutingTable = null;
    /** A reference to the Neighbouring Service*/
    private NeighborService nm;
    /** Type of service to be used for Route Request Messages*/
    private char msgTypeRREQ;
    /** Type of service to be used for Route Reply Messages*/
    private char msgTypeRREP;
    /** Type of service to be used for Route Error Messages */
    private char msgTypeRERR;
    /** The default lifetime of a route in the routing table */
    private long ACTIVE_ROUTE_TIMEOUT; 
    /** The maximum time allowed for waiting a route searched by a Route Request Message */
    private long PATH_DISCOVERY_TIME;
    /** The maximum number of nodes you have to go through to cross the whole network */
    private int NET_DIAMETER;
    /** The default lifetime of a route in the routing table before being deleted, after having been invalidated */
    private long DELETE_PERIOD;
    /** The length of the crossing of a message through a node */
    private long NODE_TRAVERSAL_TIME;
    /** The maximum time of the crossing of a message through the whole network */
    private long NET_TRAVERSAL_TIME;
    /** The table which contains the IDs of the Route Request Messages which were sent and the ID of the node which sent them
     *@see RREQsentIDsPair*/
    private Vector RREQsentIDsPairs;
    /** A reference to the thread which sends Route Request Messages */
    private SendThread SendRREQThread;
    /** The buffer for Route Request Messages which are to be sent 
     *@see RREQsentBufferEntry 
     **/
    private Vector RREQsSentBuffer;
    /** The buffer for messages which are waiting for a route to a unknown destination */
    private Vector MsgsWaitingForRoute;
    /** The maximum number of times a Route Request Message can be sent */
    private int RREQ_TRIES;
    /** A reference to the object which maintains the sequence number of this node and the ID of the Route Request Message to be sent 
     *@see SeqNumberAndRREQIDsController*/
    private SeqNumberAndRREQIDsController SeqNumberAndRREQIDsControllerObjet;
    private boolean threadRREQrunning;
    
    /** Default constructor */
    public AODV(String name, Parameters params){ 
	super(name, params);
	this.params = params;
	RoutingTable = new Vector();
	RREQsSentBuffer = new Vector();
	MsgsWaitingForRoute = new Vector();
	RREQsentIDsPairs = new Vector();
	SeqNumberAndRREQIDsControllerObjet = new SeqNumberAndRREQIDsController();
	threadRREQrunning = false;
    }
    
    /**
     * Initializes the AODV layer.
     * <p>
     * First, references for the MessagePool and the Dispatcher are created.
     * <p>
     * The following configuration parameters are read:
     * <ul>
     * <li><b>NET_DIAMETER</b> - The maximum number of nodes you have to go through to cross the whole network. </li>
     * <li><b>NODE_TRAVERSAL_TIME</b> - The length of the crossing of a message through a node. </li>
     * <li><b>RREQ_TRIES</b> - The maximum number of times a Route Request Message can be sent. </li>
     * <li><b>msgTypeRREQ</b> - The char which defines the type of message : Route Request. </li>
     * <li><b>msgTypeRREP</b> - The char which defines the type of message : Route Reply. </li>
     * <li><b>msgTypeRERR</b> - The char which defines the type of message : Route Error. </li>
     * <li><b>msgTypeRREPACK</b> - The char which defines the type of message : Route Reply Acknowledgment. </li>
     * </ul>
     * <p>
     *   The following parameters are calculated using NET_DIAMETER and NODE_TRAVERSAL_TIME :
     * <ul>
     * <li><b>NET_TRAVERSAL_TIME</b> - The maximum time of the crossing of a message through the whole network. </li>
     * <li><b>ACTIVE_ROUTE_TIMEOUT</b> - The default lifetime of a route in the Routing Table. </li>
     * <li><b>PATH_DISCOVERY_TIME</b> - The maximum time allowed for waiting a route searched by a Route Request Message. </li>
     * <li><b>DELETE_PERIOD</b> - The default lifetime of a route in the routing table before being deleted, after having been invalidated. </li>
     * </ul>
     *
     */
    public void initialize(FrancRuntime runtime) {
	
	// If a parameter is bad, the AODV layer cannot work well, so it's OK to put all the access to parameters in one try/catch.
	try{
	    this.runtime = runtime;
	    disp = runtime.getDispatcher();
	    NET_DIAMETER = params.getInt("NET_DIAMETER");
	    NODE_TRAVERSAL_TIME = params.getLong("NODE_TRAVERSAL_TIME");
	    RREQ_TRIES = params.getInt("RREQ_TRIES");
	    msgTypeRREQ = (runtime.getMessagePool()).getMessageType(params.getString("msgTypeRREQ"));
	    msgTypeRREP = (runtime.getMessagePool()).getMessageType(params.getString("msgTypeRREP"));
	    msgTypeRERR = (runtime.getMessagePool()).getMessageType(params.getString("msgTypeRERR"));
	}catch(ParamDoesNotExistException pdnee) {
	    pdnee.printStackTrace();
	    throw new RuntimeException("Exception reading a parameter in AODV.");
	}
	
	NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
	ACTIVE_ROUTE_TIMEOUT = 2 * NET_TRAVERSAL_TIME;
	PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;
	DELETE_PERIOD = 5 * NET_TRAVERSAL_TIME;
    }
    
    /**
     * Start the functionality of the AODV layer.
     * <p>
     * A reference to the NeighborService is created.
     */
    public void startup() {
	start();
	nm = (NeighborService)runtime.getService("Neighboring");
	
    }	
    
	/**
	 * This method decides whether to retransmit a message, to propagate
	 * it up in the stack or to call other methods to handle it (in case of an AODV message). 
	 * <p>
	 * @param msg The message to handle.
	 */
    public void handleMessage(Message msg) {
    	setUpToDateRoutingTable();
	if(msg == null) {
	    System.out.println("> #(handleMessage) message is null");
	}
	if((msg.getNextHop()==runtime.getNodeID())||(msg.getNextHop()==0)){
	    
	    if((int)msg.getType()==(int)msgTypeRREQ){
		handleRREQMessage((RREQmsg)msg);
	    } else if((int)msg.getType()==(int)msgTypeRREP){
		handleRREPMessage((RREPmsg)msg);
	    } else if((int)msg.getType()==(int)msgTypeRERR){	
		handleRERRMessage((RERRmsg)msg);
	    } else {
		
		msg.setTTL(msg.getTTL()-1);
		
		if(msg.getDstNode()==runtime.getNodeID()){	
		    super.handleMessage(msg);	
		} else{
		    
		    try {
			if((msg.getNextHop()==0)||(msg.getDstNode()==0)){
			    
			    msg.createCopy();//the msg will be freed twice : in the layer which will treat it and here when it would have been broadcasted.
			    super.handleMessage(msg); 
			    if(msg.getTTL()>0){
				msg.setPreviousHop(runtime.getNodeID());
				super.sendMessage(msg);
			    }else{
				(runtime.getMessagePool()).freeMessage(msg);
				
			    }
       			}else{
			    //System.out.println("forwardMessage, le message ne m'est pas destine : " +msg);
			    
			    forwardMessage(msg);
			}
		    }
		    catch(Exception ex) {
    			ex.printStackTrace();
			System.out.println("(forwardMessage() in AODV)Could not send message : " + ex.getMessage());
		    }
		}
	    }
      	}else{
	    (runtime.getMessagePool()).freeMessage(msg);
	}
    }
    
    
    /**
     * This method handles Route Request messages. 
     * It follows the AODV algorithm instructions. 
     * @param rreqmsg The Route Request message to handle.
     */
    private synchronized void handleRREQMessage(RREQmsg rreqmsg) {
	//System.out.println("HandleRREQMessage. hopcount : "+(rreqmsg.getHOP_COUNT()+1)+" src : "+rreqmsg.getSrcNode()+" dst : "+rreqmsg.getDstNode());
	/*for(int j=0;j<(rreqmsg.getIDandSEQNBpairs()).size();j++){
	    System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rreqmsg.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rreqmsg.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
	    
	}*/
	rreqmsg.setHOP_COUNT(rreqmsg.getHOP_COUNT()+1);
	searchReverseRouteRREQ(rreqmsg);
	//printStateOfRoutingTable();
	updateRREQsentIDsPairs();
	
	if (!rreqsentIDsPairsContains(rreqmsg.getSrcNode(), rreqmsg.getRREQ_ID())){//if9
	    
	    RREQsentIDsPairs.add(new RREQsentIDsPair(rreqmsg.getSrcNode(), rreqmsg.getRREQ_ID(), System.currentTimeMillis() + PATH_DISCOVERY_TIME));
	    //System.out.println("RREQsentIDspair added. SrcNode : "+rreqmsg.getSrcNode()+" and RREQ ID : "+rreqmsg.getRREQ_ID());
	    if (rreqmsg.getDstNode()==runtime.getNodeID()) {//if2
		sendRREP(rreqmsg);
	    }//if2
	    else {//else2
		
		if (rreqmsg.getTTL()>1){//if4
		    forwardRREQmsg(rreqmsg);
		}//if4
		else {//else4
		    (runtime.getMessagePool()).freeMessage(rreqmsg);
		}//else4
	    }//else2		
	}//if9
	else {//else9
	    (runtime.getMessagePool()).freeMessage(rreqmsg);
	}//else9
    }
    
    
    /**
     * This method handles Route Reply messages. 
     * It follows the AODV algorithm instructions. 
     * @param rrepmsg The Route Reply message to handle.
     */
    private synchronized void handleRREPMessage(RREPmsg rrepmsg) { //On suppose que la source a mis son IDandSEQNBpair dans le RREP
	
	rrepmsg.setHOP_COUNT(rrepmsg.getHOP_COUNT()+1);
	rrepmsg.setTTL(rrepmsg.getTTL()-1);
	//System.out.println("taille de l'arraylist du RREP : "+(rrepmsg.getIDandSEQNBpairs()).size());
	searchReverseRouteRREP(rrepmsg);
	if (rrepmsg.getDstNode()==runtime.getNodeID()){//if3
	    //System.out.println("handleRREPMessage : Le Node "+rrepmsg.getSrcNode()+" nous a repondu");
	    //printStateOfRoutingTable();
	    if (rreqSentBufferContainsRREQ(rrepmsg.getSrcNode())){
		emptyRREQsSentBufferOfRREQ(rrepmsg.getSrcNode());
		try{
		    long dst = rrepmsg.getSrcNode();
		    sendMessageWhoseDstHasApath(dst);
		}catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	    (runtime.getMessagePool()).freeMessage(rrepmsg);
	}//if3
	else {//else3
	    
	    if(rrepmsg.getTTL()>0){
		forwardRREP(rrepmsg);
		
	    }else{
		(runtime.getMessagePool()).freeMessage(rrepmsg);
	    }
	}//else3
	
    }
    
    /**
     * This method handles Route Error messages. 
     * It follows the AODV algorithm instructions. 
     * @param rerrmsg The Route Error message to handle.
     */
    private synchronized void handleRERRMessage(RERRmsg rerrmsg) {
	//System.out.println("handleRERRMessage");
	boolean RERRfurther=false;
	setUpToDateRoutingTable();
	if (getRoutingTableEntryFromID(rerrmsg.getPreviousHop())==null){
	    createRoutingTableEntryFromRERR(rerrmsg);
	}
	ArrayList Unreachable_Destinations_IDSandSEQNBSpairs = rerrmsg.getUnreachable_Destinations_IDSandSEQNBSpairs();
	for(int i=0; i<Unreachable_Destinations_IDSandSEQNBSpairs.size(); i++) {//for1
	    IDandSEQNBpair IDandSEQNBpairOfRERR =(IDandSEQNBpair)(rerrmsg.getUnreachable_Destinations_IDSandSEQNBSpairs()).get(i);
	    
	    RoutingTableEntry rte = getRoutingTableEntryFromID(IDandSEQNBpairOfRERR.getNODE_ID());
	    if(rte!=null) {//if1
		if (rte.getnextHop()==rerrmsg.getSrcNode()) {//if2
		    if ((IDandSEQNBpairOfRERR.getNODE_SEQNB()==0)||
			(IDandSEQNBpairOfRERR.getNODE_SEQNB()>rte.getDEST_SEQNB())) {//if3
			//System.out.println("A route has been invalidated due to a RERR. ID of the dst of the route : "+rte.getID_DEST());
			rte.setVALID_ENTRY(false);
			RERRfurther=true;
			rte.setDEST_SEQNB(Math.max(rte.getDEST_SEQNB(), IDandSEQNBpairOfRERR.getNODE_SEQNB()));
			Unreachable_Destinations_IDSandSEQNBSpairs = invalidateRoutesWithSameNextHop(rte.getnextHop(), Unreachable_Destinations_IDSandSEQNBSpairs);
		    }//if3
		}//if2
	    }//if1
	}//for1
	if(RERRfurther){
	    broadcastRERR(Unreachable_Destinations_IDSandSEQNBSpairs);
	}
    }
    
    
    /**
     * This method broadcast a Route Error Message containing unreachable destinations. 
     * @param Unreachable_Destinations_IDSandSEQNBSpairs The ArrayList which contains the unreachable destinations, which comes from the Route Error Message which was received.
     */
    private void broadcastRERR(ArrayList Unreachable_Destinations_IDSandSEQNBSpairs){
	//System.out.println("broadcastRERR");
	Message msgToSend = null;
	try {
	    RERRmsg rerrmsg = (RERRmsg)(runtime.getMessagePool()).getMessage(msgTypeRERR);	
	    (rerrmsg.getUnreachable_Destinations_IDSandSEQNBSpairs()).clear();
	    rerrmsg.setUnreachable_Destinations_IDSandSEQNBSpairs(new ArrayList(Unreachable_Destinations_IDSandSEQNBSpairs));
	    rerrmsg.setDstNode(0);
	    rerrmsg.setSrcNode(runtime.getNodeID());
	    rerrmsg.setPreviousHop(runtime.getNodeID());
	    rerrmsg.setNextHop(0);
	    rerrmsg.setTTL(1);
	    
	    msgToSend=rerrmsg;
	} catch(Exception ex) {
	    System.out.println(">(broadcastRERR() in AODV) # Could not create Message: " + ex.getMessage());
	}
	
	try {
	    super.sendMessage(msgToSend);
	} catch(Exception ex) {
	    System.out.println(">(broadcastRERR in AODV) # Could not send message: " + ex.getMessage());
	}
    }
    
    
    /**
     * This method sends a Route Reply Message in response to a Route Request Message whose destination is this node. 
     * @param rreqmsg The Route Request Message which was received.
     */
    private void sendRREP(RREQmsg rreqmsg){
	//System.out.println("sendRREP");
	Message msg = null;
	//System.out.println("SrcNode of the RREQ :"+rreqmsg.getSrcNode());
	try {
	    RREPmsg rrepmsg = (RREPmsg)(runtime.getMessagePool()).getMessage(msgTypeRREP);	
	    (rrepmsg.getIDandSEQNBpairs()).clear();
	    rrepmsg.setDstNode(rreqmsg.getSrcNode());
	    rrepmsg.setSrcNode(runtime.getNodeID());
	    rrepmsg.setPreviousHop(runtime.getNodeID());
	    rrepmsg.setNextHop(rreqmsg.getPreviousHop());
	    rrepmsg.setTTL(5);
	    rrepmsg.setHOP_COUNT(0);
	    rrepmsg.setIDandSEQNBpairs(new ArrayList(rreqmsg.getIDandSEQNBpairs()));
	    
	    //System.out.println("IDandSEQNBpairs of the new RREP: \n");
	    
	    /*for(int j=0;j<(rrepmsg.getIDandSEQNBpairs()).size();j++){
		System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
	    }*/
	    
	    //System.out.println("taille de l'arraylist a la creation du RREP : "+(rrepmsg.getIDandSEQNBpairs()).size());
	    rrepmsg.setSrcOfRREQseqnb(rreqmsg.getSrcOfRREQseqnb());
	    
	    if (rreqmsg.getDestOfRREQseqnb()>=SeqNumberAndRREQIDsControllerObjet.getSeqnb()){	
		SeqNumberAndRREQIDsControllerObjet.setSeqnb(1+rreqmsg.getDestOfRREQseqnb());
	    }
	    
	    rrepmsg.setDestOfRREQseqnb(SeqNumberAndRREQIDsControllerObjet.getSeqnb());
	    msg=rrepmsg;
	} catch(Exception ex) {
	    System.out.println(">(sendRREP in AODV) # Could not create Message: " + ex.getMessage());
	}
	
	/*printStateOfRoutingTable();
	  System.out.println("\nSend RREP message : ");
	  System.out.println("DstNode : "+((RREPmsg)msg).getDstNode());
	  System.out.println("NextHop : "+((RREPmsg)msg).getNextHop());
	  System.out.println("DestOfRREQseqnb : "+((RREPmsg)msg).getDestOfRREQseqnb());
	  System.out.println("SrcOfRREQseqnb : "+((RREPmsg)msg).getSrcOfRREQseqnb()+"\n");
	*/
	try {
	    super.sendMessage(msg);
	} catch(Exception ex) {
	    System.out.println(">(sendRREP() in AODV) # Could not send message: " + ex.getMessage());
	}
	
	(runtime.getMessagePool()).freeMessage(rreqmsg);
	
    }
    
    /**
     * This method adds a Route Requests Message to the buffer of the Route Request which are being sent, in order to find a path to a node .
     *
     * @param ID_DEST The ID of the destination node for which a path is searched.
     */
    private void addRREQtoRREQsentBuffer(long ID_DEST) {
	//System.out.println("addRREQtoRREQsentBuffer");
	
	// obtain a message object
	Message msg = null;
	try {
	    RREQmsg rm = (RREQmsg)(runtime.getMessagePool()).getMessage(msgTypeRREQ);
	    (rm.getIDandSEQNBpairs()).clear();
	    RoutingTableEntry rte = getRoutingTableEntryFromID(ID_DEST);
	    if(rte!=null){
		rm.setDestOfRREQseqnb(rte.getDEST_SEQNB());	
	    } else {
		rm.setDestOfRREQseqnb(0);	
	    }
	    SeqNumberAndRREQIDsControllerObjet.setSeqnb(SeqNumberAndRREQIDsControllerObjet.getSeqnb()+1);					
	    rm.setSrcOfRREQseqnb(SeqNumberAndRREQIDsControllerObjet.getSeqnb());
	    //SeqNumberAndRREQIDsControllerObjet.incrementRREQ_ID(); NOT NEEDED CAUSE IT'S DONE EVERYTIME THIS RREQ IS SENT
	    rm.setRREQ_ID(SeqNumberAndRREQIDsControllerObjet.getRREQ_ID());
	    rm.setDstNode(ID_DEST);
	    rm.setNextHop(0);
	    rm.setPreviousHop(runtime.getNodeID());
	    rm.setTTL(NET_DIAMETER); // maybe 255
	    rm.setHOP_COUNT(0);
	    rm.setSrcNode(runtime.getNodeID());
	    //System.out.println("Je rajoute un reqmsg avec source : "+rm.getSrcNode()+" et dest : "+rm.getDstNode());
	    ArrayList IDandSEQNBpairsobj = rm.getIDandSEQNBpairs();
	    IDandSEQNBpairsobj.add(new IDandSEQNBpair(runtime.getNodeID(), rm.getSrcOfRREQseqnb()));				
	    rm.setIDandSEQNBpairs(IDandSEQNBpairsobj);
	    /*
	      for(int j=0;j<(rm.getIDandSEQNBpairs()).size();j++){
	      System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rm.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rm.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
	      
	      }*/
	    msg = rm;
	} catch(Exception ex) {
	    System.out.println(">(addRREQtoRREQsentBuffer in AODV) # Could not create Message: " + ex.getMessage());
	}
	updateRREQsentIDsPairs();
	//We divide NET_TRAVERSAL_TIME by 2 because it will be multiplied by 2 even the first time the RREQ is sent
	RREQsSentBuffer.add(new RREQsentBufferEntry((RREQmsg)msg, NET_TRAVERSAL_TIME/2, System.currentTimeMillis()));
	
	if(!threadRREQrunning) {
	    SendThread SendRREQThread=new SendThread(this);
	    SendRREQThread.start();
	    threadRREQrunning=true;
	}
    }
    
    /**
     * This method updates the Table which contains the IDs of the Route Request Messages which were sent or received.
     * It removes entries which are out of date.
     */
    public void updateRREQsentIDsPairs(){
	//System.out.println("updateRREQsentIDsPairs");
	for (int i=RREQsentIDsPairs.size()-1; i>=0; i--){
	    if (((RREQsentIDsPair)(RREQsentIDsPairs.get(i))).getLifeTime() < System.currentTimeMillis()){
		RREQsentIDsPairs.remove(i);
		//System.out.println("entree removee no : "+i +" in RREQsentIDsPairs");
	    }
	}
	//printStateOfRREQsentIDsPairs();
    }
    
    /**
     * This method updates the Buffer which contains the Route Request Messages which are being sent.	
     * It removes entries which are out of date.
     */
    public void updateRREQsSentBuffer(){
	//System.out.println("updateRREQsSentBuffer");
	if (!RREQsSentBuffer.isEmpty()) {
	    for (int i=RREQsSentBuffer.size()-1;i>=0;i--){
		RREQsentBufferEntry rste = (RREQsentBufferEntry)RREQsSentBuffer.get(i);
		
		if ((rste.getVALID_ENTRY()==false)&&(rste.getnextTimeToBeSent()<=System.currentTimeMillis())) {
		    removeMsgsWhichAreWaitingForThisDest(((RREQmsg)rste.getRREQ()).getDstNode());
		    (runtime.getMessagePool()).freeMessage(((RREQsentBufferEntry)RREQsSentBuffer.get(i)).getRREQ());
		    RREQsSentBuffer.remove(i);
		    //System.out.println("Entree enlevee de RREQsSentBuffer");
		    /*In the future, we might want to tell someone that we couldn't reach a node.
		     *As for now, we don't tell anyone.*/
		}
		
		if((rste.getVALID_ENTRY())&&(rste.getnextTimeToBeSent()<=System.currentTimeMillis())) {
		    rste.setmustBeSentAtOnce(true);
		}
		
		if (rste.getcurrentRREQtries()==RREQ_TRIES) {
		    rste.setVALID_ENTRY(false);
		}
		
	    }
	}
    }
    
    /**
     * This method calculate the time for which the thread which sends the Route Request Messages has to sleep next time.
     * @return The time for which the thread which sends the Route Request Messages has to sleep next time or -1 if the buffer of Route Request Messages to send is empty.
     **/
    public long calculateTimeToSleep(){
	updateRREQsSentBuffer();
	if(!RREQsSentBuffer.isEmpty()) {
	    long TimeToSleep = 5000;	
	    for (int i=0;i<RREQsSentBuffer.size();i++) {
		RREQsentBufferEntry rste = (RREQsentBufferEntry)RREQsSentBuffer.get(i);
		if(rste.getVALID_ENTRY()){
		    TimeToSleep=Math.min(TimeToSleep, Math.max(rste.getnextTimeToBeSent()-System.currentTimeMillis(), 0));
		}
	    }
	    //System.out.println("calculateTimeToSleep returns : "+TimeToSleep);
	    return TimeToSleep;
	}
	else{
	    threadRREQrunning=false;	
	    //System.out.println("calculateTimeToSleep returns -1");
	    return -1;
	}
    }
    
    
    /**
     * This method removes the Route Request from the buffer which are sent to find a path to the ID which is given as a parameter.
     * @param ID_DEST The ID of the destination node for which the Route Request which are sent to find a path to are to be removed from the buffer.
     */
    private void removeMsgsWhichAreWaitingForThisDest(long ID_DEST){
	
	if(!MsgsWaitingForRoute.isEmpty()){
	    for(int i=MsgsWaitingForRoute.size()-1; i>=0;i--){
		if (((Message)MsgsWaitingForRoute.get(i)).getDstNode()==ID_DEST) {
		    //System.out.println("removeMsgsWhichAreWaitingForThisDest : On a pas pu trouver de route pour la dest de ce message : "+((Message)MsgsWaitingForRoute.get(i)).getDstNode());
		    MsgsWaitingForRoute.remove(i);
		}
	    }
	}
    }
    
    
    /** This method returns true if the Table of the IDs of the Route Request Messages which were sent or received contains an entry with the IDs given as parameters.
     * If there's an entry, it's lifetime is set to : current time + PATH_DISCOVERY_TIME. 
     * @param SrcNode The ID of the node which is to be looked for in the Table of the IDs of the Route Requests which were sent or received.
     * @param RREQ_ID The ID of the Route Request Message which is to be looked for in the Table of the IDs of the Route Requests which were sent or received.
     */
    private boolean rreqsentIDsPairsContains(long SrcNode, long RREQ_ID){
	
	for (int i=0; i < RREQsentIDsPairs.size(); i++){
	    if ((((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getSrcNode()== SrcNode)&&
		(((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getRREQ_ID()==RREQ_ID)&&
		(((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getLifeTime()>=System.currentTimeMillis())){
		((RREQsentIDsPair)RREQsentIDsPairs.get(i)).setLifeTime(System.currentTimeMillis()+PATH_DISCOVERY_TIME);
		//System.out.println("rreqsentIDsPairsContains returns true");
		return true;
	    }//if
	}//for
	//System.out.println("rreqsentIDsPairsContains returns false");
	return false;
    }
    
    
    /** If the routing table contains an entry for the destination ID given as a parameter, this method returns the entry.
     * If it does not contain an entry which matches, it returns null.
     * @param dst The ID of the destination for which we want to search for an entry in the Routing Table.
     * @return An entry of the Routing Table whose destination ID is the parameter given if it exists, null otherwise.
     */
    private RoutingTableEntry getRoutingTableEntryFromID(long dst){
	for (int i=0;i<RoutingTable.size();i++){
	    RoutingTableEntry rte = (RoutingTableEntry)RoutingTable.get(i);
	    if (rte.getID_DEST() == dst){
		//System.out.println("getRoutingTableEntryFromID " + dst +" returns entry no : "+i);
		return rte;
	    }
	}
	//System.out.println("getRoutingTableEntryFromID " + dst +" returns null");
	return null;
    }
    
    
    /** This method checks if it has to create a new entry in the Routing Table or to update one existing for each of the nodes through which the Route Request went 
     * @param rreqmsg The Route Request Message which was received
     */
    private void searchReverseRouteRREQ(RREQmsg rreqmsg){
	setUpToDateRoutingTable();
	ArrayList IDandSEQNBpairs = rreqmsg.getIDandSEQNBpairs();
	for (int i=0; i<IDandSEQNBpairs.size(); i++){
	    IDandSEQNBpair IDandSEQNBpairObject=(IDandSEQNBpair)IDandSEQNBpairs.get(i);
	    RoutingTableEntry rte = getRoutingTableEntryFromID(IDandSEQNBpairObject.getNODE_ID());
	    if (rte==null){
		RoutingTable.add(new RoutingTableEntry(IDandSEQNBpairObject.getNODE_ID(), IDandSEQNBpairObject.getNODE_SEQNB(), System.currentTimeMillis()+ACTIVE_ROUTE_TIMEOUT, rreqmsg.getPreviousHop(), IDandSEQNBpairs.size()-i, true));
	    }else{
		if ((IDandSEQNBpairObject.getNODE_SEQNB()>rte.getDEST_SEQNB()) ||
		    ((IDandSEQNBpairObject.getNODE_SEQNB()==rte.getDEST_SEQNB())&&(IDandSEQNBpairs.size()-i < rte.getHOP_COUNT())) || 
		    (rte.getDEST_SEQNB()==0) || 
		    ((IDandSEQNBpairObject.getNODE_SEQNB()==rte.getDEST_SEQNB())&&(rte.getVALID_ENTRY()==false))) {//if3
		    rte.setDEST_SEQNB(IDandSEQNBpairObject.getNODE_SEQNB());
		    rte.setVALID_ENTRY(true);
		    rte.setnextHop(rreqmsg.getPreviousHop());
		    rte.setHOP_COUNT(IDandSEQNBpairs.size()-i);
		    rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
		}//if3
		else {
		    rte.setVALID_ENTRY(true);
		    rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
		}
	    }
	}	
    }
    
    
    /** This method checks if it has to create a new entry in the Routing Table or to update one existing for each of the nodes through which the Route Reply went
     * @param rrepmsg The Route Reply Message which was received
     */
    private void searchReverseRouteRREP(RREPmsg rrepmsg){
	setUpToDateRoutingTable();
	
	ArrayList IDandSEQNBpairs = rrepmsg.getIDandSEQNBpairs();
	int noOfThisNodeInRREPsIDandSEQNBpairs = getnoOfIDandSEQNBpairOfTheNode(IDandSEQNBpairs);
	
	if(noOfThisNodeInRREPsIDandSEQNBpairs==-1){
	    System.out.println("Le RREP ne repasse pas par ou il est venu! Il y a une erreur quelque part!!!!");
	    noOfThisNodeInRREPsIDandSEQNBpairs=10000;
	}
	
	for (int i=0; i<IDandSEQNBpairs.size(); i++){
	    IDandSEQNBpair IDandSEQNBpairObject=(IDandSEQNBpair)IDandSEQNBpairs.get(i);
	    RoutingTableEntry rte = getRoutingTableEntryFromID(IDandSEQNBpairObject.getNODE_ID());
	    int hcs=i-noOfThisNodeInRREPsIDandSEQNBpairs;
	    long nexthop=0;
	    if(hcs<0){
		nexthop=((IDandSEQNBpair)IDandSEQNBpairs.get(noOfThisNodeInRREPsIDandSEQNBpairs-1)).getNODE_ID();
	    }else{
		if(hcs>0){
		    nexthop=((IDandSEQNBpair)IDandSEQNBpairs.get(noOfThisNodeInRREPsIDandSEQNBpairs+1)).getNODE_ID();
		}else{
		    nexthop=runtime.getNodeID();
		}
	    }
	    
	    if (rte==null){
		RoutingTable.add(new RoutingTableEntry(IDandSEQNBpairObject.getNODE_ID(), IDandSEQNBpairObject.getNODE_SEQNB(), System.currentTimeMillis()+ACTIVE_ROUTE_TIMEOUT, nexthop, Math.abs(hcs), true));
	    }else{
		if ((IDandSEQNBpairObject.getNODE_SEQNB()>rte.getDEST_SEQNB()) ||
		    ((IDandSEQNBpairObject.getNODE_SEQNB()==rte.getDEST_SEQNB())&&(Math.abs(hcs) < rte.getHOP_COUNT())) || 
		    (rte.getDEST_SEQNB()==0) || 
		    ((IDandSEQNBpairObject.getNODE_SEQNB()==rte.getDEST_SEQNB())&&(rte.getVALID_ENTRY()==false))) {//if3
		    rte.setDEST_SEQNB(IDandSEQNBpairObject.getNODE_SEQNB());
		    rte.setVALID_ENTRY(true);
		    rte.setnextHop(nexthop);
		    rte.setHOP_COUNT(Math.abs(hcs));
		    rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
		}//if3
		else {
		    rte.setVALID_ENTRY(true);
		    rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
		}
	    }
	}
	RoutingTableEntry rte = getRoutingTableEntryFromID(rrepmsg.getSrcNode());
	if (rte!=null){
	    if ((rrepmsg.getDestOfRREQseqnb()>rte.getDEST_SEQNB()) ||
		((rrepmsg.getDestOfRREQseqnb()==rte.getDEST_SEQNB())&&(rrepmsg.getHOP_COUNT() < rte.getHOP_COUNT())) || 
		(rte.getDEST_SEQNB()==0) || 
		((rrepmsg.getDestOfRREQseqnb()==rte.getDEST_SEQNB())&&(rte.getVALID_ENTRY()==false))) {//if3
		rte.setDEST_SEQNB(rrepmsg.getDestOfRREQseqnb());
		rte.setVALID_ENTRY(true);
		rte.setnextHop(rrepmsg.getPreviousHop());
		rte.setHOP_COUNT(rrepmsg.getHOP_COUNT());
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
	    }//if3
	    else {
		rte.setVALID_ENTRY(true);
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
	    }
	}
	else{
	    RoutingTable.add(new RoutingTableEntry(rrepmsg.getSrcNode(), rrepmsg.getDestOfRREQseqnb(), System.currentTimeMillis()+ACTIVE_ROUTE_TIMEOUT, rrepmsg.getPreviousHop(), rrepmsg.getHOP_COUNT(), true));
	}
    }
    
    
    
    /** This method sets the Routing Table up to date :
     * <ul>
     * <li> - It checks the Neighboring Table of the Service to see which neighbors are relevants </li>
     * <li> - It invalidates the entries whose next hop has been invalidated </li>
     * <li> - It invalidates valid entries whose lifetime is <=0 </li>
     * <li> - Finally it removes entry which are invalid and whose lifetime is <=0 </li>
     * </ul>
     *
     */
    public void setUpToDateRoutingTable(){  
	//System.out.println("setUpToDateRoutingTable");
	NeighborTable neighbors = nm.getTable();
	long[] neighborsIDs = neighbors.getNeighborsIds();
	//default value in array is false
	boolean[] neighborIDisInTable=new boolean[neighborsIDs.length];
	if(!RoutingTable.isEmpty()){
	    for(int j=RoutingTable.size()-1; j>=0; j--) {
		boolean entryHasAvalidNextHop=false;	
		RoutingTableEntry rte = (RoutingTableEntry)RoutingTable.get(j);
		
		//checking up neighbors
		if(neighbors!=null){
		    for (int i=0; i < neighborsIDs.length; i++) {//for1
			
			if (rte.getID_DEST()==neighborsIDs[i]){//if4
			    if (rte.getVALID_ENTRY()==false){
				rte.setVALID_ENTRY(true);
				rte.setDEST_SEQNB(0);
			    }
			    neighborIDisInTable[i]=true;
			    rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
			}//if4
			if (rte.getnextHop()==neighborsIDs[i]) {//if5
			    entryHasAvalidNextHop=true;
			}//if5
		    }//for1
		}
		//if a route uses a invalid nexthop then the route is invalidated and the seq_nb incremented
		if (!entryHasAvalidNextHop) {
		    if(rte.getVALID_ENTRY()){
			rte.setVALID_ENTRY(false);
			rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + DELETE_PERIOD);
			rte.setDEST_SEQNB(rte.getDEST_SEQNB()+1);
			//System.out.println("\n Path invalidated : "+rte.getID_DEST()+"\n");
		    }
		}
		//System.out.println("\n Lifetime route "+rte.getID_DEST()+" : " +(rte.getLIFETIME_ACTIVE_ROUTE()-System.currentTimeMillis()) +"\n");
		if (rte.getLIFETIME_ACTIVE_ROUTE() <= System.currentTimeMillis()){
		    if (rte.getVALID_ENTRY()==false) {
			RoutingTable.remove(j);	
			//System.out.println("\n Path removed from table : "+rte.getID_DEST()+"\n");
		    }
		    else{
			rte.setVALID_ENTRY(false);
			rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + DELETE_PERIOD);
		    }	
		}
	    }//for
	}//iftableempty
	
	//update of table concerning neighbors
	if(neighborIDisInTable.length!=0){
	    for (int i=0; i<neighborIDisInTable.length; i++){//for2
		if (neighborIDisInTable[i]==false){
		    RoutingTable.add(new RoutingTableEntry(neighborsIDs[i], 0, System.currentTimeMillis()+ACTIVE_ROUTE_TIMEOUT, neighborsIDs[i], 1, true));
		}
	    }//for2
	}
	//printStateOfRoutingTable();
    }
    
    // Method to print the state of the Routing Table
    void printStateOfRoutingTable(){
	System.out.println("\n"+"-----------------------------------------------------\n");
	System.out.println("Etat de la table de routage : \n");
	System.out.println("Taille de la table de routage : "+RoutingTable.size()+"\n");
	for (int j=0; j<RoutingTable.size(); j++){
	    System.out.println("dst : "+ ((RoutingTableEntry)RoutingTable.get(j)).getID_DEST());
	    System.out.println("next hop : "+ ((RoutingTableEntry)RoutingTable.get(j)).getnextHop());
	    System.out.println("dst seqnb : "+ ((RoutingTableEntry)RoutingTable.get(j)).getDEST_SEQNB());
	    System.out.println("lifetime : "+ ((RoutingTableEntry)RoutingTable.get(j)).getLIFETIME_ACTIVE_ROUTE());
	    System.out.println("hopcount : "+ ((RoutingTableEntry)RoutingTable.get(j)).getHOP_COUNT());
	    System.out.println("valid entry : "+ ((RoutingTableEntry)RoutingTable.get(j)).getVALID_ENTRY()+"\n");
	    System.out.println("-----------------------------------------------------\n");
	}
    }
    
    
    
    /** This method invalidates the entries of the Routing Table which use the next hop given in parameter and updates the ArrayList of unreachables nodes given in parameter
     * @param ID_Next_Hop The ID of the next hop for which we want to search for an entry in the Routing Table
     * @param Unreachable_Destinations_IDSandSEQNBSpairs An ArrayList which contains the unreachable nodes contained in a Route Error message
     * @return The maybe updated ArrayList which contains the unreachable nodes, which was contained in a Route Error message
     */
    private ArrayList invalidateRoutesWithSameNextHop(long ID_Next_Hop, ArrayList Unreachable_Destinations_IDSandSEQNBSpairs){
	//System.out.println("invalidateRoutesWithSameNextHop");
	for(int i=0; i<RoutingTable.size(); i++){
	    RoutingTableEntry rte = (RoutingTableEntry)RoutingTable.get(i);
	    if ((rte.getnextHop()==ID_Next_Hop)&&(rte.getVALID_ENTRY())){
		rte.setVALID_ENTRY(false);
		rte.setDEST_SEQNB(rte.getDEST_SEQNB()+1);
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + DELETE_PERIOD);
		if(!unreachableDestinationsIDSandSEQNBSpairsContainsID(rte.getID_DEST(), Unreachable_Destinations_IDSandSEQNBSpairs)){
		    Unreachable_Destinations_IDSandSEQNBSpairs.add(new IDandSEQNBpair(rte.getID_DEST(), rte.getDEST_SEQNB()));
		}
	    }
	}//for
	return Unreachable_Destinations_IDSandSEQNBSpairs;
    }
    
    /** This method checks if the ArrayList of unreachables nodes given in parameter contains an entry with the node ID given in parameter 
     * @param ID The ID of the node for which an entry has to be searched for in the ArrayList given in parameter
     * @param al An ArrayList which contains the unreachable nodes contained in a Route Error Message
     */
    private boolean unreachableDestinationsIDSandSEQNBSpairsContainsID(long ID, ArrayList al){
	//System.out.println("unreachableDestinationsIDSandSEQNBSpairsContainsID");
	for(int i=0;i<al.size();i++){
	    if(((IDandSEQNBpair)al.get(i)).getNODE_ID()==ID){
		return true;
	    }		
	}
	return false;
    }
    
    
    /** This method forwards a Route Reply Message which was received 
     * @param rrepmsg The Route Reply received
     * */
    private void forwardRREP(RREPmsg rrepmsg) {
	//System.out.println("forwardRREP, node ID of the source of the RREP : "+rrepmsg.getSrcNode()+" ID of the dest of the RREP : "+rrepmsg.getDstNode()+" TTL : "+rrepmsg.getTTL()+" HopCount : "+rrepmsg.getHOP_COUNT());
	
	/*for(int j=0;j<(rrepmsg.getIDandSEQNBpairs()).size();j++){
	    System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());  
	}*/
	rrepmsg.setPreviousHop(runtime.getNodeID());
	if ((rrepmsg.getIDandSEQNBpairs()).size()==0){
	    rrepmsg.setNextHop(((RoutingTableEntry)getRoutingTableEntryFromID(rrepmsg.getDstNode())).getnextHop());
	    //System.out.println("size of the ArrayList of the RREP = 0");
	}
	else{
	    int noOfIDandSEQNBpairOfTheNode = getnoOfIDandSEQNBpairOfTheNode(rrepmsg.getIDandSEQNBpairs());
	    /*
	      System.out.println("APRES");
	      for(int j=0;j<(rrepmsg.getIDandSEQNBpairs()).size();j++){
	      System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rrepmsg.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
	      
	      }*/
	    rrepmsg.setNextHop(((IDandSEQNBpair)((rrepmsg.getIDandSEQNBpairs()).get(noOfIDandSEQNBpairOfTheNode-1))).getNODE_ID());
	}
	
	
	try {
	    //System.out.println("On a forward un RREP : "+rrepmsg);
	    //System.out.println("Next hop : "+rrepmsg.getNextHop());
	    super.sendMessage(rrepmsg);
	    
	} catch(Exception ex) {
	    System.out.println(">(forwardRREP in AODV) # Could not send message: " + ex.getMessage());
	}
	
	
    }
    
    
    /** This method forwards a Route Request Message which was received and whose destination isn't this node 
     * @param rreqmsg The Route Request Message which was received
     * */
    private void forwardRREQmsg(RREQmsg rreqmsg){
	//System.out.println("forwardRREQmsg");
	rreqmsg.setPreviousHop(runtime.getNodeID());
	rreqmsg.setTTL(rreqmsg.getTTL()-1);
	RoutingTableEntry rte = getRoutingTableEntryFromID(rreqmsg.getDstNode());
	if(rte!=null){
	    rreqmsg.setDestOfRREQseqnb(Math.max(rreqmsg.getDestOfRREQseqnb(), rte.getDEST_SEQNB()));
	}
	if(getnoOfIDandSEQNBpairOfTheNode(rreqmsg.getIDandSEQNBpairs())!=-1){
	    (runtime.getMessagePool()).freeMessage(rreqmsg);
    	}
    	else{
	    ArrayList IDandSEQNBpairs=rreqmsg.getIDandSEQNBpairs();
	    IDandSEQNBpairs.add(new IDandSEQNBpair(runtime.getNodeID(), SeqNumberAndRREQIDsControllerObjet.getSeqnb()));				
	    rreqmsg.setIDandSEQNBpairs(IDandSEQNBpairs);
	    
	    
	    /*for(int j=0;j<(rreqmsg.getIDandSEQNBpairs()).size();j++){
		System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)(rreqmsg.getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)(rreqmsg.getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
	    }*/
	    
	    // send message through the AsynchronousLayer's default method
	    
	    try {
		super.sendMessage(rreqmsg);
	    } catch(Exception ex) {
		System.out.println("> #(AODV.forwardRREQmsg) Could not send message: " + ex.getMessage());
	    }
	}
    	
    }
    
    
    /** This method returns the position of the ID and sequence number pair of this node in the ArrayList which comes from a Route Reply Message given in parameter
     * @param IDandSEQNBpairs The ArrayList which was contained in the Route Reply which was received
     * @return the position of the ID and sequence number pair of this node in the ArrayList which comes from the Route Reply Message given in parameter
     * */
    private int getnoOfIDandSEQNBpairOfTheNode(ArrayList IDandSEQNBpairs){
	
	for(int i=0;i<IDandSEQNBpairs.size();i++){ 
	    IDandSEQNBpair IDandSEQNBpairobj = (IDandSEQNBpair)IDandSEQNBpairs.get(i);
	    if (IDandSEQNBpairobj.getNODE_ID()==runtime.getNodeID()) {
		//System.out.println("getnoOfIDandSEQNBpairOfTheNode returns :"+i);	
		return i;
	    }
	}//for
	//System.out.println("getnoOfIDandSEQNBpairOfTheNode returns : -1");
	return -1;
    }
    
    /** This method creates an entry in the Routing Table with informations contained in a Route Error which was received
     * @param rerrmsg The Route Error which was received
     * */
    private void createRoutingTableEntryFromRERR(RERRmsg rerrmsg) {
	//System.out.println("createRoutingTableEntryFromRERR");
	RoutingTable.add(new RoutingTableEntry(rerrmsg.getPreviousHop(), 0, System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT, rerrmsg.getPreviousHop(), 1, true));
    }
    
    /** This method creates a new Route Error Message, due to the fact that a message to forward was received and that this node's Routing Table doesn't know the destination of the message 
     * @param msg The Message whose destination isn't in the Routing Table which should be forwarded
     * @return The Route Error Message to send
     * */
    private Message getNewRERRtoSend(Message msg, RoutingTableEntry rte){
	//System.out.println("getNewRERRtoSend");
	Message msgToSend = null;
	try {
	    RERRmsg rerrmsg = (RERRmsg)(runtime.getMessagePool()).getMessage(msgTypeRERR);	
	    (rerrmsg.getUnreachable_Destinations_IDSandSEQNBSpairs()).clear();
	    rerrmsg.setDstNode(0);
	    rerrmsg.setSrcNode(runtime.getNodeID());
	    rerrmsg.setPreviousHop(runtime.getNodeID());
	    rerrmsg.setNextHop(0);
	    rerrmsg.setTTL(1);
	    ArrayList IDandSEQNBpairs = new ArrayList(rerrmsg.getUnreachable_Destinations_IDSandSEQNBSpairs());
	    if (rte!=null) {
		IDandSEQNBpair idandseqnbpair = new IDandSEQNBpair(msg.getDstNode(), rte.getDEST_SEQNB());
		IDandSEQNBpairs.add(idandseqnbpair);
		IDandSEQNBpairs = updateIDandSEQNBpairsBeforeSendingRERR(IDandSEQNBpairs, rte.getnextHop());
	    }
	    else {
		IDandSEQNBpair idandseqnbpair = new IDandSEQNBpair(msg.getDstNode(), 0);
		IDandSEQNBpairs.add(idandseqnbpair);
	    }
	    
	    
	    rerrmsg.setUnreachable_Destinations_IDSandSEQNBSpairs(IDandSEQNBpairs);
	    msgToSend=rerrmsg;
	    (runtime.getMessagePool()).freeMessage(msg);
	} catch(Exception ex) {
	    System.out.println(">(getNewRERRtoSend in AODV) # Could not create Message: " + ex.getMessage());
	    ex.printStackTrace();
	}
	
	return msgToSend;	
    }
    
    /** This method updates an ArrayList contained in a Route Error which was received before broadcasting it further. It adds eventual nodes which are now unreachable (whose corresponding entry in the Routing Table uses the same next hop as the entry for an ureachable destination due to the Route Error which was received)
     * @param IDandSEQNBpairs The ArrayList contained in a Route Error which was received
     * @param nodeWhichHasAproblem The next hop of an entry which corresponds to an unreachable destination
     * @return The maybe updated ArrayList which contains the unreachable destinations
     * */
    private ArrayList updateIDandSEQNBpairsBeforeSendingRERR(ArrayList IDandSEQNBpairs, long nodeWhichHasAproblem){
	//System.out.println("updateIDandSEQNBpairsBeforeSendingRERR");
	
	for(int i=0; i<RoutingTable.size(); i++){
	    RoutingTableEntry rte = (RoutingTableEntry)RoutingTable.get(i);
	    if (rte.getnextHop()==nodeWhichHasAproblem){
		rte.setVALID_ENTRY(false);
		//MAYBE WE MUST INCREMENT SEQNB????!!!!!!!??????
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + DELETE_PERIOD);
		IDandSEQNBpairs.add(new IDandSEQNBpair(rte.getID_DEST(), rte.getDEST_SEQNB()));
	    }
	}//for
	return IDandSEQNBpairs;
    }
    
    
    /** This method overrides the sendMessage method of the class AsynchronousLayer. It checks if there's an entry for the message's destination in the Routing Table. If it hasn't, the message is buffered until it has found one. For finding it, it uses Route Requests
     * @param msg The Message we have to send
     * @return The int which shows the state of the method after finishing
     * */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException { //PTETE METTRE UN TRAITEMENT DES ERREURS...
	//System.out.println("sendMessage");
	
	if(msg!=null){
	    if(msg.getDstNode()==0){
		msg.setNextHop(0);
		if(msg.getType()!=5){
		    
		    //System.out.println("On broadcast un message : "+msg);
		}
		return super.sendMessage(msg);
	    }
	    else {	
		setUpToDateRoutingTable();
		updateRREQsentIDsPairs();
		//printStateOfRoutingTable();
		RoutingTableEntry rte = getRoutingTableEntryFromID(msg.getDstNode());
		if(rte!=null) {
		    if(rte.getVALID_ENTRY()==true) {
			rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
			msg.setNextHop(rte.getnextHop());
			//System.out.println("On unicast un message : "+msg+" au nexthop :" + rte.getnextHop()+" pour la dest : "+msg.getDstNode());
			return super.sendMessage(msg);
			
		    } 
		}
		
		if (!rreqSentBufferContainsRREQ(msg.getDstNode())){
		    MsgsWaitingForRoute.add(msg);
		    addRREQtoRREQsentBuffer(msg.getDstNode()); 
		    /*System.out.println("*************************************");
		      printStateOfRREQsentIDsPairs();
		      printStateOfRREQsSentBuffer();
		      printStateOfRoutingTable();*/
		    
		} else {
		    //System.out.println("message a envoyer a ete bufferise");
		    MsgsWaitingForRoute.add(msg);
		}
	    }
	    
	} 
	return 0;
    }
    
    /** This method is called to forward a message. It checks if there's a route in the Routing Table for the destination of the message. If there's isn't, it calls another method to generate a Route Error Message which will be sent to aware other nodes that this node has no route for this destination
     * @param msg The Message we have to send
     * */
    private void forwardMessage(Message msg) throws SendMessageFailedException{ // Here we know that msg.getDstNode()!=cl.getNodeID()
	//System.out.println("forwardMessage");
	boolean sendFurther=true;
	setUpToDateRoutingTable();
	//msg.setTTL(msg.getTTL()-1);
	RoutingTableEntry rte = getRoutingTableEntryFromID(msg.getDstNode());
	if (rte!=null) {
	    if (rte.getVALID_ENTRY()==true){
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + ACTIVE_ROUTE_TIMEOUT);
		
		msg.setNextHop(rte.getnextHop());
		msg.setPreviousHop(runtime.getNodeID());
		if (msg.getTTL()>0){
		    //System.out.println("On forward le message plus loin, TTL = "+ msg.getTTL()+" : "+msg);
		    super.sendMessage(msg);
		    sendFurther=false;
		}
		else {
		    (runtime.getMessagePool()).freeMessage(msg);
		    sendFurther=false;
		    //System.out.println("forwardMessage : TTL null -> free Message : "+msg);
		}
	    }
	    else{
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis() + DELETE_PERIOD);
	    } 
	}
	if (sendFurther){ 
	    //System.out.println("On génère un RERR : "+msg);
	    super.sendMessage(getNewRERRtoSend(msg, rte));
	}
    }
    
    /** This method checks if the Buffer which contains the Route Request Messages which are being sent contains one for the destination given in parameter
     * @param dst The destination for which we want to know if we are sending some Route Request Messages
     * */
    private boolean rreqSentBufferContainsRREQ(long dst){
	//System.out.println("rreqSentBufferContainsRREQ");
	for (int i=0;i<RREQsSentBuffer.size();i++){
	    if ((((RREQsentBufferEntry)(RREQsSentBuffer.get(i))).getRREQ()).getDstNode()==dst) {
		//System.out.println("rreqSentBufferContainsRREQ returns true");
		return true;
	    }
	}
	//System.out.println("rreqSentBufferContainsRREQ returns false");
	return false;
    }
    
    
    public void superSendMessage(Message msg) throws SendMessageFailedException{
	//System.out.println("superSendMessage : On envoie un RREQ");
	super.sendMessage(msg);
    }
    
    
    /** This method removes the Route Request Message contained in the Buffer which have the destination given in parameter
     * @param dst The destination for which we do not need any Route Request anymore
     * */
    private void emptyRREQsSentBufferOfRREQ(long dst){
	for(int i=RREQsSentBuffer.size()-1;i>=0;i--){
	    if(((RREQmsg)((RREQsentBufferEntry)RREQsSentBuffer.get(i)).getRREQ()).getDstNode()==dst){
		RREQsSentBuffer.remove(i);
	    }
	}
	//System.out.println("emptyRREQsSentBufferOfRREQ. Size of RREQsSentBufferOfRREQ : "+RREQsSentBuffer.size());
    }
    
    
    /** This method sends the Messages which were buffered due to the lack of a route and which can know be sent
     * @param dst The destination for which we have found a route
     * */		
    private void sendMessageWhoseDstHasApath(long dst) throws SendMessageFailedException{
	//System.out.println("sendMessageWhoseDstHasApath. Taille de MsgsWaitingForRoute "+MsgsWaitingForRoute.size()+" dst : "+dst);
	for(int i=MsgsWaitingForRoute.size()-1; i>=0;i--){
	    //System.out.println("dst du msg :"+((Message)MsgsWaitingForRoute.get(i)).getDstNode()+" nexthop : "+((Message)MsgsWaitingForRoute.get(i)).getNextHop()+" src : "+((Message)MsgsWaitingForRoute.get(i)).getSrcNode()+" ttl : "+((Message)MsgsWaitingForRoute.get(i)).getTTL());
	    if (((Message)MsgsWaitingForRoute.get(i)).getDstNode()==dst){
		RoutingTableEntry rte = getRoutingTableEntryFromID(dst);
		rte.setLIFETIME_ACTIVE_ROUTE(System.currentTimeMillis()+ACTIVE_ROUTE_TIMEOUT);
		((Message)MsgsWaitingForRoute.get(i)).setNextHop(rte.getnextHop());
		super.sendMessage((Message)MsgsWaitingForRoute.get(i));
		MsgsWaitingForRoute.remove(i);
	    }
	}
	
	
    }
    
    
    public Vector getRREQsSentBuffer() {
	return RREQsSentBuffer;
    }
    
    public void setRREQsSentBuffer(Vector RREQsSentBuffer) {
	this.RREQsSentBuffer=RREQsSentBuffer;
    }
    
    public Vector getRREQsentIDsPairs(){
	return RREQsentIDsPairs;
    }
    
    public void setRREQsentIDsPairs(Vector RREQsentIDsPairs){
	this.RREQsentIDsPairs=RREQsentIDsPairs;
    }
    
    public SeqNumberAndRREQIDsController getSeqNumberAndRREQIDsControllerObjet(){
	return SeqNumberAndRREQIDsControllerObjet;
    }

    public void setSeqNumberAndRREQIDsControllerObjet(SeqNumberAndRREQIDsController seqNumberAndRREQIDsControllerObjet){
	this.SeqNumberAndRREQIDsControllerObjet=seqNumberAndRREQIDsControllerObjet;
    }
    
    public long getPATH_DISCOVERY_TIME(){
	return PATH_DISCOVERY_TIME;
    }
    
    public long getRREQ_TRIES(){
	return RREQ_TRIES;
    }
    void printStateOfRREQsentIDsPairs(){
	System.out.println("\n -------------------------------------------------- \n");
	System.out.println("Etat de la table de RREQsentIDsPairs :\n");
	for (int i=RREQsentIDsPairs.size()-1; i>=0; i--){
	    System.out.println("SrcNode : "+((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getSrcNode());
	    System.out.println("RREQ ID :"+((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getRREQ_ID());
	    System.out.println("Lifetime :"+((RREQsentIDsPair)RREQsentIDsPairs.get(i)).getLifeTime());
	}
	System.out.println("\n -------------------------------------------------- \n");
    }
    
    public void printStateOfRREQsSentBuffer(){
	System.out.println("\n -------------------------------------------------- \n");
	System.out.println("Etat de la table de RREQsSentBuffer :\n");
	for (int i=RREQsSentBuffer.size()-1; i>=0; i--){
	    System.out.println("ID : "+(((RREQsentBufferEntry)RREQsSentBuffer.get(i)).getRREQ()).getRREQ_ID());
	    System.out.println("SrcSeqNb : "+(((RREQsentBufferEntry)RREQsSentBuffer.get(i)).getRREQ()).getSrcOfRREQseqnb());
	    System.out.println("Dst : "+(((RREQsentBufferEntry)RREQsSentBuffer.get(i)).getRREQ()).getDstNode());
	}
	System.out.println("\n -------------------------------------------------- \n");
    } 
    
    
}//aodv class

