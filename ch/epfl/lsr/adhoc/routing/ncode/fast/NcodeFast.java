

package ch.epfl.lsr.adhoc.routing.ncode.fast;

import ch.epfl.lsr.adhoc.routing.ncode.CoefEltPool;
import ch.epfl.lsr.adhoc.routing.ncode.Coef_Elt;
import ch.epfl.lsr.adhoc.routing.ncode.DelayedTransmission;
import ch.epfl.lsr.adhoc.routing.ncode.ExtendedGaloisField;
import ch.epfl.lsr.adhoc.routing.ncode.GaloisException;
import ch.epfl.lsr.adhoc.routing.ncode.GaloisField;
import ch.epfl.lsr.adhoc.routing.ncode.NCGlobals;
import ch.epfl.lsr.adhoc.routing.ncode.NCdatagram;
import ch.epfl.lsr.adhoc.routing.ncode.NCdatagramPool;
import ch.epfl.lsr.adhoc.routing.ncode.NCodeMessage;
import ch.epfl.lsr.adhoc.routing.ncode.Pkt_ID;

import java.lang.*;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
//import ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer;
import ch.epfl.lsr.adhoc.runtime.DefaultLoggerConfig;
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
public class NcodeFast extends AsynchronousLayer {

 // private static final SimLogger logger = new SimLogger(NcodeFast.class);
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
  private Vector<NCFastdatagram> decodingBuf;
  /** List containing the decoded packet index */
  private Vector<NCFastdatagram> decodedBuf;
  
  private NCFastdatagramPool ncpool;
  private CoefEltPool cfpool;
  protected byte [] outBuf=new byte[NCGlobals.MaxBufLength];
  protected byte [] inBuf=new byte[NCGlobals.MaxBufLength];
  protected byte [] encBuf=new byte[NCGlobals.MaxBufLength];
  
  //  private NCdatagram tmpEncDatagram;
  private Vector<Pkt_ID> varList;
  private Vector<Pkt_ID> decodedList;

  private int rank;
  private int[][] A;
  private Random rnd;
  // GF initialization
  int Lfield;
  GField GF;
//  int[] BIT;
//  int[] ExptoFE;
//  int[] FEtoExp;  
  int GF_Size;
  public int PktIndex;
  protected int recvPkt=0;
  protected int sendPkt=0;
  protected static SimLogger logger;

  /** The node if of this node. */
  private long myNodeID;
  /** A reference to message pool. */
  public MessagePool mp;
  
  boolean simulMode;

  /** Default constructor */

