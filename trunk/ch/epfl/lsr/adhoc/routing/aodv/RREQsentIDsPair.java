package ch.epfl.lsr.adhoc.routing.aodv;

/**
 * This class implements a pair which contains an ID of a node and an ID of a Route Request this node sent or forwarded
 * 
 * @author Alain Bidiville
 */
public class RREQsentIDsPair extends Object{
    
    /** The ID of the node which sent the Route Request Message sent or forwarded by this node */
    private long SrcNode;
    /** The ID of the Route Request Message sent or forwarded by this node */
    private long RREQ_ID;
    /** The lifetime of the pair in the ArrayList of RREQsentIDsPairs */
    private long LifeTime;
    
    /**Default constructor */
    public RREQsentIDsPair(long SrcNode, long RREQ_ID, long LifeTime){
	this.SrcNode=SrcNode;
	this.RREQ_ID=RREQ_ID;
	this.LifeTime=LifeTime;
    }
    
    /**
     * Changes the ID of the node which sent the Route Request Message sent or forwarded by this node
     * <p>
     * @param SrcNode The ID of the node which sent the Route Request Message sent or forwarded by this node
     */
    void setSrcNode(long SrcNode) {
	this.SrcNode = SrcNode;
    }
    
    
    /**
     * Changes the ID of the node which sent the Route Request Message sent or forwarded by this node
     * <p>
     * @param RREQ_ID The ID of the Route Request Message sent or forwarded by this node
     */
    void setRREQ_ID(long RREQ_ID) {
	this.RREQ_ID = RREQ_ID;
    }
    
    /**
     * Changes the lifetime of the pair in the ArrayList of RREQsentIDsPairs
     * <p>
     * @param LifeTime The lifetime of the pair in the ArrayList of RREQsentIDsPairs
     */
    void setLifeTime(long LifeTime) {
	this.LifeTime = LifeTime;
    }
    
    /**
     * Returns the ID of the node which sent the Route Request Message sent or forwarded by this node
     * <p>
     * @return SrcNode The ID of the node which sent the Route Request Message sent or forwarded by this node
     */
    long getSrcNode() {
	return SrcNode;
    }
    
    /**
     * Changes the ID of the Route Request Message sent or forwarded by this node
     * <p>
     * @return RREQ_ID The ID of the Route Request Message sent or forwarded by this node
     */
    long getRREQ_ID() {
	return RREQ_ID;
    }
    
    /**
     * Changes the lifetime of the pair in the ArrayList of RREQsentIDsPairs
     * <p>
     * @return LifeTime The lifetime of the pair in the ArrayList of RREQsentIDsPairs
     */
    long getLifeTime() {
	return LifeTime;
    }
    
}
