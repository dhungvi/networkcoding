
package ch.epfl.lsr.adhoc.routing.ncode.feedback;

import java.net.InetAddress;
import java.util.Random;
import java.util.Vector;

import ch.epfl.lsr.adhoc.routing.ncode.*;
//import ch.epfl.lsr.adhoc.routing.ncode.NCdatagram;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.tools.VectorUtils;


/**
 * This layer implements : network coding.
 * <p>
 * Every message received, is stored in a buffer. At the time of transmission 
 * a random linear combination of packets in buffer is sent.
 * The message is stored in the buffer, when:
 * <ul>
 * <li>message not already received (srcNode & seqNumber)</li>
 * <li>TTL &gt; 0</li>
 * <li>srcNode not this node</li>
 * <li>dstNode not this node</li>
 * * </ul>
 * <p>
 * A decoded message is propagated up the stack, if its destination is this node, or
 * if it is a broadcast (destination node number is 0).
 * The message will be propagated up the stack, if:
 * <ul>
 * <li>The message is for this node</li>
 * <li>Or the message is a broadcast</li>
 * </ul>
 * <p>
 * @author Kave Salamatian
 * @version 1.0
 */
public class NcodeFB extends Ncode {

  private Parameters params;
  public char msgType;
  public char msgTypeFB;
  /** Used to retransmit a message after a random delay */
  private DelayedTransmission dt;
  /** Size of the buffer */
  private int bufferLength;
  /** The probability of retransmission (in percent) */
  private int MDU;	// max data unit
  private int MCLU;
  private int HCL;	// Hard Coefficient Limit
  private int recv_cntr = 0;
  /** Buffer containing the packet to decode */
  private Vector decodingBuf;
  /** Buffer containing the decoded packet */
  private Vector decodedBuf;
  
  private Vector map_list;
  private int rank;
  private Random rnd;
  private GaloisField base;
  private ExtendedGaloisField GF;

  private short fresh_fwd_factor;

  /**
   * For previously received messages, a list of their sequence numbers.
   * The variable sourceNodes contains the corresponding source node ids.
   */
  private int [] seqNumbers;
  /**
   * For previously received messages, a list of their source node ids.
   * The variable sourceNodes contains the corresponding sequence numbers.
   */
  private long [] sourceNodes;

  /** 
  	* I changed the random number generation mecanism in order to make
	* it compatible with the Jeode JVM available for the Sharp Zaurus. 
	* The common Random object has been replaced with the call to 
	* Math.random method. Jan 2004 dcavin
	*
   * The random number generator used to decide on retransmission 
  	* private Random rand = new Random();
   */
  
  /** The node if of this node. */
  private long myNodeID;
  /** A reference to message pool. */
  public MessagePool mp;

  /** Default constructor */
  public NcodeFB(String name, Parameters params){
	  super(name, params);
	  this.params = params;
	  decodedBuf=new Vector(0);
	  decodingBuf=new Vector(0);
  }

