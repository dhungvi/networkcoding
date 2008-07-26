

package ch.epfl.lsr.adhoc.routing.ncode;

import ch.epfl.lsr.adhoc.routing.ncode.GaloisException;

import java.lang.*;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;


import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.SimLogger;



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
public class Ncode extends AsynchronousLayer {


//  private static final SimLogger logger = new SimLogger(Ncode.class);
  private Parameters params;
  public char msgType;
  /** Used to retransmit a message after a random delay */
  private DelayedTransmission dt;
  /** Size of the buffer */
//  private int bufferLength;
  /** The probability of retransmission (in percent) */
//  private int MDU;	// max data unit
  private int MCLU;
  private int HCL;	// Hard Coefficient Limit
  private int recv_cntr = 0;
//  private Vector PktBuffer;
  /** List containing the packet to decode index */
  private Vector decodingBuf;
  /** List containing the decoded packet index */
  private Vector decodedBuf;
  
  private NCdatagramPool ncpool;
  private CoefEltPool cfpool;
  private byte [] outBuf=new byte[Globals.MaxBufLength];
  private byte [] inBuf=new byte[Globals.MaxBufLength];
  private byte [] encBuf=new byte[Globals.MaxBufLength];
  
  //  private NCdatagram tmpEncDatagram;
  private Vector varList;
  private Vector decodedList;
//  private Hashtable decodedList;
  private int rank;
  private int[][] A;
  private Random rnd;
  private GaloisField base;
  private ExtendedGaloisField GF;
  public int PktIndex;
  
  private InetAddress source, destination;
  private static SimLogger logger;
  
  /** The node if of this node. */
  private long myNodeID;
  /** A reference to message pool. */
  public MessagePool mp;
  
  boolean simulMode;

  /** Default constructor */

