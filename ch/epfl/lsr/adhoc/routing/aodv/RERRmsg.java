package ch.epfl.lsr.adhoc.routing.aodv;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.*;

/**
 * This is a simple implementation of a custom made message object.
 * <p>
 * This Message is used by AODV for notifying other Nodes of a link break.
 * It is created through the MessageFactory class
 * <p>
 * @see RERRMessageFactory
 * @see Message
 * @author Alain Bidiville
 */
public class RERRmsg extends Message {  
    
    /** Contains the pairs of IDs and sequence numbers of the Nodes which are unreachable due to the link break */
    private ArrayList Unreachable_Destinations_IDSandSEQNBSpairs;
    
    /**
     * Creates a new instance of RERR Message.
     * <p>
     * The type of this message object is initialized at creation time and cannot
     * be changed later (for better use in a MessagePool).
     * <p>
     * @param type The type of service for this message
     */
    public RERRmsg(char type) {  
	super(type);
	Unreachable_Destinations_IDSandSEQNBSpairs = new ArrayList();
    }
    
    /**
     * Changes the pairs of IDs and sequence numbers of the Nodes which are unreachable due to the link break of the RERR in this message object
     * <p>
     * @param Unreachable_Destinations_IDSandSEQNBSpairs The pairs of IDs and sequence numbers of the Nodes which are unreachable due to the link break of the RERR Message
     */
    public void setUnreachable_Destinations_IDSandSEQNBSpairs(ArrayList Unreachable_Destinations_IDSandSEQNBSpairs){
	this.Unreachable_Destinations_IDSandSEQNBSpairs=Unreachable_Destinations_IDSandSEQNBSpairs;
    }
    
    
    /**
     * Returns the pairs of IDs and sequence numbers of the Nodes which are unreachable due to the link break
     * <p>
     * @return The pairs of IDs and sequence numbers of the Nodes which are unreachable due to the link break
     */
    public ArrayList getUnreachable_Destinations_IDSandSEQNBSpairs(){
	return Unreachable_Destinations_IDSandSEQNBSpairs;
    }
    
    /** Serialize the Data in the RERR Message */
    public void prepareData() {
	addInt(Unreachable_Destinations_IDSandSEQNBSpairs.size());
	
	for (int i=0;i<Unreachable_Destinations_IDSandSEQNBSpairs.size();i++){
	    IDandSEQNBpair objIDandSEQNBpair=(IDandSEQNBpair)Unreachable_Destinations_IDSandSEQNBSpairs.get(i);
	    addLong(objIDandSEQNBpair.getNODE_ID());
	    addInt(objIDandSEQNBpair.getNODE_SEQNB());
    	}
    }
    
    /** Deserialize the Data from the RERR Message */
    public void readData() {
	Unreachable_Destinations_IDSandSEQNBSpairs.clear();
	int IDandSEQNBpairsSize = getInt();
	//System.out.println(IDandSEQNBpairsSize+" : Size of objIDandSEQNBpair in readdata in RERRmsgFactory");
	for (int i=0;i<IDandSEQNBpairsSize;i++){
	    long nodeID=getLong();
	    int nodeSEQNB=getInt();
	    IDandSEQNBpair objIDandSEQNBpair = new IDandSEQNBpair(nodeID, nodeSEQNB);
	    //System.out.println("node ÎD and SEQNB of the pair nb: "+i+" : "+objIDandSEQNBpair.getNODE_ID()+" , " +objIDandSEQNBpair.getNODE_SEQNB());
	    Unreachable_Destinations_IDSandSEQNBSpairs.add(objIDandSEQNBpair);
    	}
    }
}
