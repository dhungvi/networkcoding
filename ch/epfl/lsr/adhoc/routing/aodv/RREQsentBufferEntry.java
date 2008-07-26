package ch.epfl.lsr.adhoc.routing.aodv;

/**
 * This class implements a single table entry added to the RREQsent buffer for each Route Request which is being sent
 * @author Alain Bidiville
 */
class RREQsentBufferEntry {
    
    /** The Route Request Message contained in the entry */
    RREQmsg RREQ;
    /** The time at which the RREQ must be sent contained in the entry */
    long timeToWait;
    /** The validity of the entry */
    boolean VALID_ENTRY;
    /** The field contained in the entry which says if the RREQ must be sent at once */
    boolean mustBeSentAtOnce;
    /** The number of times the RREQ contained in the entry has been sent*/
    int currentRREQtries;
    /** The next time at which the Route Request Message has to be sent*/
    long nextTimeToBeSent;
    
    /** Default constructor */
    RREQsentBufferEntry(RREQmsg RREQ, long timeToWait, long nextTimeToBeSent){
	this.RREQ = RREQ;
	this.timeToWait = timeToWait;
	this.VALID_ENTRY=true;
	this.mustBeSentAtOnce=true;
	this.currentRREQtries=0;
	this.nextTimeToBeSent=nextTimeToBeSent;
	
    }
    
    /**
     * Changes the validity of the entry of the Buffer of Route Requests to send
     * <p>
     * @param VALID_ENTRY The new validity of the entry of the Buffer of Route Requests to send
     */
    void setVALID_ENTRY(boolean VALID_ENTRY) {
	this.VALID_ENTRY = VALID_ENTRY;
    }
    
    /**
     * Changes the value of the time to wait before sending the Route Request Message of the entry of the Buffer of Route Requests to send
     * This time is the one which goes from one send to the next
     * Its value doubles after each send to avoid network congestion
     * <p>
     * @param timeToWait The new time to wait before sending the Route Request Message of the entry of the Buffer of Route Requests to send
     */
    void setTimeToWait(long timeToWait) {
	this.timeToWait = timeToWait;
    }
    
    /**
     * Changes the Route Request Message of the entry of the Buffer of Route Requests to send
     * <p>
     * @param RREQ The new Route Request Message of the entry of the Buffer of Route Requests to send
     */
    void setRREQ(RREQmsg RREQ) {
	this.RREQ = RREQ;
    }
    
    /**
     * Changes boolean which says if the Route Request Message of this entry has to be sent the next time the thread SendThread sends some Route Request Messages
     * <p>
   * @param mustBeSentAtOnce The boolean which says if the Route Request Message of this entry has to be sent the next time the thread SendThread sends some Route Request Messages
   */
    void setmustBeSentAtOnce(boolean mustBeSentAtOnce) {
	this.mustBeSentAtOnce = mustBeSentAtOnce;
    }
    
    /**
     * Changes the number of times this Route Request Message has been sent until now
     * <p>
     * @param currentRREQtries The number of times this Route Request Message has been sent until now
     */
    void setcurrentRREQtries(int currentRREQtries) {
	this.currentRREQtries = currentRREQtries;
    }
    
    /**
     * Changes the next time at which the Route Request Message of the entry has to be sent 
     * <p>
     * @param nextTimeToBeSent The next time at which the Route Request Message of the entry has to be sent
     */
    void setnextTimeToBeSent(long nextTimeToBeSent) {
	this.nextTimeToBeSent = nextTimeToBeSent;
    }
    
    /**
     * Returns the validity of the entry
     * <p>
     * @return VALID_ENTRY The validity of the entry
     */
    boolean getVALID_ENTRY() {
	return VALID_ENTRY;
    }
    
    /**
     * Returns the value of the time to wait before sending the Route Request Message of the entry of the Buffer of Route Requests to send
     * <p>
     * @return timeToWait The value of the time to wait before sending the Route Request Message of the entry of the Buffer of Route Requests to send
     */
    long getTimeToWait() {
	return timeToWait;
    }
    
    /**
     * Returns the Route Request Message of the entry of the Buffer of Route Requests to send
     * <p>
     * @return timeToWait The Route Request Message of the entry of the Buffer of Route Requests to send
     */
    RREQmsg getRREQ() {
	return RREQ;
    }
    
    /**
     * Returns the value which says if the Route Request Message of the entry of the Buffer of Route Requests to send has to be sent immediately
     * <p>
     * @return mustBeSentAtOnce The value which says if the Route Request Message of the entry of the Buffer of Route Requests to send has to be sent immediately
     */
    boolean getmustBeSentAtOnce() {
	return mustBeSentAtOnce;
    }
    
    /**
     * Returns the number of times this Route Request Message has been sent until now
     * <p>
     * @return currentRREQtries The number of times this Route Request Message has been sent until now
     */
    int getcurrentRREQtries() {
	return currentRREQtries;
    }
    
    /**
     * Returns the next time at which the Route Request Message of the entry has to be sent
     * <p>
     * @return nextTimeToBeSent The next time at which the Route Request Message of the entry has to be sent
     */
    long getnextTimeToBeSent() {
	return nextTimeToBeSent;
    }
    
    
    
}