  public Ncode(String name, Parameters params){
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
   * <li><b>layers.NCcode.prT</b> - The probability that a message is retransmitted</li>
   * <li><b>layers.NCcode.delayMay</b> - The maximum delay for retransmitting a message</li>
   * </ul>
   *
   */
  public void initialize(FrancRuntime runtime) {

	int delayMax = 0;
	try {
      delayMax = params.getInt("delayMax");
    } catch(ParamDoesNotExistException pdnee) {
      throw new RuntimeException("Error reading configuration value delayMax: " + pdnee.getMessage());
    }
    if(delayMax < 0)
      throw new RuntimeException("Configuration variable delayMax cannot be negative");

    this.dt = new DelayedTransmission(this, delayMax);
    this.myNodeID = runtime.getNodeID();
    this.mp = runtime.getMessagePool();
    this.cfpool=new CoefEltPool();
    this.ncpool=new NCdatagramPool(cfpool);
//	base = Globals.base;
	GF = Globals.GF;
//	map_list = new Vector();
	varList=new Vector();
	decodedList= new Vector();
	
	rank = 0;
    try {
    	Globals.MaxBufLength=params.getInt("MaximumDataUnit");
    } catch(ParamDoesNotExistException pdnee) {
        throw new RuntimeException("Error reading configuration value MDU: " + pdnee.getMessage());
    }

    try {
        MCLU = params.getInt("MaximumCLU");
    } catch(ParamDoesNotExistException pdnee) {
        throw new RuntimeException("Error reading configuration value MCLU: " + pdnee.getMessage());
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

	rnd = new Random(System.currentTimeMillis());
	PktIndex=rnd.nextInt();

	logger=Globals.logger;
//	PropertyConfigurator.configure(
//			DefaultLoggerConfig.getLoggerConfig("error.log", "simulation.log"));
	simulMode= runtime.isSimulMode();

	
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
  protected void handleMessage(Message msg) {
	
    int sequenceNumber = msg.getSequenceNumber();
    long srcNode = msg.getSrcNode();
    long dstNode = msg.getDstNode();
    int ttl = msg.getTTL();
    int index = 0;
    boolean found = false;
    
    
	if(msg == null) {
		System.out.println("> # message is null");
		return;
	}
	if (msg.getSrcNode()==myNodeID) {
		// Loopback !
		return;
	}
	if(msg.getType()==msgType) {
		// it is a Ncode packet
		synchronized(inBuf) {
			int len=((NCodeMessage)msg).getInBuf(inBuf);
			NCdatagram nc=ncpool.getNCdatagram();
			synchronized(nc) {
//				nc.free(cfpool);
//				nc.validate();
				try {
					if (nc.unserialize(inBuf,cfpool)>0) {
						logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" RCVFROM: "+msg.getSrcNode()+" IND: "+nc.getIndex()+" #COEF: "+nc.coefs_list.size());
						decode(nc);					
					} else {
						//Nothing to do !!!
					}
					ncpool.freeNCdatagram(nc); 
				} catch(RuntimeException e) {
				//	 Exception in decoding the packet
	  			// Should not happen but seems to happen because of fragmentation !!!
//	  				return 0;
					 logger.debug("Fragmentation happened !!");
				 }
			}
		}
		mp.freeMessage(msg);
		// What to do with processes NCdatagram ?
	} else {
//		logger.error("Unknown message received !");
        super.handleMessage(msg);
	}
  }
  
	public int sendMessage(Message msg) throws SendMessageFailedException {		
		Coef_Elt coef;
		NCdatagram tmpNCdatagram = (NCdatagram) ncpool.getNCdatagram();
		if (msg.getType()==5) {
			if(msg.getTTL() <= 0) return 0;	
	  		msg.setNextHop(0);
	  		return super.sendMessage(msg);
		} else {
			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" APPLI: IN "+msg); 					
			int l=msg.getByteArray(tmpNCdatagram.Buf);
			tmpNCdatagram.setLength(l);
			tmpNCdatagram.setDecoded();
			tmpNCdatagram.setIndex(PktIndex);
			coef=cfpool.getCoefElt(1,PktIndex++);
			tmpNCdatagram.coefs_list.put(coef.key(),coef);
//		generateEncoded();
//		synchronized(outBuf) {
//			int len=tmpNCdatagram.serialize(outBuf);
//			NCodeMessage tm = (NCodeMessage)mp.getMessage(msgType);
		
//			tm.setOutbuf(outBuf,len);
//			tm.setDstNode(0);
//			tm.setTTL(1);
//			send(tm);
//		}
//		logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" SEND: N "+" IND: "+tmpNCdatagram.getIndex()+" #COEF: "+tmpNCdatagram.coefs_list.size()); 
//		logger.info("At time: "+System.currentTimeMillis()+":NODE ID "+myNodeID+": new Packet send with "++" coeffs");
			decodedBuf.add(tmpNCdatagram);
			decodedList.add(coef.getID());
//		decodedList.add(new Integer(PktBuffer.size()));
		
//		decodedBuf.add(freshData.clone());
//		fresh_send_ctr = fresh_fwd_factor;
//		PktIndex++;

//		generated();
			mp.freeMessage(msg);
		} 
		return 1;
	}

	public void generateEncoded() { 
//		NCdatagram tmpEncDatagram=ncpool.getNCdatagram();
		NCdatagram tmpEncDatagram;
		
		try{
			if (!decodedBuf.isEmpty()) {
				tmpEncDatagram=encode();
				if (tmpEncDatagram!= null && !tmpEncDatagram.isNull()) {
//					tmpEncDatagram.validate();
					synchronized(tmpEncDatagram){
						tmpEncDatagram.setIndex(PktIndex++);
//						tmpEncDatagram.validate();
						int len=tmpEncDatagram.serialize(encBuf);					
						NCodeMessage tm = (NCodeMessage)mp.getMessage(msgType);
						tm.setOutbuf(encBuf,len);
						tm.setDstNode(0);
						tm.setTTL(1);
						logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" SEND: E "+" IND: "+tmpEncDatagram.getIndex()+" #COEF: "+tmpEncDatagram.coefs_list.size()); 					
						send(tm);
//					 	Should we free the tmpEncDatagram ! to check if send(tm) could be made synchronized ! 	  
						ncpool.freeNCdatagram(tmpEncDatagram);
					}				
				}
			}
		}
	  catch(GaloisException e) {
			logger.error("NetCod_Module: generate Encoded: " + e);
	  } catch (SendMessageFailedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	
	
	public int  send(Message msg) throws SendMessageFailedException {		
		if(msg.getTTL() <= 0) return 0;	
  		msg.setNextHop(0);
  		return super.sendMessage(msg);
	}


//--------------------------------------------------------------------	
/**
* blbla
* @param int[]
* @return NCdatagram 
*/
	public synchronized NCdatagram encode() throws GaloisException {
			int L,l;
			int choice, order,coef;
			NCdatagram g, gclone=ncpool.getNCdatagram();
			boolean flag=false;
			
			NCdatagram nc=ncpool.getNCdatagram();
//			nc.validate();
			L=decodedBuf.size();
			l=(decodedBuf.size() + varList.size());
			// For encoding we should have received packets 
			if (l==0) {
				return null;
			}
			// As a First try we do not put any upper bound on the number of coefficient 
			synchronized(decodingBuf) {
				int len=(decodingBuf.size() + decodedBuf.size());
				// len should be larger than 0 unless an Error 
				if (len ==0) {
					logger.error("ERROR : LEN ==0 while L!=0");
				}
				// choose number of packets to mix, ensure that it is larger than 1
				order=Math.max(rnd.nextInt(Math.min(MCLU,l)),1);
				for (int i=0;i<order; i++) {
					NCdatagram nc1=ncpool.NCdatagramclone(nc);
//					nc1.validate();
					choice=rnd.nextInt(len);
					// We should insure that coef !=0
					coef= rnd.nextInt(255-1)+1;				
					if (choice > (L - 1)) {
						choice=choice-L;
						g=(NCdatagram)decodingBuf.elementAt(choice);
					} else {
						g=(NCdatagram)decodedBuf.elementAt(choice);
					}
//					g.validate();
					gclone=(NCdatagram) ncpool.NCdatagramclone(g);
					if (i>0) {					
						nc= nc.sum(gclone.product(coef),cfpool);
					} else {
						nc=gclone.product(coef);
					}	
//				ncpool.freeNCdatagram(gclone);
//					nc.validate();
				}
			}
			nc.setIndex(PktIndex++);
			return nc;	
			
				
/*			//Now we are sure that some packets exists	
				order=rnd.nextInt(Math.min(MCLU,l))+2;
			} else {
				order =1;
			}
			
			int len=(decodingBuf.size() + decodedBuf.size());
			if (len > 0) {
				while(true) {		
					index++;
					gclone=(NCdatagram) g1.clone();
					choice=rnd.nextInt(len);
					// we should ensure that the coef is not null !
					coef= rnd.nextInt(255-1)+1;
					if (choice > (L - 1)) {
						choice=choice-L-1;
						g1 = NC_sum(g1,NC_product(coef, (NCdatagram) decodingBuf.elementAt(choice)));
						flag=true;
					} else {
						g1 = NC_sum(g1,NC_product(coef , (NCdatagram) decodedBuf.elementAt(choice)));				
					}
					if (g1.coefs_list.size()> order) {
						g1=gclone;
					} else {
						if (g1.coefs_list.size()==order){
							break;
						} 
					}
				} 
				validate(g1);
				return g1;	
			} else {
				System.out.println("NULLLLLLLLLL !");
				return null;
			}
*/	}

////--------------------------------------------------------------------	
//	public synchronized NCdatagram NC_product(int coef, NCdatagram g) throws GaloisException {
//
//		int i,j,k;
//		Coef_Elt coeftp;
//		NCdatagram gc = (NCdatagram) ncpool.NCdatagramclone(g);
//		
//		// handling the coefs_list
//  	  	Enumeration ec = gc.coefs_list.elements();
//		while(ec.hasMoreElements())
//		{
//			coeftp=(Coef_Elt) ec.nextElement();
//			coeftp.coef_ = GF.product(coeftp.coef_ , coef);
//		}
//		
//
//		// handling the data in the StringBuffer
////		System.out.println("NC_product " + gc.Buf.length());
//		for(i = 0; i < g.dataLength ; i++) {			
//			gc.Buf[i]=int2byte(GF.product(byte2int(g.Buf[i]), coef)); 				
//		}		
//		
//		return gc;
//	}
//
//
////--------------------------------------------------------------------	
//	public synchronized NCdatagram NC_sum(NCdatagram g1, NCdatagram g2) throws GaloisException {
//
//		int i,j;
//
//		Coef_Elt coef, coefp;
//
////		Vector list=(Vector)g1.coefs_list.clone();
////		Vector list= new Vector(0);
//		
//		synchronized(g1) {
//			synchronized(g2) {
//				Enumeration egp=g1.coefs_list.elements();
//				while (egp.hasMoreElements()) {
//					coefp=(Coef_Elt) egp.nextElement();
//					coef= (Coef_Elt) g2.coefs_list.get(coefp.key());
//					if (coef != null) {
//						coef.coef_=GF.sum(coef.getCoef(),coefp.getCoef());
//						if (coef.coef_==0) {
//							g1.coefs_list.remove(coef.key());
//						} else {
//							coef=cf.coefclone(coefp);
//							g1.coefs_list.put(coef.key(),coef);	
//							}
//					}
//				}
//
//		
//		
//		Enumeration eg=g2.coefs_list.elements();
//		while (eg.hasMoreElements()) {
//			coef=(Coef_Elt) eg.nextElement();
//			coefp=cfpool.coefclone(coef);
//			g1.coefs_list.add(coefp);
//		}
//		
//		
//		for (i=0;i<g1.coefs_list.size();i++) {
//			coef=(Coef_Elt)g1.coefs_list.elementAt(i);
//			for (j=i+1;j<g1.coefs_list.size();j++){
//				coefp=(Coef_Elt)g1.coefs_list.elementAt(j);
//				if (coefp.equals(coef)) {
//					coef.coef_=GF.sum(coef.getCoef(),coefp.getCoef());
//					g1.coefs_list.remove(j);
//				}
//			}
//			if (coef.coef_==0) {
//				g1.coefs_list.remove(i);
//			} else {
//				g1.coefs_list.setElementAt(coef,i);
//			}
//		}
//		
//		if (g1.dataLength>=g2.dataLength){
//			for(i = 0; i < g2.dataLength ; i++) {			
//				g1.Buf[i]=int2byte(GF.sum(g1.Buf[i], g2.Buf[i]));				
//			}	
//		} else {
//			for(i = 0; i < g1.dataLength ; i++) {			
//				g1.Buf[i]=int2byte(GF.sum(g1.Buf[i], g2.Buf[i]));				
//			}
//			System.arraycopy(g2.Buf,g1.dataLength,g1.Buf,g1.dataLength,g2.dataLength-g1.dataLength);
//		}		
//		return g1;
//	}
//
////--------------------------------------------------------------------	
//
//	public NCdatagram NC_minus(NCdatagram g1, NCdatagram g2) throws Exception {
//		int i,j;
//
//		Coef_Elt coef, coefp;
//
////		Vector list=(Vector)g1.coefs_list.clone();
////		Vector list= new Vector(0);
//		
//		
//		Enumeration eg=g2.coefs_list.elements();
//		while (eg.hasMoreElements()) {
//			coef=(Coef_Elt) eg.nextElement();
//			coefp=cfpool.coefclone(coef);
//			g1.coefs_list.add(coefp);
//		}
//		
//		
//		for (i=0;i<g1.coefs_list.size();i++) {
//			coef=(Coef_Elt)g1.coefs_list.elementAt(i);
//			for (j=i+1;j<g1.coefs_list.size();j++){
//				coefp=(Coef_Elt)g1.coefs_list.elementAt(j);
//				if (coefp.equals(coef)) {
//					coef.coef_=GF.minus(coef.getCoef(),coefp.getCoef());
//					g1.coefs_list.remove(j);
//					cfpool.freeCoef(coefp);
//				}
//			}
//			if (coef.coef_==0) {
//				g1.coefs_list.remove(i);
//			} else {
//				g1.coefs_list.setElementAt(coef,i);
//			}
//		}
//		
//		if (g1.dataLength>=g2.dataLength){
//			for(i = 0; i < g2.dataLength ; i++) {			
//				g1.Buf[i]=int2byte(GF.minus(g1.Buf[i], g2.Buf[i]));				
//			}	
//		} else {
//			for(i = 0; i < g1.dataLength ; i++) {			
//				g1.Buf[i]=int2byte(GF.minus(g1.Buf[i], g2.Buf[i]));				
//			}
//			System.arraycopy(g2.Buf,g1.dataLength,g1.Buf,g1.dataLength,g2.dataLength-g1.dataLength);
//		}		
//		return g1;
//	}


//--------------------------------------------------------------------	
	public synchronized  NCdatagram reduce(NCdatagram g) throws GaloisException, Exception {
	
		int i,j;
		NCdatagram g1,g2,g3;
		Coef_Elt coef, coef1;
		int index;
        
        
		Enumeration eg=g.coefs_list.elements();
		while (eg.hasMoreElements()) {
			coef=(Coef_Elt) eg.nextElement();
			index=decodedList.indexOf(coef.getID());
			if (index!=-1) {
				g1=(NCdatagram) decodedBuf.elementAt(index);
				g2=ncpool.NCdatagramclone(g1);
				g3=ncpool.NCdatagramclone(g);
				g.minus(g2.product(coef.getCoef()),cfpool);
//				g.validate();
				g3.minus(g2,cfpool);

				ncpool.freeNCdatagram(g2);
			}
		}
		return g;					
	}


//--------------------------------------------------------------------	
	public synchronized void update_varList(NCdatagram g) throws Exception {

		Coef_Elt coef;
		
		// first detect if new variables exists !
		Enumeration eg=g.coefs_list.elements();
		while (eg.hasMoreElements()) {
			coef=(Coef_Elt) eg.nextElement();
			Pkt_ID id=coef.getID(); 
			if (!varList.contains(id)) {				
				varList.add(id);
			}
		}
	}



//--------------------------------------------------------------------	
	public synchronized void genA() throws GaloisException, Exception {

		int k,ind;
		NCdatagram g;
		Pkt_ID id,idp;
		Coef_Elt coef;		
		// Number of variables
		int N=varList.size();
		// Number of equations
		int M=decodingBuf.size();
		int index=0;
		if (M>N) {
			logger.error("Problem !");
		}
		
		A=new int[M][N];
		
		int i=0;
		Enumeration eg=decodingBuf.elements();
		while(eg.hasMoreElements()) {
			g=(NCdatagram) eg.nextElement();
			Enumeration ecoef=g.coefs_list.elements();			
			while (ecoef.hasMoreElements()) {
				coef=(Coef_Elt) ecoef.nextElement();
				id=coef.getID();
				index=varList.indexOf(id);
				if (index==-1) {
					System.out.println("ERROR in genA");
				}
				A[i][index]=coef.getCoef();
			}					
			i++;							
		}
	}			


//--------------------------------------------------------------------	
	public synchronized int GausElim(int M, int N) throws GaloisException, Exception {

		// Input:
		// M: Number of equations
		// N: Number of variables
		// return:
		// dimension of the coefficient matrix after gaussian elimination.

		NCdatagram g,g1,g2;
		int[] tmp;
		int k,i,j,n;
		int pivot;

		rank = 0;

		// Main Loop : # of iteration = # of lines
		for(k = 0; k < M ; k++) { 
			// Check if the pivot is zero and a swap is needed			
			boolean SWAP = false;
			// first check if we can exchange with a column larger than k
			while(!SWAP) { 
				if(A[k][k] == 0) {
					// if the pivot is zero we should exchange line or column order !
					for(n = k+1 ; n < N ; n++){
						if(A[k][n] != 0) {
							// we have found a column for exchange. Let's swap.
							// Caution: when swapping column we have to take care of map_list!
							Permut_col(A, k, n, M);
							SWAP = true;
							break;
						}
					} 
					if (!SWAP) {
						// We have a full zero line = an equation is linearly dependent !
						// we have to remove it !
						// we search for a line to exchange with it. Begin with the last line
						// and reduce matrix size
						if (k==(M-1)) {
							// we have reached the last line swapping is useless 
							SWAP=true;
							// This line should be removed
							M=M-1;
							break;
						} else { 
							M=M-1;
							Permut_line(A,M,k);
						}
					}
				} 
				if (k<M) {
					// the pivot is not zero. Let's do the operation
					SWAP=true;
					rank++;
					pivot = A[k][k];
					if (pivot != 1) {
						// we have to rescale the line by the pivot
						g=(NCdatagram) decodingBuf.elementAt(k);												
//						g.line_validate(A[k],N);
//						g.validate();
						for(i= k; i < N ; i++) {
							A[k][i] = GF.divide(A[k][i], pivot);
						}
						g.product(GF.divide(1,pivot));
//						g.line_validate(A[k],N);
						
//						for(i=0;i<g.dataLength;i++) {							
//							g.Buf[i] = int2byte(GF.divide(byte2int(g.Buf[i]),pivot));
//						}						
						
//						g.validate();
//						g.line_validate(A[k],N);

					}
					// make the value under the pivot equal zero 
					for(i = k+1; i < M ; i++) {  // Line index
						if (A[i][k]!=0) {
							int p=A[i][k];
							for(j = k; j < N ; j++) { //Column index
								A[i][j] =  GF.minus(A[i][j], GF.product(p, A[k][j]));
							}							
							g1=(NCdatagram) decodingBuf.elementAt(i);
							g2=ncpool.NCdatagramclone((NCdatagram) decodingBuf.elementAt(k));
							g1.minus(g2.product(p),cfpool);
//							if (g1.dataLength>=g2.dataLength){
//								for(j=0;j<g2.dataLength;j++) {
//									g1.Buf[j] = int2byte(GF.minus(byte2int(g1.Buf[j]),GF.product(A[i][k],byte2int(g2.Buf[j]))));
//								}
//							} else {
//								for(j=0;j<g1.dataLength;j++) {
//									g1.Buf[j] = int2byte(GF.minus(byte2int(g1.Buf[j]),GF.product(A[i][k],byte2int(g2.Buf[j]))));
//								}
//								for(j=g1.dataLength;j<g2.dataLength;j++) {
//									g1.Buf[j] = int2byte(GF.minus(0,GF.product(A[i][k],byte2int(g2.Buf[j]))));
//								}
//							}
//							A[i][k]=0;
//							g1.line_validate(A[i],N);
//							g1.validate();
						}
					}
				}
			}
//			line_validate(A[k],M,N);
		}
		extractSolved(M,N);
		return M;
	}


//--------------------------------------------------------------------	
// Function swapping column of the coefficient matrix taking care of map_list. 	
	public synchronized void Permut_col(int[][] B, int col1, int col2, int L) {
		// B: The coefficient matrix
		// col1, col2: column to swap
		// L : length of column
		int tmpd;
		if (col1!=col2) {
			// 	swap column in map_list
			Pkt_ID id = (Pkt_ID) varList.elementAt(col1);
			varList.set(col1, varList.elementAt(col2));
			varList.set(col2, id);

			// swap column in coefficient matrix
			for(int l = 0; l < L; l++){
				tmpd = B[l][col1];
				B[l][col1] = B[l][col2];
				B[l][col2] = tmpd;
			}
		}
	}

//	--------------------------------------------------------------------	
//	 Function swapping line of the coefficient matrix taking care of decodingBuf. 	
		public synchronized void Permut_line(int[][] B, int lin1, int lin2) {
			// B: The coefficient matrix
			// lin1, lin2: column to swap
			int[] tmp;
			if (lin1 != lin2) {
				//swap line in coefficient matrix
				tmp = B[lin1];
				B[lin1] = B[lin2];
				B[lin2] = tmp;
				// Swap element in decodingBuf
				Object TMP=decodingBuf.elementAt(lin1);
				decodingBuf.set(lin1,decodingBuf.elementAt(lin2));
				decodingBuf.set(lin2,TMP);
			}
		}


//--------------------------------------------------------------------	
	public synchronized void extractSolved(int M, int N) throws GaloisException, Exception {

		int i,j,k,l,upPivot,cntr;
		NCdatagram g,g1,g2;
		Vector list=new Vector(0);
		Coef_Elt coef;
		Pkt_ID id,idp;
		boolean Solved=true;
//		Vector clone=new Vector(decodingBuf.size());
		
//		for (i=0;i<decodingBuf.size();i++) {
//			clone.add(((NCdatagram)decodingBuf.elementAt(i)).clone());
//		}

//		int[][] B=new int[M][L];
		
//		for (i=0; i<M; i++) {
//			for (j=0; j<L; j++ ) {
//				B[i][j]=A[i][j];
//			}
//		}

//		decodingBuf.clear();
		if (M < decodingBuf.size()){
			for (i=M;i<decodingBuf.size();i++){
				g=(NCdatagram) decodingBuf.elementAt(i);
				decodingBuf.remove(i);
			}
		}
		//Check if one variable have been determined

		for (i=M-1;i>=0;i--) {
        	Solved=true;
        	// Prepare the NCdatagram
        	g1=(NCdatagram)decodingBuf.elementAt(i);
        	synchronized(g1) {
    			Enumeration ecoef=g1.coefs_list.elements();
    			while (ecoef.hasMoreElements()) {
    				coef=(Coef_Elt) ecoef.nextElement();
        			cfpool.freeCoef(coef);
    			}
//    			g1.coefs_list.removeAllElements();
    			g1.coefs_list.clear();

        		id = (Pkt_ID) varList.elementAt(i);        		        		
        		coef=cfpool.getCoefElt(A[i][i], id.index_);        		        		
        		coef.setSaddr(id.getSaddr());
        		g1.coefs_list.put(coef.key(),coef);
        		for (j=i+1;j<N;j++) {
        			if (A[i][j]!=0) {        			
        				Solved=false;
        				id = (Pkt_ID) varList.elementAt(j);
        				coef=cfpool.getCoefElt(A[i][j], id.index_);
        				coef.setSaddr(id.getSaddr());				
        				g1.coefs_list.put(coef.key(),coef);
        			}
        		}
//        		g1.validate();
        		if (Solved) {
    				if (g1.Buf[1]!=32) {
    					logger.error("OHHHHHHHHHHH ERRRRRRRRORRRR !!");
    				}    					
        			// a variable has been solved
        			// First propagate this info in higher lines (equations)
        			for (k=i-1;k>=0;k--) {
        				// make the value up the pivot equal zero
        				upPivot=A[k][i];
        				if (upPivot!=0){
        					for(l = k+1; l < N ; l++) { //Column index
        						A[k][l] =  GF.minus(A[k][l], GF.product(upPivot, A[i][l]));
        					}
        					g2=(NCdatagram)decodingBuf.elementAt(k);
        					for(l = 0; l < Math.max(g1.dataLength,g2.dataLength) ; l++) { //Column index    						
        						g2.Buf[l] = int2byte(GF.minus(byte2int(g2.Buf[l]), GF.product(upPivot, byte2int(g1.Buf[l]))));
        					}
        				}
        			}
        			//
        			//	Transfer the decoded packet to decodedbuffer;
//        			g1.validate();
        		
        			decodedBuf.add(g1);
        			if (g1.coefs_list.size() !=1) {
        				System.out.println("Error in decoded Packet !");
        			}
        			ecoef=g1.coefs_list.elements();			
        			coef=(Coef_Elt) ecoef.nextElement();
        			decodedList.add(coef.getID());        			
         			
        			Message msg = mp.createMessage(g1.Buf,(short)g1.Buf.length);
        			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" TO APPLI: "+msg);
        			super.handleMessage(msg);
    				//update matrix A
    				//swap lines
    				Permut_line(A,M-1,i);
    				//swap column
    				Permut_col(A,N-1,i,M);
    				//remove the variable    				
    				varList.remove(N-1);
    				decodingBuf.remove(M-1);
//    				ncpool.freeNCdatagram(g1);
    				M--;
    				N--;
        		}
        	}
        }
		if (M>N) {
			logger.fatal("Problem !");	
		}
 		
	}




//--------------------------------------------------------------------	
	public void decode(NCdatagram g) {
		
		// received packet validity check
		int tmpNumCoef=g.coefs_list.size();
//		g.validate();
		NCdatagram g1=ncpool.NCdatagramclone(g);
		recv_cntr++;
		try{
			g = reduce(g);
		}
		catch(Exception ev) {
			logger.error("NetCode_Module: insert: reduce " + ev);
		}
//		g.validate();
		
		if(g.coefs_list.size() == 0) {
			if (simulMode) {
				logger.info("At time: "+System.currentTimeMillis()+" NODE ID: "+myNodeID+" INDEX: "+ g.getIndex()+" #COEF: "+tmpNumCoef+"ACT: Useless packet!!!");				
			} else {
				logger.info("At time: "+System.currentTimeMillis()+" NODE ID: "+myNodeID+" INDEX: "+ g.getIndex()+" #COEF: "+tmpNumCoef+"ACT: Useless packet!!!");
			}
			
//			System.out.println("At time: "+System.currentTimeMillis()+" NODE ID: "+myNodeID+" INDEX: "+ g.getIndex()+" #COEF:"+tmpNumCoef+"ACT: Useless packet!!!");
			return;
		}
//		synchronized(decodingBuf) {
			decodingBuf.insertElementAt(ncpool.NCdatagramclone(g),0);
//		}

		try{
			update_varList(g);
		}
		catch(Exception e1) {
			System.err.println("NetCode_Module: insert: update_map_list " + e1);
		}
		try{
			// M: number of equations
			int M = decodingBuf.size();
			// N: number of variables
			int N = varList.size();
			if (M>N) {
				logger.fatal("Problem !");	
			}
			if (A !=null) {
				int[][] B=(int[][]) A.clone();
				for (int i=0; i< M-1; i++) {
					B[i]=(int[]) A[i].clone();
				}
			} else {
				int[][] B=null;
			}
			
			genA();

			try {
				int[][] B1;
				if (A !=null) {
					B1=(int[][]) A.clone();
					for (int i=0; i< M; i++) {
						B1[i]=(int[]) A[i].clone();
					}
				} else {
					B1=null;
				}

			// L: column length (Number of variables+size of payload)
//			L = N + MDU;

				logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" STATUS: B "+M+" "+N);			
				M = GausElim(M, N);
				B1=B1;
			} catch(Exception e3) {
				logger.error("NetCode_Module: decode : GausElim" + e3);
			}
			if (M>N) {
				logger.error("Problem !");	
			}
			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" STATUS: A "+decodingBuf.size()+" "+varList.size());
			//		decodable();
		}
		catch(GaloisException e) {
			System.err.println("NetCode_Module: insert: decode" + e);
		}
		catch(Exception e3) {
			System.err.println("NetCode_Module: insert: decode " + e3);
		}

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

	
