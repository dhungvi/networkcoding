package ch.epfl.lsr.adhoc.routing.aodv;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.*;

/**
 * This is a simple implementation of a custom made message object.
 * <p>
 * This Message is used by AODV for replying to route requests.
 * It is created through the MessageFactory class
 * <p>
 * @see RREPMessageFactory
 * @see Message
 * @author Alain Bidiville
 */
public class RREPmsg extends Message { 
    /** Contains the hop count of the RREP Message */
    private int HOP_COUNT;
    /** Contains the sequence number of the destination of the RREQ Message to which this RREP Message replies */
    private int DestOfRREQseqnb;
    /** Contains the sequence number of the source of the RREQ Message to which this RREP Message replies */
    private int SrcOfRREQseqnb;
    /** Contains the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ Message to which this RREP replies */
    private ArrayList IDandSEQNBpairs;
    
    /**
     * Creates a new instance of RREP Message.
     * <p>
     * The type of this message object is initialized at creation time and cannot
     * be changed later (for better use in a MessagePool).
     * <p>
     * @param type The type of service for this message
     */
    public RREPmsg(char type) {  
	super(type);
	IDandSEQNBpairs = new ArrayList();
    }
    
    
    /**
     * Changes the hop count of the RREP in this message object
     * <p>
     * @param HOP_COUNT The hop count of the RREP Message
     */
    public void setHOP_COUNT(int HOP_COUNT){
  	this.HOP_COUNT=HOP_COUNT;
    }
    
    /**
     * Changes the sequence number of the destination of the RREP in this message object
     * <p>
     * @param SrcOfRREQseqnb The sequence number of the destination of the RREP Message
     */
    public void setSrcOfRREQseqnb(int SrcOfRREQseqnb){
  	this.SrcOfRREQseqnb=SrcOfRREQseqnb;
    }
    
    /**
     * Changes the sequence number of the source of the RREP in this message object
     * <p>
     * @param DestOfRREQseqnb The sequence number of the source of the RREP Message
     */
    public void setDestOfRREQseqnb(int DestOfRREQseqnb){
  	this.DestOfRREQseqnb=DestOfRREQseqnb;
    }
    
    
    /**
     * Changes the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ in this message object
     * <p>
     * @param IDandSEQNBpairs The ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ Message to which this RREP replies 
     */
    public void setIDandSEQNBpairs(ArrayList IDandSEQNBpairs){
  	this.IDandSEQNBpairs=IDandSEQNBpairs;
    }
    
    /**
     * Returns the hop count of the RREP in this message object
     * <p>
     * @return The hop count of the RREP in this message object
     */
    public int getHOP_COUNT(){
  	return HOP_COUNT;
    }
    
    /**
     * Returns the sequence number of the source of the RREP in this message object
     * <p>
     * @return The sequence number of the source of the RREP in this message object
     */
    public int getDestOfRREQseqnb(){
  	return DestOfRREQseqnb;
    }
    
    /**
     * Returns the sequence number of the destination of the RREP in this message object
     * <p>
     * @return The sequence number of the destination of the RREP in this message object
     */
    public int getSrcOfRREQseqnb(){
	return SrcOfRREQseqnb;
    }
    
    
    /**
     * Returns the ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ to which this RREP replies in this message object
     * <p>
     * @return The ArrayList which contains the IDs and the sequence numbers of the Nodes which were on the path of the RREQ to which this RREP replies in this message object
     */
    public ArrayList getIDandSEQNBpairs(){
  	return IDandSEQNBpairs;
    }//getIDandSEQNBpairs
    
    /** Serialize the Data in the RREP Message */
    public void prepareData() {
	addInt(HOP_COUNT);
	addInt(DestOfRREQseqnb);
	addInt(SrcOfRREQseqnb);
	addInt(IDandSEQNBpairs.size());
	
	for (int i=0;i<IDandSEQNBpairs.size();i++){
	    IDandSEQNBpair objIDandSEQNBpair=(IDandSEQNBpair)IDandSEQNBpairs.get(i);
	    addLong(objIDandSEQNBpair.getNODE_ID());
	    addInt(objIDandSEQNBpair.getNODE_SEQNB());
    	}//for
    }
    
    /** Deserialize the Data from the RREP Message */
    public void readData() {
   	IDandSEQNBpairs.clear();
	HOP_COUNT = getInt();
	DestOfRREQseqnb = getInt();
	SrcOfRREQseqnb = getInt();
	int IDandSEQNBpairsSize = getInt();
	
	for (int i=0;i<IDandSEQNBpairsSize;i++){
	    IDandSEQNBpair objIDandSEQNBpair = new IDandSEQNBpair(getLong(), getInt());
	    IDandSEQNBpairs.add(objIDandSEQNBpair);
    	}//for
	
    }	
}
