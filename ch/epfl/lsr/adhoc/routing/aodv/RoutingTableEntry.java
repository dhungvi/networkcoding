package ch.epfl.lsr.adhoc.routing.aodv;

/**
 * This class implements an entry of the Routing Table.
 * <p>
 * It has the following informations on a destination:
 * <p>
 * <li><b> ID_DEST </b> - The ID of the destination node </li>
 * <li><b> DEST_SEQNB </b> - The last known sequence number of the destination node </li>
 * <li><b> LIFETIME_ACTIVE_ROUTE </b> - The lifetime of this route </li>
 * <li><b> nextHop </b> - The next hop towards the destination </li>
 * <li><b> HOP_COUNT </b> - The hop count to this destination </li> 
 * <li><b> VALID_ENTRY </b> - The validity of the route </li> 
 * <p>
 * @author Alain Bidiville
 */
public class RoutingTableEntry extends Object {
    
    /** The destination ID contained in the entry */
    private long ID_DEST;
    /** The destination sequence number contained in the entry */	
    private int DEST_SEQNB;
    /** The lifetime of the entry */
    private long LIFETIME_ACTIVE_ROUTE;
    /** The next hop ID contained in the entry */
    private long nextHop;
    /** The hop count to the destination contained in the entry */
    private int HOP_COUNT;
    /** The validity of the entry */
    private boolean VALID_ENTRY;
    
    /** Default constructor */
    public RoutingTableEntry(long ID_DEST, int DEST_SEQNB, long LIFETIME_ACTIVE_ROUTE, long nextHop, int HOP_COUNT, boolean VALID_ENTRY){
	this.ID_DEST=ID_DEST;
	this.DEST_SEQNB=DEST_SEQNB;
	this.LIFETIME_ACTIVE_ROUTE=LIFETIME_ACTIVE_ROUTE;
	this.nextHop=nextHop;
	this.HOP_COUNT=HOP_COUNT;
	this.VALID_ENTRY=VALID_ENTRY;
	
    }
    /**
     * Changes the ID of the destination node of the entry
     * <p>
     * @param ID_DEST The ID of the new destination of the route
     */
    void setID_DEST(long ID_DEST) {
	this.ID_DEST = ID_DEST;
    }
    /**
     * Changes the sequence number of the destination node of the entry
     * <p>
     * @param DEST_SEQNB The new DEST_SEQNB of the destination node of the route
     */
    void setDEST_SEQNB(int DEST_SEQNB) {
	this.DEST_SEQNB = DEST_SEQNB;
    }
    
    /**
     * Changes the lifetime of the route
     * <p>
     * @param LIFETIME_ACTIVE_ROUTE The new lifetime of the route
     */
    void setLIFETIME_ACTIVE_ROUTE(long LIFETIME_ACTIVE_ROUTE) {
	this.LIFETIME_ACTIVE_ROUTE = LIFETIME_ACTIVE_ROUTE;
    }
    /**
     * Changes the next hop of the route
     * <p>
     * @param nextHop The new next hop of the route
     */
    void setnextHop(long nextHop) {
	this.nextHop = nextHop;
    }
    /**
     * Changes the hop count to the destination of the route
     * <p>
     * @param HOP_COUNT The new hop count to the destination of the route
     */
    void setHOP_COUNT(int HOP_COUNT) {
	this.HOP_COUNT = HOP_COUNT;
    }
			/**
			 * Changes the validity of the route
			 * <p>
			 * @param VALID_ENTRY The new validity of the route
			 */
    void setVALID_ENTRY(boolean VALID_ENTRY) {
	this.VALID_ENTRY = VALID_ENTRY;
    }
    
    /**
     * Returns the ID of the destination node of the route
     * <p>
     * @return ID_DEST The ID of the destination node of the route
     */
    long getID_DEST() {
	return ID_DEST;
    }
    /**
     * Returns the sequence number of the destination node of the route
     * <p>
     * @return DEST_SEQNB The sequence number of the destination node of the route
     */
    int getDEST_SEQNB() {
	return DEST_SEQNB;
    }
    /**
     * Returns the lifetime of the route
     * <p>
     * @return LIFETIME_ACTIVE_ROUTE The lifetime of the route
     */
    long getLIFETIME_ACTIVE_ROUTE() {
	return LIFETIME_ACTIVE_ROUTE;
    }
    /**
     * Returns the next hop towards the destination of the route
     * <p>
     * @return nextHop The next hop towards the destination of the route
     */
    long getnextHop() {
	return nextHop;
    }
    /**
     * Returns the hop count to the destination of the route
     * <p>
     * @return HOP_COUNT The hop count to the destination of the route
     */
    int getHOP_COUNT() {
	return HOP_COUNT;
    }
    /**
     * Returns the validity of the route
     * <p>
     * @return VALID_ENTRY The validity of the route
     */
    boolean getVALID_ENTRY() {
	return VALID_ENTRY;
    }
    
    
}
