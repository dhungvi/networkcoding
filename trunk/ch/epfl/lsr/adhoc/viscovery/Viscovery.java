package ch.epfl.lsr.adhoc.viscovery;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborService;
import java.util.*;


/**
 * This is a graphical application to discover (<b>vis</b>ual dis<b>covery</b>) the
 * topology of a MANET.
 * <p>
 * the viscovery application is based on an routing algorithm similar to
 * &quot;TokenRing&quot;: A node that is running the viscovery layer can create a
 * TokenMessage. This TokenMessage is then sent from node to node, according to the
 * configured algorithm (<b>LRV for Least Recently Visited</b> or <b>LFV for Least
 * Frequently Visited</b>). This algorithm is configured at creation time of a token
 * and thus needs a Viscovery layer running in the graphics-mode (param 'frameVisible'
 * in the xml config file).<br>Every time a node running Viscovery receives this
 * TokenMessage, it can update its knowledge of the current network topology by 
 * reading and evaluating the Tokens content.
 * If necessars, the node adds/updates its own entry in the Tokens content before
 * resending it again to one of its neighbors, according to the configured algorithm.
 * <p>
 * To display the so gathered network topology graphically, the Viscovery layer uses
 * visFrame.
 * 
 * @see TokenMessage
 * @see CommunicationsLayer
 * @see VisFrame
 * @see TokenCreator
 * @author Stefan Thurnherr
 * @version 1.0
 */

public class Viscovery extends AsynchronousLayer {
    /** The version number of this Viscovery class */
    protected static String version = "v1.0";
	private Parameters params;	
    /** A reference to FRANC runtime*/
    private FrancRuntime runtime;
    /** The variable representing the whole graphical interface */
    private VisFrame visFrame;
    /** A reference to the asynchronous interface used for the communications. */
    private Dispatcher disp = null;
    /** A reference to the message pool (for creating new messages) */
    private MessagePool mp = null;
    /** Type of service to be used for messages */
    private char msgType;
    /** The time to live for messages sent by chat (parameter in config file) */
    private int ttl;
    /** Boolean parameter to decide whether or not a frame should be used to visualize the
     * discovered network topology
     */
    private boolean frameVisible = false;
    /** long to set the delay waited for before the node sends the token again */
    private long baseDelay = 1000;
    /** variable to enable holding the next token via the graphical interface */ 
    private boolean holdNextToken = false;
    /** variable to store whether next time a token gets released it should be
     * destroyed or kept going
     */
    private boolean keepTokenAlive = true;
    /** A reference to the neighbor service (to obtain the list of neighbors)*/
    private NeighborService nm;
    
    /**
     * ID (name) that is sent with every message.
     * The node name is used to replace the default value.
     */
    private String id;
    
    /**
     * The constructor creates a new Frame for the viscovery application.
     * <p>
     * A new Frame is created, set to an appropriate size and centered on the window.
     * <p>
     * All the network stuff is done either in the method initialize() or in the
     * method startup() because it is only at that time that the layer hierarchy will
     * be up and working.
     */
    
    public Viscovery (String name, Parameters params) {
		super(name,params);
	this.params = params;
    }
    
