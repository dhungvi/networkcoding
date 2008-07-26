package ch.epfl.lsr.adhoc.runtime;

import java.net.*;
import java.io.*;

/**
 * This class implements the asyncronous layer for the MANET access
 * through a java.net.MulticastSocket.
 * <p>
 * This is usually the lowest layer in the MANET layer hierarchie.
 * <p>
 * <p> <b>Important note to developers</b>
 * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> in 
 * <b>ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer</b> have been
 * implemented to mimic the same behaviour as the ones in here regarding return
 * values, thrown exceptions and data writtern on System.out. If for some reason
 * this behaviour changes or is not needed anymore, the changes should be
 * propagated to ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer as well.
 * 
 * @author Javier Bonny
 * @version 1.0
 */
public final class AsyncMulticast extends AsynchronousLayer {
    /** A reference to FRANC runtime */
    private FrancRuntime runtime;
    /** Network interface for Multicast (needed if the computer has several physical netwok interfaces)*/
    private String networkInterface;
    /** The socket used for sending/receiving IP multicast packets.*/
    private MulticastSocket socket;
    /** The InetAdress object which contains the multicast group (class D IP adress)*/
    private InetAddress multicastGroup;
    /** contains the multicast port*/
    private int multicastPort;
    /** maximum size of the socket buffer*/
    private int maxSize;
    /** the socket buffer for transmission*/
    private byte[] bufTX;
    /** the socket buffer for reception*/
    private byte[] bufRX;
    /** The datagram packet object for transmission*/
    private DatagramPacket packetTX;
    /** The datagram packet object for reception*/
    private DatagramPacket packetRX;
    /** A reference to the message pool of this node*/
    private MessagePool messagePool;
    /** The unique node id for this node (obtained from CommunicationsLayer)*/
    private long nodeID;
    /** The sequence number for the next message to be sent */
    private int seqNumber = 0;

    /**
     * The default constructor.
     */
    /*public AsyncMulticast(String name) {
		super(name);
		}*/

	public AsyncMulticast(String name, Parameters params) {
		super(name,params);
		try {
			multicastPort = params.getInt("port");
		} catch(ParamDoesNotExistException pdnee) {
			pdnee.printStackTrace();
		}
		try {
			String mGroup = params.getString("multicastgroup");
			multicastGroup = InetAddress.getByName(mGroup);
		} catch(ParamDoesNotExistException pdnee) {
			pdnee.printStackTrace();
		} catch(UnknownHostException uhe) {
			uhe.printStackTrace();
		}	   
		try {
			maxSize = params.getInt("maxBufferSize");
		} catch(ParamDoesNotExistException pdnee) {
			pdnee.printStackTrace();
		}
		networkInterface = params.getString("networkInterface",null);
	}

