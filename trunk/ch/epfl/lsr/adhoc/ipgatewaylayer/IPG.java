package ch.epfl.lsr.adhoc.ipgatewaylayer;

//
//  IPG.java
//  
//
//  Created by Dominique Tschopp on Thu Oct 30 2003.
//  
//

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Buffer;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.UnknownMessageTypeException;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborService;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborTable;

import java.util.*;
import java.net.*;
import java.io.*;

/**
 * This class defines the core of the IP Gateway  Layer, which enables communication with distant adhoc 
 * networks through the WAN (Wide Area Network).
 * <p>
 * This means that based on the next hop field of a message, this layer decides whether
 * it should send it to the lower layer (which will most likely be the data link layer), or
 * write it on a TCP connection to another gateway.
 * <p>
 * 
 *
 * @see WANServer
 * @see GServer
 * @see Gateway
 * @see GatewayServer
 * @see Connect
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */


public class IPG extends AsynchronousLayer {
	
	private Parameters params;

    /**The message pool*/
    private MessagePool mp=null;
    /**The dispatcher*/
    private Dispatcher disp = null;
    /**The neighbor Service*/
    private NeighborService nm;
    /**The communication layer associated with this layer*/
    private FrancRuntime runtime;
    /** maximum size of the socket buffer*/
    private int maxSize;
    /** The unique node id for this node (obtained from CommunicationsLayer)*/
    private long nodeID;
    /**A vector containing the node IDs of neighbor gateways*/
    private Vector gways=new Vector();
	
    private Buffer buffer;
    /**The IP address of the next gateway*/
    private InetAddress GatewayServer;
    /**The IP address of the local WAN interface*/
    private InetAddress localaddr=null;
    /** The sequence number for the next message to be sent */
    private int seqNumber = 0;
    /**
     * Constructor.
     * <p>
     */
    public IPG(String name, Parameters params){
	super(name, params);
	this.buffer=new Buffer();
	this.params = params;
    }
    /**
     * This method is called to initialize the layer.
     *
     */
	
    public void initialize (FrancRuntime runtime){
	this.runtime = runtime;
	this.mp = runtime.getMessagePool();
		
	// obtain reference to dispatcher
	try {
	    disp = runtime.getDispatcher();
	} catch(Exception ex) {
	    System.out.println("> # Could not obtain Dispatcher: " + ex.getMessage());
	    throw new RuntimeException(ex.getMessage());
	}
	if(disp == null) {
	    throw new RuntimeException("Dispatcher is null");
	}
	try {
	    maxSize = params.getInt("maxBufferSize");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading max buffer size configuration parameter: " + pdnee.getMessage());
	}
		
    }
    /**
     * This method is called to start the layer.
     * <p>
     * This method will start the main thread for the layer, initialize the Neighbor Service,
     * the local interface, the node ID and establish a communication to the Gateway Server.
     */
	