    /**
     * Initializes the viscovery layer.
     * <p>
     * This layer initializes network resources, such as the message pool and the
     * dispatcher. It further obtains the type of messages used by this layer
     * (configuration parameter 'msgType') and the ttl (configuration parameter 'ttl').
     *
     */
    public void initialize(FrancRuntime runtime) {
	this.runtime = runtime;
	//writeLog("My node ID is " + cl.getNodeID());
	
	// obtain reference to dispatcher
	try {
	    disp = runtime.getDispatcher();
	} catch(Exception ex) {
	    //System.out.println("> # Could not obtain Dispatcher: " + ex.getMessage());
	    throw new RuntimeException(ex.getMessage());
	}
	if(disp == null) {
	    throw new RuntimeException("Dispatcher is null");
	}
	
	// Obtain reference to MessagePool
	try {
	    mp = runtime.getMessagePool();
	} catch(Exception ex) {
	    //System.out.println("> # Could not obtain MessagePool: " + ex.getMessage());
	    throw new RuntimeException(ex.getMessage());
	}
	if(mp == null) {
	    throw new RuntimeException("MessagePool is null");
	}
		    
	String msgName = null;
	try {
	    msgName =  params.getString("msgType");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException(pdnee);
	}
	try {
	    msgType = mp.getMessageType(msgName);
	} catch(Exception ex) {
	    throw new RuntimeException("Message type '" + msgName + "' not found");
	}
		    
	try {
	    ttl = params.getInt("ttl");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException(pdnee);
	}
	try {
	    frameVisible = params.getBoolean("frameVisible");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException(pdnee);
	}
	try {
	    baseDelay = params.getLong("baseDelay");
	    //set an upper and lower bound for the baseDelay in msecs
	    if (baseDelay < 1000) {
		baseDelay = 1000;
	    }
	    else if ( baseDelay > 9000) {
		baseDelay = 9000;
	    }
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException(pdnee);
	}
	id = runtime.getNodeName();
		    
    }

    /**
     * Get a reference to the messagepool of this viscovery layer
     * @return the reference to the layers messagepool
     */
    protected MessagePool getMp() {
	return this.mp;
    }

    /**
     * Get the messagetype of this viscovery layer (param in config file)
     * @return the msgType of this layer
     */
    protected char getMsgType() {
	return this.msgType;
    }

    /**
     * Get the nodeID of this node
     * @return the nodeID of this node
     */
    protected long getNodeID() {
	return runtime.getNodeID();
    }

    /**
     * Get the TTL for messages created through this viscovery layer
     *<p>
     * This method gets the TTL (param in the config file) that will be used for messages created
     * by this layer
     * @return the configured TTL
     */
    protected int getTTL() {
	return this.ttl;
    }

    /**
     * Set the delay that the application should wait before sending away a token
     * <p>
     * This method sets the delay in seconds that the viscovery application should wait before sending
     * away a received and evaluated token
     * @param secs the new delay in seconds
     */
    protected void setDelay(long secs) {
	this.baseDelay = secs * 1000;
	//writeLog("delay set to " + secs * 1000);
    }

    /**
     * Get the baseDelay that the Viscovery layer currently applies before sending a TokenMessage
     * towards the network, i.e. down the layerstack
     * @return the delay that the viscovery layer is currently appliying before sending a tokenMessage
     */
    protected long getDelay() {
	return this.baseDelay;
    }

    /**
     * Set whether the token should be hold back next time it is received
     * <p>
     * This method sets the boolean value that decides whether a received token should be hold back the
     * next time such a token has been received
     * away a received and evaluated token
     * @param hold boolean value to decide whether to hold back or not
     */
    protected void setHoldNextToken(boolean hold) {
	//writeLog("holding next token..");
	this.holdNextToken = hold;
    }

    /**
     * Get the boolean value whether the next visiting token should be hold back or not
     * @return A boolean indicating whether the next visiting token will be hold back or not
     */
    protected boolean getHoldNextToken() {
	return this.holdNextToken;
    }

    /**
     * Start the functionality of the viscovery layer.
     * <p>
     * This method essentially starts the thread that handles instances of TokenMessage. Furtherly if
     * the graphical representation of the network topology should be displayed (config param), a
     * corresponding Frame is initialized and set up via the VisFrame constructor
     * @see VisFrame
     */
    public void startup() {
	this.setDaemon(false);
	start();

	//get the neighborservice for this manet-framework
	nm = (NeighborService)runtime.getService("Neighboring");

	//display the graphical representation (depending on config param)
	if (frameVisible) {
	    visFrame = new VisFrame(this, runtime.getNodeID(), id);
	    visFrame.setVisible(true);
	}
	else {
	    writeLog("Welcome to the Viscovery application " + Viscovery.version);
	}
    }

