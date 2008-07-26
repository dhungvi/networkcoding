package ch.epfl.lsr.adhoc.routing.aodv;

/**
 * This class implements a controller which manages the node's own AODV sequence number and the node's own ID for the Route Request Messages it sends
 * @author Alain Bidiville
 */
class SeqNumberAndRREQIDsController {
    
    /** The node's own AODV sequence number */
    int Seqnb;
    /** The node's own ID for the Route Request Messages it sends */
    int RREQ_ID;
    
    /** Default constructor */
    public SeqNumberAndRREQIDsController(){
	Seqnb=1;
	RREQ_ID=0;
    }
    
    /**
     * Changes the node's own AODV sequence number
     * <p>
     * @param Seqnb The new node's own AODV sequence number
     */
    public synchronized void setSeqnb(int Seqnb) {
	this.Seqnb=(Seqnb)%Integer.MAX_VALUE;
    }
    
    /**
     * Returns the node's own AODV sequence number
     * <p>
     * @return Seqnb The node's own AODV sequence number
     */
    public synchronized int getSeqnb(){
	return Seqnb;
    }
    
    /**Increments by one the node's own ID for the Route Request Messages it sends
     *The ID is set to one when it goes higher than the maximum integer value existing */
    public synchronized void incrementRREQ_ID() {
	RREQ_ID=(RREQ_ID+1)%Integer.MAX_VALUE;
    }	
    
    /**
     * Returns the node's own ID for the Route Request Messages it sends
     * <p>
     * @return Seqnb The node's own ID for the Route Request Messages it sends
     */
    public synchronized int getRREQ_ID() {
	return RREQ_ID;
    }		
}