  /**
   * Initializes the network coding layer.
   * <p>
   * The following configuration parameters are read:
   * <ul>
   * <li><b>layer.Flooding.bufferLength</b> - The number of entries for
   *     identifying the most recently received messages</li>
   * <li><b>layers.Flooding.prT</b> - The probability that a message is retransmitted</li>
   * <li><b>layers.Flooding.delayMay</b> - The maximum delay for retransmitting a message</li>
   * </ul>
   *
   */
  public void initialize(FrancRuntime runtime) {
    try {
      bufferLength = params.getInt("bufferLength");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value bufferLength: " + pdnee.getMessage());
    }
    if(bufferLength < 0)
      throw new RuntimeException("Configuration variable bufferLength cannot be negative");
    int delayMax = 0;
    try {
      delayMax = params.getInt("delayMax");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value delayMax: " + pdnee.getMessage());
    }
    if(delayMax < 0)
      throw new RuntimeException("Configuration variable delayMax cannot be negative");

    seqNumbers = new int[bufferLength];
    sourceNodes = new long[bufferLength];
    this.dt = new DelayedTransmission(this, delayMax);
    this.myNodeID = runtime.getNodeID();
    this.mp = runtime.getMessagePool();
//	public NetCod_Module(int mdu, int mclu, int HCL, int port) throws GaloisException, UnknownHostException {	
		try {
		base = new GaloisField(2);
		GF = new ExtendedGaloisField(base,'a',8);
		} catch (GaloisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	map_list = new Vector();
	rank = 0;
    try {
        MDU = params.getInt("MaximumDataUnit");
    } catch(ParamDoesNotExistException pdnee) {
        throw new RuntimeException("Error reading configuration value MDU: " + pdnee.getMessage());
    }

    try {
        MCLU = params.getInt("MaximumCLU");
    } catch(ParamDoesNotExistException pdnee) {
        throw new RuntimeException("Error reading configuration value MCLU: " + pdnee.getMessage());
    }
    
    try {
        HCL = params.getInt("HCL");
    } catch(ParamDoesNotExistException pdnee) {
        throw new RuntimeException("Error reading configuration value HCL: " + pdnee.getMessage());
    }

    String msgName = null;
	try {
		msgName = params.getString("msgType");
	} catch(ParamDoesNotExistException pdnee) {
		pdnee.printStackTrace();
		throw new RuntimeException("Could not read configuration parameter 'msgType' " +
								   "for NCode layer: " + pdnee.getMessage());
	}
	if(msgName == null) {
		throw new RuntimeException("Could not read configuration parameter 'msgType' " +
								   "for NCode layer");
	}
	try {
		msgType = mp.getMessageType(msgName);
	} catch(Exception ex) {
		throw new RuntimeException("Message type '" + msgName + "' not found");
	}

	msgName = null;
	try {
		msgName = params.getString("msgTypeFB");
	} catch(ParamDoesNotExistException pdnee) {
		pdnee.printStackTrace();
		throw new RuntimeException("Could not read configuration parameter 'msgTypeFB' " +
								   "for NCode layer: " + pdnee.getMessage());
	}
	if(msgName == null) {
		throw new RuntimeException("Could not read configuration parameter 'msgTypeFB' " +
								   "for NCode layer");
	}
	try {
		msgTypeFB = mp.getMessageType(msgName);
	} catch(Exception ex) {
		throw new RuntimeException("Message type '" + msgName + "' not found");
	}

	rnd = new Random(System.currentTimeMillis());


  }

  /**
   * Starts the threads for this layer and for retransmitting messages after a
   * certain delay.
   */
  public void startup() {
    start();
    dt.start();
  }

  /**
   * This method implements what to do when we receive a message
   * it up in the stack.
   * For more details see the class description.
   * <p>
   * @param msg The message to handle
   */
  protected void handleMessage(Message msg){
	
    int sequenceNumber = msg.getSequenceNumber();
    long dstNode = msg.getDstNode();
    int ttl = msg.getTTL();
    int index = 0,s;
    boolean found = false;
    
    
	if(msg == null) {
		System.out.println("> # message is null");
		return;
	}	
	if(msg.getType() == msgType) {
//		NCdatagram nc = ((NCodeMessage)msg).getNCdatagram();
//		recv(nc);
		mp.freeMessage(msg);
		return;
	}
	if (msg.getType() == msgTypeFB){
		if (msg.getSrcNode()==myNodeID) {
			mp.freeMessage(msg);
			return;
		} else {
			System.out.println("Feedack Sent "+myNodeID);
			FeedbackData fbdata=((NCodeFBMessage)msg).getFbdata();
			if (fbdata.decoded_list.size()==0) {
				s=Math.max(10,decodedBuf.size()+decodingBuf.size());
			} else {
				s=(fbdata.NumbVar-fbdata.NumbEq);
			}
			for (int i=0;i<s;i++){
				  if (decodingBuf.size() + decodedBuf.size() > 0){
						Vector vectId=new Vector();
//						Coef_Elt coef;
				  }
			}
		}
	}
//						NCdatagram g =encode();						
//						if (g!=null){
//							for (int j=0; j < g.coefs_list.size();j++) {
//								coef=(Coef_Elt)g.coefs_list.elementAt(j);
//								vectId.add(coef.id_);
//							}
////							Vector v=VectorUtils.difference(g.coefs_list,vectId);
////							if (v.size() > 0){
//								NCodeMessage tm = (NCodeMessage)mp.getMessage(msgType);
//								tm.setNCdatagram(g);
//								tm.setDstNode(0);
//								tm.setTTL(1);
//								try {
//									send(tm);
//								} catch (SendMessageFailedException e) {
//									e.printStackTrace();
//								}
//								GlobalIndex.globalIndex++;
//							}
//						}
////					  }
//					  catch(GaloisException e) {
//							System.err.println("NetCod_Module: send: " + e);
//					  }
//				  }
//			}
//			mp.freeMessage(msg);
//			return;			
//		}
//	}
//	else {
// 		NCdatagram freshData = new NCdatagram(MDU,0);
//		msg.getByteArray(freshData.Buf);
//		
//		Coef_Elt coef_elt = new Coef_Elt(1);
//		coef_elt.length=freshData.Buf.length;
//		freshData.coefs_list.add(coef_elt);
//		decodedBuf.add(freshData);
//
//		NCodeMessage tm = (NCodeMessage)mp.getMessage(msgType);
//		tm.setNCdatagram(freshData);
//		tm.setDstNode(0);
//		tm.setTTL(1);
//		try {
//			send(tm);
//		} catch (SendMessageFailedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
// 		GlobalIndex.globalIndex++;
//		mp.freeMessage(msg);
//		return;
//	}
  }
  

	public void sendFbData() throws SendMessageFailedException {
		FeedbackData fbdata=new FeedbackData();
		int i, s=decodedBuf.size();
		
		Pkt_ID id;
		NCdatagram nc;
		Coef_Elt coef;
		
		for (i=0; i < s; i++ ) {
//			id=new Pkt_ID(PktIndex++);
			nc=(NCdatagram) decodedBuf.elementAt(i);
//			coef=(Coef_Elt) nc.coefs_list.elementAt(0);
//			id=coef.id_;
//			fbdata.decoded_list.add(id);
		}
		fbdata.NumbEq=decodingBuf.size();
		fbdata.NumbVar=map_list.size();
		NCodeFBMessage msg=new NCodeFBMessage(msgTypeFB);
		msg.setFbdata(fbdata);
		send(msg);

	}


//--------------------------------------------------------------------
	public byte int2byte(int val) {
		return (byte) val;
	}

	public int byte2int(byte val){
		int i=val;
		if (i <0 ) {
			i=i+256;
			return i;
		} else {
			return i;
		}
	}
}