    /**
     * The method that handles all messages arriving in the Viscovery buffer
     * <p>
     * handleMessage(Message msg) tests whether msg is destined to the
     * viscovery application. If this is the case, it calls treatToken(msg). Else
     * the msg is passed to the super class and thus put into the buffer, where
     * it will be read by the layer above.
     *
     * @param msg the message newly arrived
     */
    protected void handleMessage(Message msg) {
	char type = msg.getType();
	if (type == msgType) {
	    /**
	     * Because a routing algo has not been implemented yet on a low layer basis, we do here
	     * an explicit application-level filtering by dropping all TokenMessages that are not
	     * destined to this node.
	     */
	    if (msg.getDstNode() == runtime.getNodeID()){
		writeLog("A TokenMessage has been detected..");
		((TokenMessage)msg).setCurrentNodeIndex(runtime.getNodeID());
		treatToken((TokenMessage)msg); /* read and treat (and probably send) this token */
	    }
	    else if (msg.getDstNode() == (new Long("0")).longValue()) {
		writeLog("A Broadcast message has been detected, which will not be treated (nonsense)");
	    }
	    else {
		writeLog("A TokenMessage has been received, but was not destined to me..evaluating content and then discarding msg");
		if (frameVisible) {
		    visFrame.updateGraph((TokenMessage)msg);
		}
		writeLog("------treatment finished--------");
	    }
	}
	else {
	    super.handleMessage(msg); //put msg in buffer
	    mp.freeMessage(msg);
	}
    }
	 

    /**
     * This method creates a new message and sends it to the lower layer.
     * <p>
     * @param dst        The destination node id (i.e. the node id of the node that should
     *                   receive this message)
     * @param tokenTrace The token content that should be sent
     */
    private void send(long dst, String algo, List tokenTrace, long nbVisits) {
	//writeLog("start of send method");
	// obtain a message object
	Message msg = null;
	try {
	    TokenMessage tm = (TokenMessage)mp.getMessage(msgType);
	    tm.setTrace(tokenTrace);
	    tm.setDstNode(dst);
	    tm.setTTL(ttl);
	    tm.setAlgo(algo);
	    tm.setNbVisits(nbVisits);
	    msg = tm;
	} catch(Exception ex) {
	    writeLog("# Could not create TokenMessage before sending: " + ex.getMessage());
	}

	// send message through the AsynchronousLayer's default method
	try {
	    sendMessage(msg);
	} catch(Exception ex) {
	    writeLog("# Could not send TokenMessage: " + ex.getMessage());
	    /**
	       }	finally {
	       mp.freeMessage(msg);
	    */
	}
	//writeLog(" token successfully sent to " + dst);
    }
		