    public void startup(){
	Socket clientSocket=null;
	BufferedOutputStream bos;
	BufferedInputStream bis;
	BufferedOutputStream toGate=null;
	BufferedInputStream fromGate=null;
	int length = 0;
	byte[] b,bb,bbb;
	String srx=new String("");
		
	start();
	nodeID = runtime.getNodeID();
	nm = (NeighborService)runtime.getService("Neighboring");
		
	String gs=null;
	String WANInt=null;
	try {
		gs=params.getString("gatewayServer");
		WANInt=params.getString("WANInterface");
	} catch(ParamDoesNotExistException pdnee){
		throw new RuntimeException("Error reading \"gatewayServer\" or \"WANInterface\" parameter: " + pdnee.getMessage());
	}
	this.setInterface(WANInt);
	try{
	    GatewayServer=InetAddress.getByName(gs);
	}
	catch(UnknownHostException exc)
	    {
		System.out.println("*************");
		System.out.println("Error:");
		System.out.println("The address you entered for the gateway server appears not to be in a valid format (IP)");
		System.out.println("*************");
		System.exit(0);
	    }
	//connect to Gateway Server
		
	Random rand=new Random();
	int port=30000+rand.nextInt(9999);
	boolean conok=true,showmessage=true;
	//Try to connect to the gateway server as long as necessary
	while(conok){
	    try{
		clientSocket= new Socket(GatewayServer,10000,localaddr,port);
		bos=new BufferedOutputStream(clientSocket.getOutputStream());
		bis=new BufferedInputStream(clientSocket.getInputStream());
				
		//phase 1: give your identification
		Long id=new Long(nodeID);
		String subscribe=0+":"+localaddr.getHostAddress()+":"+id+":";
		length=subscribe.length();
		if(length<32){
		    String nada="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		    subscribe=subscribe.concat(nada.substring(length,32));
		}
		b=subscribe.getBytes();
		bos.write(b,0,b.length);
		bos.flush();
				
		//phase 2: receive number of gateways
		bb=new byte[3];
		int i=bis.read(bb);
		String gcount=new String(bb);
				
		int gc=Integer.parseInt(gcount.trim());
				
		//phase 3: sending "ready"
		String sb="ready";
		bbb=sb.getBytes();
		bos.write(bbb,0,bbb.length);
		bos.flush();
				
		byte[] bufRX=new byte[gc*30];                                       
		i=bis.read(bufRX);
				
		String rx=new String(bufRX);
		srx=rx;
		conok=false;
	    }
	    catch(IOException ioe){
		if(showmessage){
		    System.out.println("*************");
		    System.out.println("Error:");
		    System.out.println("The connection to the gateway server failed. Trying again... ");
		    System.out.println("The startup will block until the server can be reached");
		    System.out.println("*************");
		    showmessage=false;
		}
	    }
	}
	if(!showmessage){
	    System.out.println("*************");
	    System.out.println("The connection to the gateway server is established");
	    System.out.println("This node now operates as a gateway");
	    System.out.println("*************");
	}
	//Storing Gateways
	String[] nodes=srx.split("/");
	int p=11000;
		
	for (int q=0;q<nodes.length-1;q++){
	    InetAddress add=null;
	    try{
		String[] gwa=nodes[q].split(":");
		add=InetAddress.getByName(gwa[0]);
		p=p+1;
		clientSocket= new Socket(add,9999,localaddr,p);
		toGate=new BufferedOutputStream(clientSocket.getOutputStream());
		fromGate=new BufferedInputStream(clientSocket.getInputStream());
				
				
		Gateway gads=new Gateway(gwa[0],gwa[1],toGate,fromGate);
		gways.add(gads);
				
		ReadStream rs=new ReadStream(clientSocket,fromGate,maxSize,this);
		rs.start();
	    }
	    catch(Exception ioe){
		System.out.println("unable to connect to "+add.getHostAddress());
		reportFailure(add.getHostAddress());
		try {
		    if (toGate != null)
			toGate.close();
		    if (fromGate != null)
			fromGate.close();
		    if (clientSocket != null)
			clientSocket.close();
		}
		catch (IOException ioee) {
		    System.err.println(ioee);
		}
	    }
	}
	WANServer WS=new WANServer(this,localaddr,maxSize); 
	WS.start();
    }
    /**
     * Get the vector containing the list of known gateways.
     * <p>
     */
    public Vector getgways(){
	return this.gways;
    }
    /**
     * Add a new gateway to the list of gateways.
     * <p>
     * @param ia The IP address for the new node
     * @param id The nodeID for the new node
     * @param netout The OutputStream to the new node
     * @param netin The InputStream from the new node
     */
    public int addGateway(String ia, String id,BufferedOutputStream netout,BufferedInputStream netin){
	Gateway g=new Gateway(ia,id,netout,netin);
	this.gways.add(g);
	return gways.size();
    }
    /**
     * This method is used to recover a transmitted message.
     * <p>
     * This method creates a new message from the message pool corresponding
     * to the byte[] received over the WAN.
     * <p>
     * @param bufRX The recived byte array
     * @param length The length of the message
     */
    
    public Long byteToMsg(byte[] bufRX,short length){
	Long src=null;
	try {
	    if (mp==null)
		System.out.println("mp null ");
	    Message message = mp.createMessage(bufRX,length);
	    src=new Long(message.getPreviousHop());
	    this.handleMessage(message);
        } catch(UnknownMessageTypeException umte) {
	    System.out.println(">>> Unknown Message type: " + ((int)umte.getUnknownType()));
        }
        return src;                
		
    }
    /**
     * This method is called to report the failure of a gateway.
     * When invoked, a connection is opened to the gateway server to report the failure
     * <p>
     * @param s The IP address of the crashed gateway node
     */
	
    public void reportFailure(String s){
	Socket clientSocket;
	BufferedOutputStream bos;
	BufferedInputStream bis;
	byte[] b;
		
	Random rand=new Random();
	int port=30000+rand.nextInt(10000);
		
	try{
	    clientSocket= new Socket(GatewayServer,10000,localaddr,port);
	    bos=new BufferedOutputStream(clientSocket.getOutputStream());
	    bis=new BufferedInputStream(clientSocket.getInputStream());
	    String remove=1+":"+s+":";
	    b=remove.getBytes();
	    bos.write(b,0,b.length);
	    bos.flush();
	}
	catch(IOException ioe){
	    System.out.println("*************");
	    System.out.println("Error:");
	    System.out.println("The connection to the gateway server failed while reporting an error");
	    System.out.println("*************");
	}
	boolean fin=true;
	for(int r=0;r<gways.size() && fin;r++){
	    Gateway gw=(Gateway)gways.elementAt(r);
	    if(gw.getIa().compareTo(s)==0){
		gways.removeElement(gw);
		fin=false;
	    }
	}
    }
    /**
     * This method is used to set the WAN interface.
     * <p>
     * The InetAddress of the local WAN interface is assigned 
     * correspondingly to the name of the chosen interface.
     * <p>
     * @param WANInt The name of the WAN interface
     */
	
