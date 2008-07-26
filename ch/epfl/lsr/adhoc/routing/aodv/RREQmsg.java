package ch.epfl.lsr.adhoc.routing.aodv;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.*;

/**
 * This is a simple implementation of a custom made message object.
 * <p>
 * This Message is used by AODV for requesting paths to nodes for which it has none.
 * It is created through the MessageFactory class
 * <p>
 * @see RREQMessageFactory
 * @see Message
 * @author Alain Bidiville
 */
public class RREQmsg extends Message {  // Here srcNode and dstNode don't need to be used because 
    // everything is already done in Message.
    /** Contains the ID of the RREQ Message */
    private int RREQ_ID;
    /** Contains the hop count of the RREQ Message */
    private int HOP_COUNT;
    /** Contains the sequence number of the destination of the RREQ Message */
    private int DestOfRREQseqnb;
    /** Contains the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ */
    private ArrayList IDandSEQNBpairs;
    /** Contains the sequence number of the source of the RREQ Message */
    private int SrcOfRREQseqnb;
    
    /**
     * Creates a new instance of RREQ Message.
     * <p>
     * The type of this message object is initialized at creation time and cannot
     * be changed later (for better use in a MessagePool).
     * <p>
     * @param type The type of service for this message
     */
    public RREQmsg(char type) {  
	super(type);
	IDandSEQNBpairs = new ArrayList();
    }
    
    /**
     * Changes the ID of the RREQ in this message object
     * <p>
     * @param RREQ_ID The ID of the RREQ Message
     */
    public void setRREQ_ID(int RREQ_ID){
  	this.RREQ_ID=RREQ_ID;
    }
    
    /**
     * Changes the hop count of the RREQ in this message object
     * <p>
     * @param HOP_COUNT The hop count of the RREQ Message
     */
    public void setHOP_COUNT(int HOP_COUNT){
  	this.HOP_COUNT=HOP_COUNT;
    }
    
    /**
     * Changes the sequence number of the destination of the RREQ in this message object
     * <p>
     * @param DestOfRREQseqnb The sequence number of the destination of the RREQ Message
     */
    public void setDestOfRREQseqnb(int DestOfRREQseqnb){
  	this.DestOfRREQseqnb=DestOfRREQseqnb;
    }
    
    /**
     * Changes the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ in this message object
     * <p>
     * @param IDandSEQNBpairs The ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ
     */
    public void setIDandSEQNBpairs(ArrayList IDandSEQNBpairs){
  	this.IDandSEQNBpairs=IDandSEQNBpairs;
    }
    
    /**
     * Changes the sequence number of the source of the RREQ in this message object
     * <p>
     * @param SrcOfRREQseqnb The sequence number of the source of the RREQ Message
     */
  	public void setSrcOfRREQseqnb(int SrcOfRREQseqnb){
	    this.SrcOfRREQseqnb=SrcOfRREQseqnb;
  	}
    
    
    /**
   * Returns the ID of the RREQ in this message object
   * <p>
   * @return The ID of the RREQ in this message object
   */
    public int getRREQ_ID(){
  	return RREQ_ID;
    }
    
    /**
     * Returns the hop count of the RREQ in this message object
     * <p>
     * @return The hop count of the RREQ in this message object
     */
    public int getHOP_COUNT(){
  	return HOP_COUNT;
    }
    
    /**
     * Returns the sequence number of the destination of the RREQ in this message object
     * <p>
     * @return The sequence number of the destination of the RREQ in this message object
     */
    public int getDestOfRREQseqnb(){
  	return DestOfRREQseqnb;
    } 
    
    /**
     * Returns the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ in this message object
     * <p>
     * @return The ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ in this message object
     */
    public ArrayList getIDandSEQNBpairs(){
  	return IDandSEQNBpairs;
    }
    
    /**
     * Returns the sequence number of the source of the RREQ in this message object
     * <p>
     * @return The sequence number of the source of the RREQ in this message object
     */
    public int getSrcOfRREQseqnb(){
  	return SrcOfRREQseqnb;
    }
    
    
    /** Serialize the Data in the RREQ Message */
    public void prepareData() {
	
	addInt(RREQ_ID); 
	addInt(HOP_COUNT);
	addInt(DestOfRREQseqnb);
	addInt(IDandSEQNBpairs.size());
	
	for (int i=0;i<IDandSEQNBpairs.size();i++){
	    IDandSEQNBpair objIDandSEQNBpair=(IDandSEQNBpair)IDandSEQNBpairs.get(i);
	    addLong(objIDandSEQNBpair.getNODE_ID());
	    addInt(objIDandSEQNBpair.getNODE_SEQNB());
    	}
	addInt(SrcOfRREQseqnb);
	
	
    }
    /** Deserialize the Data from the RREQ Message */
    public void readData() {
   	IDandSEQNBpairs.clear();
	RREQ_ID = getInt();
	HOP_COUNT = getInt();
	DestOfRREQseqnb = getInt();
	int IDandSEQNBpairsSize = getInt();
	
	for (int i=0;i<IDandSEQNBpairsSize;i++){
	    IDandSEQNBpair objIDandSEQNBpair = new IDandSEQNBpair(getLong(), getInt());
	    IDandSEQNBpairs.add(objIDandSEQNBpair);
    	}
	SrcOfRREQseqnb = getInt();
	
    }
}