  public NcodeFast(String name, Parameters params){
	  super(name, params);
	  this.params = params;
//	  PktBuffer=new Vector(0); 
	  decodedBuf=new Vector<NCFastdatagram>(0);
	  decodingBuf=new Vector<NCFastdatagram>(0);
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
	try {
        Lfield = params.getInt("LFIELD");
	} catch(ParamDoesNotExistException pdnee) {
		Lfield=8;
	}
	GF=new GField(Lfield);

    this.ncpool=new NCFastdatagramPool(cfpool,GF);

	varList=new Vector<Pkt_ID>();
	decodedList= new Vector<Pkt_ID>();
	
	rank = 0;
    try {
    	NCGlobals.MaxBufLength=params.getInt("MaximumDataUnit");
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

	PropertyConfigurator.configure(
			DefaultLoggerConfig.getLoggerConfig("error.log", "simulation.log"));
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
		// it is a NcodeFast packet
		synchronized(inBuf) {
			int len=((NCodeFastMessage)msg).getInBuf(inBuf);
			NCFastdatagram nc=ncpool.get();
			synchronized(nc) {
				try {
					if (nc.unserialize(inBuf,len, cfpool)>0) {
						logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" RCVFROM: "+msg.getSrcNode()+" IND: "+nc.getIndex()+" #COEF: "+nc.coefs_list.size());
						decode(nc);					
					} else {
						//Nothing to do !!!
					}
					ncpool.free(nc);
				} catch(RuntimeException e) {
					//	 Exception in decoding the packet
						 logger.debug("Fragmentation happened !!");
					 }
			}
		}
		mp.freeMessage(msg);
		// What to do with processes NCdatagram ?
	} else {
		logger.error("Unknown message received !");
	}
  }
  
	public int sendMessage(Message msg) throws SendMessageFailedException {		
		Coef_Elt coef;
		NCFastdatagram tmp = (NCFastdatagram) ncpool.get();
		if (msg.getType()==5) {
			if(msg.getTTL() <= 0) return 0;	
	  		msg.setNextHop(0);
	  		return super.sendMessage(msg);
		} else {
			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" APPLI: IN "+msg); 					
			int l=msg.getByteArray(inBuf);
			tmp.toIntBuf(inBuf,0, l);
			tmp.setLength(l);
			tmp.setIndex(PktIndex);
			coef=cfpool.getCoefElt(1,PktIndex++);
			tmp.coefs_list.put(coef.key(),coef);
			decodedBuf.add(tmp);
			decodedList.add(coef.getID());
			mp.freeMessage(msg);
		} 
		return 1;

		
		

	}

	public void generateEncoded() { 
		NCFastdatagram tmpEncDatagram;
		
		try{
			if (!decodedBuf.isEmpty()) {
				tmpEncDatagram=encode();
				if (tmpEncDatagram!= null && !tmpEncDatagram.isNull()) {
					synchronized(tmpEncDatagram){
						tmpEncDatagram.setIndex(PktIndex++);
						int len=tmpEncDatagram.serialize(encBuf);					
						NCodeFastMessage tm = (NCodeFastMessage)mp.getMessage(msgType);
						tm.setOutbuf(encBuf,len);
						tm.setDstNode(0);
						tm.setTTL(1);
						logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" SEND: E "+" IND: "+tmpEncDatagram.getIndex()+" #COEF: "+tmpEncDatagram.coefs_list.size()); 					
						send(tm);
						//					 	Should we free the tmpEncDatagram ! to check if send(tm) could be made synchronized ! 	  
						ncpool.free(tmpEncDatagram);
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
// 	  Should we free the tmpEncDatagram ! to check if send(tm) could be made synchronized ! 	  
//	  ncpool.freeNCdatagram(tmpEncDatagram);
	  
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
	public synchronized NCFastdatagram encode() throws GaloisException {
		int L,l;
		int choice, order,coef;
		NCFastdatagram g, gclone=ncpool.get();
		boolean flag=false;

		NCFastdatagram nc=ncpool.get();
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
				choice=rnd.nextInt(len);
				// We should insure that coef !=0
				coef= rnd.nextInt(255-1)+1;				
				if (choice > (L - 1)) {
					choice=choice-L;
					g=(NCFastdatagram)decodingBuf.elementAt(choice);
				} else {
					g=(NCFastdatagram)decodedBuf.elementAt(choice);
				}
				gclone=(NCFastdatagram) ncpool.clone(g);
				if (i>0) {					
					nc= nc.sum(gclone.product(coef),cfpool);
					ncpool.free(gclone);
				} else {
					nc=gclone.product(coef);
				}	
			}
		}
		nc.setIndex(PktIndex++);
		return nc;	
	}			
				



//--------------------------------------------------------------------	
	public synchronized NCFastdatagram reduce(NCFastdatagram g) throws GaloisException, Exception {
	
 		
		NCFastdatagram g1,g2;
		Coef_Elt coef;
		int index;
		Enumeration eg=g.coefs_list.elements();
		while (eg.hasMoreElements()) {
			coef=(Coef_Elt) eg.nextElement();
			index=decodedList.indexOf(coef.getID());
			if (index!=-1) {
				g1=(NCFastdatagram) decodedBuf.elementAt(index);
				g2=ncpool.clone(g1);
				g.minus(g2.product(coef.getCoef()),cfpool);
				ncpool.free(g2);
			}
		}
		return g;					
	}


//--------------------------------------------------------------------	
	public synchronized void update_varList(NCFastdatagram g) throws Exception {

		Coef_Elt coef;
		// first detect if new variables exists !
		Enumeration<Coef_Elt> eg=g.coefs_list.elements();
		while (eg.hasMoreElements()) {
			coef=(Coef_Elt) eg.nextElement();
			Pkt_ID id=coef.getID(); 
			if (!(decodedList.contains(id) || varList.contains(id))) {
				varList.add(id);
			}
		}
	}



//--------------------------------------------------------------------	
	public synchronized void genA() throws GaloisException, Exception {

		NCFastdatagram g;
		Pkt_ID id;
		Coef_Elt coef;		
		// Number of variables
		int N=varList.size();
		// Number of equations
		int M=decodingBuf.size();
		int index=0;

		if (M>N) {
			logger.error("Problem !");
		}
		
		int i=0;
		Enumeration<NCFastdatagram> eg=decodingBuf.elements();
		while(eg.hasMoreElements()) {
			g=(NCFastdatagram) eg.nextElement();
			Enumeration<Coef_Elt> ecoef=g.coefs_list.elements();			
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

		NCFastdatagram g,g1,g2;
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
						g=(NCFastdatagram) decodingBuf.elementAt(k);												
						for(i= k; i < N ; i++) {
							A[k][i] = GF.divide(A[k][i], pivot);
						}
						g.product(GF.divide(1, pivot));
					}
					// make the value under the pivot equal zero 
					for(i = k+1; i < M ; i++) {  // Line index
						if (A[i][k]!=0) {
							int p=A[i][k];
							for(j = k; j < N ; j++) { //Column index
								A[i][j] =  GF.minus(A[i][j], GF.product(p, A[k][j]));
							}							
							g1=(NCFastdatagram) decodingBuf.elementAt(i);
							g2=ncpool.clone((NCFastdatagram) decodingBuf.elementAt(k));
							g1.minus(g2.product(p),cfpool);
						}
					}
				}
			}
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
				NCFastdatagram TMP=decodingBuf.elementAt(lin1);
				decodingBuf.set(lin1,decodingBuf.elementAt(lin2));
				decodingBuf.set(lin2,TMP);
			}
		}