    void setInterface(String WANInt) {
	Enumeration ia;
	try{
	    NetworkInterface ni=NetworkInterface.getByName(WANInt);
	    ia=ni.getInetAddresses();
	    localaddr=(InetAddress)ia.nextElement();
	}
	catch(java.lang.NullPointerException npe){
	    System.out.println("*************");
	    System.out.println("Error:");
	    System.out.println("You must enter a valid WAN interface in the configuration file");
	    System.out.println("*************");
	    System.err.println(npe);
	}
	catch(SocketException exc)
	    {
		System.err.println(exc);
	    }
    }
    /**
     * This method is used to send a Message.
     * <p>
     * The decision whether to send the message to the WAN or to the lower layer 
     * is based on the next hop field.
     * <p>
     * @param msg The message object to be send
     * @throws IllegalArgumentException
     * @throws SendMessageFailedException
     */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException {
		
	BufferedOutputStream bos=null;
	boolean WAN=false;
	boolean Bct=false;
	boolean Nbr=false;
	long nextHop=msg.getNextHop();
	Long nh=new Long(nextHop);
	NeighborTable nt=nm.getTable();
	long[] neighbors=nt.getNeighborsIds();
		
	//test whether the message is a broadcast
	if (nextHop==0){
	    Bct=true;
	    WAN=true;
	}
	//test whether the next hop is a neighbor, if yes check whether it is a gateway neighbor
	//in the latter case, send the message to this gateway
	for(int i=0;(i<neighbors.length && WAN==false);i++){
	    if (neighbors[i]==nextHop){
		Nbr=true;
		for(int j=0;j<gways.size() && WAN==false;j++){
		    String s=nh.toString();
		    if(((Gateway)gways.elementAt(j)).getId()==s){
			bos=((Gateway)gways.elementAt(j)).getOut();
			WAN=true;
		    }			
		}
	    }
	}
		
	if(WAN || Nbr==false){
	    if (msg == null) {
		throw new IllegalArgumentException("The message cannot be null");
	    } else
		{
		    // only set the source node and sequence number when not already set
		    // this is because the routing layer may resend a message without it
		    // being changed (i.e. the source node should be the one of the original node)
		    if(msg.getNextHop() == -1)
			throw new SendMessageFailedException("Next hop not defined in message : "+msg);
		    if(msg.getDstNode() == -1)
			throw new SendMessageFailedException("Destination not defined message : "+msg);				
		    if(msg.getSrcNode() == -1 || msg.getSrcNode() == nodeID) {
			msg.setSrcNode(nodeID);
			msg.setSequenceNumber(seqNumber);
			seqNumber = ((seqNumber + 1) & 0xFFFF);
		    }
		    msg.setPreviousHop(nodeID);
				
		    //Preparing message
		    byte[] bufTX=new byte[maxSize];
		    int l = msg.getByteArray(bufTX);
				
				
		    //Sending to all gateways
		    if(Bct || Nbr==false){
			for(int v=0;v<gways.size();v++){
			    try{
				((Gateway)gways.elementAt(v)).getOut().write(bufTX,0,l);
				((Gateway)gways.elementAt(v)).getOut().flush();
			    }
			    catch(IOException ioe){
				System.out.println("*************");
				System.out.println("Error:");
				System.out.println("The connection to a gateway failed");
				System.out.println("*************");
			    }
			}
		    }
		    else{
			try{
			    bos.write(bufTX,0,l);
			    bos.flush();
			}
			catch(IOException ioe){
			    System.out.println("*************");
			    System.out.println("Error:");
			    System.out.println("The connection to a gateway failed");
			    System.out.println("*************");
			}
					
		    }
		}
	    if(Bct){
		super.sendMessage(msg);
	    }
	}
	else
	    {
		super.sendMessage(msg);
	    }
	return -1;
		
    }
	
}

/**
 * This class represents a Stream Reader.
 * <p>
 * Reads the input streams from the gateways received from the GServer
 * <p>
 * @see WANServer
 * @see GServer
 * @see Gateway
 * @see GatewayServer
 * @see Connect
 * @see Connection
 * @see IPG
 * @author Dominique Tschopp
 */


class ReadStream extends FrancThread{
	
    private BufferedInputStream bis;
    private byte[] bufRX;
    private IPG ipg;
    private int maxSize;
    private Socket cs=null;
    /**
     * Constructor
     * <p>
     * @param cs The socket for the connection
     * @param bis The corresponding input Stream
     * @param maxSize The maximum size for a message
     * @param ipg The IPG this streamreader is attached to
     */
    
    ReadStream(Socket cs,BufferedInputStream bis,int maxSize,IPG ipg){
	this.bis=bis;
	this.maxSize=maxSize;
	this.ipg=ipg;
	this.cs=cs;
    }
    /**
     * this method is invoked as a separate thread
     */
    public void run(){
	bufRX=new byte[maxSize];
	try {
	    while(true){
		int length=bis.read(bufRX,0,maxSize);
		Long NodeID=ipg.byteToMsg(bufRX,(short)length);
	    }
	}
	catch (Exception ioe) {
	    ipg.reportFailure(cs.getInetAddress().getHostAddress());
	}
	finally {
	    try {
		if (bis != null)
		    bis.close();
		if (cs != null)
		    cs.close();
	    }
	    catch (IOException ioee) {
		System.err.println(ioee);
	    }
	}
		
    }
}
