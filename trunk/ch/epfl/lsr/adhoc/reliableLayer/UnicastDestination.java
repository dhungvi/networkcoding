package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;

/** Class that is needed by the ReliableMultihop layer. Each Unicast message sent and having not yet been acknowledged has his
 *  appropriatae UnicastDestination object. The timeout and a copy of the message sent will be stored here, as well
 *  as the destination node of the message sent.
 */

class UnicastDestination{
    /** System Time at which the timeout expires.*/
    private long timeout;
    /** A copy of the message sent.*/
    private Message msg;
    /** Number of times the message has already been sent. */
    private int nbr_resend;
    /** The Destination of the message.*/
    private long dst;
    /**Size of awaited acknowledgments:  0 = no entry (message ack), 1 = message has not yet been acknowledged */
    private int size;

    /** Public constructor */
    public UnicastDestination(long time, Message msg){
	this.timeout=time;
	this.msg=msg;
	this.nbr_resend=0;
        this.size=0;
    }

    /** Add the Destination. */
    public void setDest(long dst){
	this.dst=dst;
	this.size=1;
    }

    public void setTime(long timeout){
      this.timeout=timeout;
    }
  /**
   * @return Time at which the message should be resent the next time.
   */
    public long getTime(){
	return timeout;
    }

    /**
     * @return The reference of the message.
     */
    public Message getMessage(){
	return msg;
    }

    /** Number of times the message has already been sent
     *<p>
     * @return Integer
     */
    public int getNbr_Resend(){
	return nbr_resend;
    }

    /** Increment the Number of times a message has already been sent.
     *
     */
    public void incrementNbr_Resend(){
	nbr_resend++;
    }

    /** The message was acknowledged. Thus the size of awaited acknowledges is set to 0. */
    public void updateDest(long srcAck){
	if(this.dst==srcAck){
          this.size=0;
        }
    }

    public long getDst(){
      return this.dst;
    }

    /**
     * @return 0: The acknowledgment from the destination has already been received. 1: There is still
     * a acknowleddment awaited.
     */
    public int size(){
      return this.size;
    }
}