    /**
     * This method is the core method of the viscovery layer. It inspects all TokenMessages
     * received by and destined to this node,
     * <p>
     * This method inspects a token, updates the contained information and then
     * decides - potentially depending on the GUI's user input - what to do with such
     * a tokenMessage. Before this method can be called with a TokenMessage as parameter,
     * such a TokenMessage must fulfill the following requirements:<br>
     * - the dstNode field must be initialized (to any valid long value)
     * - the TTL &amp; algo fields of the token must be set correctly
     * - the nbVisits field must either be set to the old value (i.e. the count that was
     * valid for the last visit), or be initialized to 0 (in case of a newly created token)
     * - the currentNodeIndex field must be set correctly
     * <br>These requirements can all be achieved via the corresponding setter methods of 
     * the tokenMessage. If a tokenMessage is received from the MANET Framwork stack, these
     * requirements are all fulfilled automatically by the viscovery layer. 
     *
     * @param msg A TokenMessage containing potentially relevant information for this node
     * and its viscovery application
     */
    protected synchronized void treatToken(TokenMessage msg) {
	writeLog(" delay:   " + this.baseDelay); 
	if (msg.getDstNode() > 0) {
	    //message was received from network and not created from scratch by viscovery			
	    writeLog(" msgType: " + runtime.getMessagePool().getMessageType(msg.getType()) + " (" + (int)msg.getType() + ")");
	    writeLog(" length:  " + msg.getLength());
	    writeLog(" source:  " + msg.getSrcNode());
	    writeLog(" dest:    " + msg.getDstNode());
	    writeLog(" TTL:     " + msg.getTTL());
	    writeLog(" seq #:   " + msg.getSequenceNumber());
	}
	writeLog(" algo:    " + msg.getAlgo());
	writeLog(" nbVisits " + msg.getNbVisits() + "(this one excluded)");
	writeLog(" currentNodeIndex:   " + msg.getCurrentNodeIndex());
	writeLog(" trace:   " + msg);

	// hold the token if set so by checkbox of GUI
	if (holdNextToken) {
	    visFrame.showTab(1);
	    writeLog("holding token..press 'release' to continue");
	    try {
		this.wait();
	    }
	    catch (InterruptedException e) {
		writeLog("viscovery.java:wait failed - 'hold token' not possible");
	    }
	}

	if(keepTokenAlive) {
	    msg.addVisit(runtime.getNodeID());
	    msg.updateNeighbors(nm.getTable().getNeighborsIds());
	    msg.updateOthersNeighbors();
	    //System.out.println("treatToken:updatNeighbors successful");
	    if(frameVisible) {
		//System.out.println("now calling updateGraph(msg)");
		visFrame.updateGraph(msg);
	    }
						
	    long nextDest = this.getNextDest(msg);
	    //writeLog(" nextDest is " + nextDest);
	    if (nextDest == runtime.getNodeID()) {
		writeLog(" No neighbors available..token evaluated and destroyed");
		//mp.freeMessage(msg);
	    }
	    else {
		writeLog(" New target neighbor for this token is " + nextDest);
		try {
		    FrancThread.sleep(baseDelay);
		}
		catch (InterruptedException e) {
		    writeLog("Unable to apply the baseDelay..sending without delay");
		}
		this.send(nextDest, msg.getAlgo(), msg.getTrace(), msg.getNbVisits());
	    }
	}
	else {
	    writeLog(" token destroyed (as you wished to..)");
	    /**
	     * set keepTokenAlive back to true - if we don't do this, every other token
	     *potentially arriving later on will be destroyed.
	     */
	    this.keepTokenAlive = true;
	}
	mp.freeMessage(msg);
	writeLog("------treatment finished--------");
    }

    /**
     * Method used to obtain the monitor for this object
     * <p>
     * This method is used to obtain the monitor for this object and to notify it, i.e.
     * waking it up from a wait()
     */
    protected synchronized void releaseToken(boolean keepTokenAlive) {
	this.keepTokenAlive = keepTokenAlive;
	//writeLog("releasing token with notify()");
	this.notifyAll();
	//writeLog("releaseToken:notify überstanden");
	//System.out.println("releaseToken:notify überstanden");
    }