    /**
     * Initializes the layer.
     * <p>
     * This method reads the following configuration variables:
     * <ul>
     * <li><b>layers.AsyncMulticast.port</b> - The port to use for the multicast communication</li>
     * <li><b>layers.AsyncMulticast.multicastgroup</b> - The multicast address to use</li>
     * <li><b>layers.AsyncMulticast.networkInterface</b> - The address of the physical
     *     network adapter to use (optional, useful if a computer has several network interfaces)</li>
     * <li><b>layers.AsyncMulticast.maxBufferSize</b> - The number of bytes reserved
     *     for sending/receiving messages (the maximum length of network messages)</li>
     * </ul>
     *
     */
    public void initialize(FrancRuntime runtime) {
		this.runtime = runtime;
		this.messagePool = runtime.getMessagePool();

    
		try{
			socket = new MulticastSocket(multicastPort);
			if(networkInterface != null) {
				//socket.setInterface(InetAddress.getByName(networkInterface));
				socket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName(networkInterface)));
			}
			socket.joinGroup(multicastGroup);
			socket.setLoopbackMode(false);
		} catch(IOException e) {
			throw new RuntimeException("Error initializing network interface: " + e.getMessage());
		}
   
		bufTX = new byte[maxSize];
		bufRX = new byte[maxSize];
		packetTX = new DatagramPacket(bufTX, maxSize, multicastGroup, multicastPort);
		packetRX = new DatagramPacket(bufRX, maxSize);
    }
    
    /**
     * Start the layer.
     */
    public void startup() {
		nodeID = runtime.getNodeID();
		start();
    }
    
    /**
     * The method to get the next message on the socket.
     * <p>
     * This method will block
     * until a message is available. In fact this method will call the receive()
     * method from the MulticastSocket class, and once a message is received,
     * a new message Object (through MessagePool) will be created/obtained.
     * <p>
     * @return The message object obtained from the network (or null, if the
     *         message type is unknown)
     * <p> <b>Important note to developers</b>
     * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> in 
     * <b>ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer</b> have been
     * implemented to mimic the same behaviour as the ones in here regarding
     * return values, thrown exceptions and data writtern on System.out. If for
     * some reason this behaviour changes or is not needed anymore, the changes
     * should be propagated to ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer
     * as well.
     */
    protected Message getNetworkMessage() {
		try {
			socket.receive(packetRX);
		} catch(IOException e){
			throw new RuntimeException("Error receiving message: " + e.getMessage());
		}
	
		try {
			Message msg = messagePool.createMessage(packetRX.getData(),(short)packetRX.getLength());
			packetRX.setLength(maxSize); // this line seems necessary for certain java versions
			return msg;
		} catch(UnknownMessageTypeException umte) {
			System.out.println(">>> Unknown Message type: " + ((int)umte.getUnknownType()));
		}
		return null;
    }
    
    /**
     * The method to send a message over the network.
     * <p>
     * @param msg The message to be send
     * @throws IllegalArgumentException
     * @throws SendMessageFailedException
     * <p> <b>Important note to developers</b>
     * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> in 
     * <b>ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer</b> have been
     * implemented to mimic the same behaviour as the ones in here regarding
     * return values, thrown exceptions and data writtern on System.out. If for
     * some reason this behaviour changes or is not needed anymore, the changes
     * should be propagated to ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer
     * as well.
     */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException {
		int length = 0;
		if (msg == null) {
			throw new IllegalArgumentException("The message cannot be null");
		} else {
			// only set the source node and sequence number when not already set
			// this is because the routing layer may resend a message without it
			// being changed (i.e. the source node should be the one of the original node)
			if(msg.getNextHop() == -1)
				throw new SendMessageFailedException("Next hop not defined in message : "+msg);
			if(msg.getDstNode() == -1)
				throw new SendMessageFailedException("Destination not defined in message : "+msg);				
			if(msg.getSrcNode() == -1 || msg.getSrcNode() == nodeID) {
				msg.setSrcNode(nodeID);
				msg.setSequenceNumber(seqNumber);
				seqNumber = ((seqNumber + 1) & 0xFFFF);
			}
			msg.setPreviousHop(nodeID);
			length = msg.getByteArray(bufTX);
			packetTX.setLength(length);
			try {
				socket.send(packetTX);
			} catch (IOException e) {
				throw new SendMessageFailedException("Sending failed due to an I/O error.",e);
			}
		}
		messagePool.freeMessage(msg);
		return length;
    }
    
    /**
     * This method returns a unique ID for this node.
     * <p>
     * No assumption what so ever should be made on the format of this id. Don't try
     * to analyse the contents of this ID in anyway, just use it to identify a node.
     * <p>
     * @return A unique ID for this node.
     */
    public final long proposeNodeID() {
		byte[] adr = null;
		try {
			adr = InetAddress.getLocalHost().getAddress();
		} catch(Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
		return    (System.currentTimeMillis() & 0xFFFFFFFF) +
			((adr[0] & 0xFF) << 56) +
			((adr[1] & 0xFF) << 48) +
			((adr[2] & 0xFF) << 40) +
			((adr[3] & 0xFF) << 32);
    }
}
