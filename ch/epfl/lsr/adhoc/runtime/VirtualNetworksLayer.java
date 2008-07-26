package ch.epfl.lsr.adhoc.runtime;

import java.util.*;


/**
 * This layer makes it possible to simulate different link qualities between
 * nodes.
 * <p>
 * The idea is to assign to every node one or more virtual networks, plus a
 * probability (in percent) for receiving a message that was sent on this network.
 * Every message that is sent by any node, is physically received by all nodes.
 * When the message passes through this layer however, the layer applies the
 * following steps to decide whether to propagate the message:
 * <ul>
 * <li>If the message was sent by this node, it is always propagated up the stack</li>
 * <li>For every network that this node participates in, it is tested whether the
 *     message was sent on this network</li>
 * <li>If the message was sent on one of the nodes networks, then the random
 *     generator is used to decide whether the message is to be propagated up the stack</li>
 * <li>If the message was not forwarded, the testing continues with the next
 *     network</li>
 * <li>If the message was not forwarded for all the networks for this node,
 *     it is discarded</li>
 * </ul>
 * <p>
 * @author Urs Hunkeler
 * @version 1.0
 */
public class VirtualNetworksLayer extends AsynchronousLayer {

    /**
     * An n x 2 array containing in every row an entry for a network for this node.
     * The first number is the network number, the second is the probability that
     * a message is received on this network.
     */
    private short[][] networks;
    /**
     * Byte array where the n-th bit is set to '1' if this node participates in
     * the n-th network. This information is added to every message sent through
     * this layer.
     */
    private byte[] netBits;
    /** The random number generator used for deciding whether to propagate a message */
    private Random rand;
    /** The node id of this node */
    private long nodeID;
    /** A reference to the message pool. */
    private MessagePool mp;
    
    /** Default constructor, initializes the random number geneartor */
    public VirtualNetworksLayer(String name, Parameters params) {
		super(name,params);
		System.out.println("Virtual networks layer : "+params);
		rand = new Random();
		int maxNetNum = 0;
		String names[] = params.names();
		int numNets = names.length;
		networks = new short[numNets][2];
		for(int i=0;i<numNets;i++) {
			String subName = names[i];
			short subNumber = (short)0;
			try {subNumber = params.getShort(subName);}
			catch(ParamDoesNotExistException pdnee) {
				throw new RuntimeException("Invalid parameter for subnet "+subName+".");
			}
			networks[i][0] = Short.parseShort(subName);
			networks[i][1] = subNumber;
			System.out.println("-> Add subnetwork: "+subName+"-"+subNumber);
			if(networks[i][0] > maxNetNum) maxNetNum = networks[i][0];
		}
		int numNetBytes = (short)(maxNetNum >> 3) + 1;
		netBits = new byte[numNetBytes];
		for(int i = 0; i < numNets; i++) {
			int nByte = networks[i][0] >> 3; // division par 8
			int nBit  = networks[i][0] &  7; // modulo 8
			netBits[nByte] = (byte)(netBits[nByte] | (1 << nBit)); // set the corresponding bit
		}
    }

    /**
     * This method is called to initialize the layer.
     * <p>
     * The information about VirtualNetworks for this node is read and the two
     * variables, networks and netBits, are initialized accordingly.
     * 
     */
    public void initialize(FrancRuntime runtime) {
		this.nodeID = runtime.getNodeID();
		this.mp = runtime.getMessagePool();		
    }
    
    /** Starts this layer's thread. */
    public void startup() {
		start();
    }
    
    /**
     * Decides whether a message should be propagated further up the stack.
     * <p>
     * For further details, see the class description.
     * <p>
     * @param msg The message to handle
     */
    public void handleMessage(Message msg) {
		boolean receive = false;
		if(msg.getSrcNode() == nodeID) {
			receive = true;
		} else {
			for(int i = 0; i < networks.length; i++) {
				if(msg.isInNetwork(networks[i][0])) {
					if(rand.nextInt(100) < networks[i][1]) {
						receive = true;
						break;
					}
				}
			}
		}
		if(receive) {
			super.handleMessage(msg);
		} else {
			mp.freeMessage(msg);
		}
    }
    
    /**
     * This method is used to send a Message.
     * <p>
     * This method adds information about the virtual networks on which this message
     * is sent, and then calls the superclass's sendMessage() method to send
     * the message.
     * <p>
     * @param msg The message object to be send
     * @throws IllegalArgumentException
     * @throws SendMessageFailedException
     */
    public int sendMessage(Message msg) throws SendMessageFailedException {
		msg.setNetworks(netBits);
		return super.sendMessage(msg);
    }
}