    /**
     * This method finds a possible destination node to send the token to. This is
     * done according to the set algorithm of the tokenMessage that is being treated. If
     * no neighbor node exists, the method returns the ID of this node.
     * @param msg the msg for which the next destination must be determinated (according to its algo)
     * @return the unique long ID of the next destination node (the ID of this node if no neighbor exists)
     */
    private long getNextDest(TokenMessage msg) {
	String algo = msg.getAlgo();
	List oldTrace = msg.getTrace();
	long nextDest = runtime.getNodeID();
	long[] nt = nm.getTable().getNeighborsIds();
	if (algo.equals("LFV") || algo.equals("LRV")) { //applying LRV or LFV to find next neighbor
	    if(nt.length > 1) { //at least 1 neighbor apart meself does exist
		//writeLog("------------\n at least one other neighbor does exist..");
		long aNeighbor = nextDest;

		/**
		 * init index of trace-array of the least recently/frequently visited neighbor
		 * to a no-good value and its time to max-value
		 */
		int lfNeighbor = -1;
		long lfNeighborTime = msg.getNbVisits();

		boolean stopSearch = false;
		for ( int i = 0; i < nt.length; i++) {
		    /**
		     *writeLog("-----nt-----");
		     *for(int k = 0; k < nt.length; k++) {
		     *	writeLog(" " + nt[k]);
		     *}
		     *writeLog("-----end nt-----");
		     */
		    aNeighbor = nt[i];
		    //writeLog("  working on neighbor " + i + " ( " + aNeighbor + " ) of neighbortable");
		    if(aNeighbor == runtime.getNodeID()) {
			//writeLog("that's me..I'm no good target");
		    }
		    else {
			//for (int j = 0; j < oldTrace.length; j++) {
			List traceRow;
			synchronized(oldTrace) {
			    long anId = -1;
			    long aCount = 0;
			    for (Iterator it = oldTrace.iterator(); it.hasNext();) {
				traceRow = (List)(it.next());
				//writeLog("working on traceRow " + traceRow);
				anId = ((Long)(traceRow.get(0))).longValue();
				aCount = ((Long)(traceRow.get(1))).longValue();
				//writeLog("   comparing to traceLine " + oldTrace.indexOf(traceRow) + " ( " + anId + ") of tokenTrace");
				if (anId == aNeighbor) { //NeighborTable[i] found
				    //writeLog("   this neighbor found in trace;recentness? nbVisits=" + msg.getNbVisits());
				    if (aCount <= lfNeighborTime) {
					//oldTrace[j] has not been visited for a longer time than oldTrace[lfNeighbor]
					//writeLog("   this neighbor not visited for a longer time: "+ lfNeighborTime + " vs. " + aCount);
					lfNeighbor = oldTrace.indexOf(traceRow);
					lfNeighborTime = aCount;
				    }
				    //writeLog("   breaking inner loop");
				    break;
				}
				else if (!(it.hasNext())) {
				    /**
				     * the neighbor i is not yet present in the TokenTrace and has thus
				     * never had the token - don't continue to search next neighbor, but
				     * take this neighbor i as the next destination
				     */
				    nextDest = aNeighbor;
				    lfNeighbor = oldTrace.size();; //not necessary, just to be consistent
				    stopSearch = true;
				    //writeLog("  stopSearch becomes true; breaking inner loop");
				    break;
				}
			    }
			}
		    }
		    if (stopSearch) {
			//writeLog(" stopSearch is true..breaking both loops..");
			break;
		    }
		}
		if (!stopSearch && lfNeighborTime <= msg.getNbVisits() && lfNeighbor > -1) {
		    /**
		     * every neighbor in neighbortable has had the token at least once AND
		     * an occurance of at least one neighbor has been found in the tokenTrace
		     * that has not been visited for a long time AND lfNeighbor has been
		     * modified at least once (it's possible that lfNeighbor stays at init value -1)
		     */
		    nextDest = ((Long)(((List)(oldTrace.get(lfNeighbor))).get(0))).longValue();
		}
	    }
	}
	else {
	    writeLog("# algorithm not recognized -> getNextDest() failed");
	}
	return nextDest;
    }

    /**
     * This method prints out the messages that the Viscovery layer produces.
     * If the graphical display of the network topology is turned on (parameter in the
     * configuration file), the method writes the messages to the corresponding
     * TextField in that frame. Otherwise the messages are written to standard output.
     */
    protected void writeLog(String text) {
	if(frameVisible) {
	    //System.out.println("viscovery:before call visFrame.writeLog()");
	    visFrame.writeLog(text);
	    //System.out.println("viscovery:after call visFrame.writeLog()"); 
	}
	else {
	    System.out.println(text);
	}
    }
}
