package ch.epfl.lsr.adhoc.routing.aodv;

/**
 * This class implements a pair which contains an ID of a node and his sequence number
 * 
 * @author Alain Bidiville
 */
public class IDandSEQNBpair extends Object {//IDandSEQNBpair class
    /** The sequence number of the node */
    int NODE_SEQNB;
    /** The ID of the node */
    long NODE_ID;
    
    /** Default constructor */
    public IDandSEQNBpair(long NODE_ID, int NODE_SEQNB){
	this.NODE_ID=NODE_ID;
	this.NODE_SEQNB=NODE_SEQNB;
    }
    
    /**
     * Changes the ID of the node of the IDandSEQNBpair
     * <p>
     * @param NODE_ID The new ID of the node of the IDandSEQNBpair
     */
    void setNODE_ID(long NODE_ID) {
	this.NODE_ID = NODE_ID;
    }
    
    /**
     * Changes the sequence number of the node of the IDandSEQNBpair
     * <p>
     * @param NODE_SEQNB The new sequence number of the node of the IDandSEQNBpair
     */
    void setNODE_SEQNB(int NODE_SEQNB) {
	this.NODE_SEQNB = NODE_SEQNB;
    }
    
    /** Returns the ID of the node of the IDandSEQNBpair 
     *<p>
     * @return The ID of the node of the IDandSEQNBpair  */
    
    long getNODE_ID() {
	return NODE_ID;
    }
    
    /** Returns the sequence number of the node of the IDandSEQNBpair 
     *<p>
     * @return The sequence number of the node of the IDandSEQNBpair */
    int getNODE_SEQNB() {
	return NODE_SEQNB;
    }
    
}