//--------------------------------------------------------------------	
	public synchronized void extractSolved(int M, int N) throws GaloisException, Exception {

		int i,j,k,l,upPivot;
		NCFastdatagram g1,g2,g3;
		Coef_Elt coef;
		Pkt_ID id;
		boolean Solved=true;
		if (M < decodingBuf.size()){
			for (i=M;i<decodingBuf.size();i++){
				decodingBuf.remove(i);
			}
		}
		//Check if one variable have been determined

		for (i=M-1;i>=0;i--) {
        	Solved=true;
        	// Prepare the NCdatagram
        	g1=(NCFastdatagram)decodingBuf.elementAt(i);
        	synchronized(g1) {
    			Enumeration ecoef=g1.coefs_list.elements();
    			while (ecoef.hasMoreElements()) {
    				coef=(Coef_Elt) ecoef.nextElement();
        			cfpool.freeCoef(coef);
    			}
    			g1.coefs_list.clear();
    			
        		id = (Pkt_ID) varList.elementAt(i);        		        		
        		coef=cfpool.getCoefElt(A[i][i], id.index_);        		        		
				coef.setnodeID_(id.getID());
        		g1.coefs_list.put(coef.key(),coef);
        		for (j=i+1;j<N;j++) {
        			if (A[i][j]!=0) {        			
        				Solved=false;
        				id = (Pkt_ID) varList.elementAt(j);
        				coef=cfpool.getCoefElt(A[i][j], id.index_);
						coef.setnodeID_(id.getID());
        				g1.coefs_list.put(coef.key(),coef);
        			}
        		}

        		if (Solved) {
        			// a variable has been solved
        			// First propagate this info in higher lines (equations)
        			for (k=i-1;k>=0;k--) {
        				// make the value up the pivot equal zero
        				upPivot=A[k][i];
        				if (upPivot!=0){
        					for(l = k+1; l < N ; l++) { //Column index
        						A[k][l] =  GF.minus(A[k][l], GF.product(upPivot, A[i][l]));
        					}
        					g2=(NCFastdatagram)decodingBuf.elementAt(k);
        					g3=(NCFastdatagram) g2.clone();
	//						for(l = 0; l < Math.max(g1.dataLength,g3.dataLength) ; l++) { //Column index    						
	//							g3.Buf[l] = int2byte(GF.minus(byte2int(g3.Buf[l]), GF.product(upPivot, byte2int(g1.Buf[l]))));
	//						}
        					g2.minus(g1.product(upPivot), cfpool);

        				}
        			}
        			//
        			//	Transfer the decoded packet to decodedbuffer;
        		
 
        			decodedBuf.add(g1);
        			if (g1.coefs_list.size() !=1) {
        				System.out.println("Error in decoded Packet !");
        			}
        			ecoef=g1.coefs_list.elements();			
        			coef=(Coef_Elt) ecoef.nextElement();
        			decodedList.add(coef.getID());        			
    
        			g1.fromIntBuf(outBuf, 0,g1.dataLength);
        			Message msg = mp.createMessage(outBuf,(short)g1.dataLength);
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
	public void decode(NCFastdatagram g) {

		// received packet validity check
		int tmpNumCoef=g.coefs_list.size();
		recv_cntr++;
		try{
			g = reduce(g);
		}
		catch(Exception ev) {
			logger.error("NetCode_Module: insert: reduce " + ev);
		}

		if(g.coefs_list.size() == 0) {
			if (simulMode) {
				logger.info("At time: "+System.currentTimeMillis()+" NODE ID: "+myNodeID+" INDEX: "+ g.getIndex()+" #COEF: "+tmpNumCoef+"ACT: Useless packet!!!");				
			} else {
				logger.info("At time: "+System.currentTimeMillis()+" NODE ID: "+myNodeID+" INDEX: "+ g.getIndex()+" #COEF: "+tmpNumCoef+"ACT: Useless packet!!!");
			}			
			return;
		}
		decodingBuf.insertElementAt(ncpool.clone(g),0);

		try{
			update_varList(g);
		}
		catch(Exception e1) {
			System.err.println("NetCode_Module: insert: update_map_list " + e1);
		}
		// M: number of equations
		int M = decodingBuf.size();
		// N: number of variables
		int N = varList.size();
		try{
			if (M>N) {
				logger.fatal("Problem !");	
			}
			genA();
			// L: column length (Number of variables+size of payload)
			//			L = N + MDU;

			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" STATUS: B "+M+" "+N);			
			M = GausElim(M, N);
			if (M>N) {
				logger.error("Problem !");	
			}
			logger.info("NID: "+myNodeID+" TIME: "+System.currentTimeMillis()%1000000+" STATUS: A "+decodingBuf.size()+" "+varList.size());
			//		decodable();
		} catch(Exception e3) {
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

	
