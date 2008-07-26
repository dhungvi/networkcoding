package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.Vector;
import java.util.ArrayList;

/** Class that is needed by the ReliableLayer. Each message sent and having not yet been acknowledged has his
 *  appropriatae Destinations object. The timeout and a copy of the message sent will be stored here, as weel
 *  as the neighbours that have not yet sent an acknowledgment.
 */
class Destinations extends ArrayList{
    /** System time when the msg have to be resent . Used to control the timeout */
    private long timeout;
    /** A copy of the Message sent.*/
    private Message msg;
    /** Number of times the message has already been resent. */
    private int nbr_resend;

    /**
     * Public constructor.
     */
    public Destinations(long time, Message msg){
	super();
	this.timeout=time;
	this.msg=msg;
	this.nbr_resend=0;
    }

    /** Adds one more Destination to the destTable. This method is only called by the first time a message is sent.*/
    public void addDest(long dst){
	Long dstObj=new Long(dst);
	this.add(dstObj);
    }
  /**
   * Set the next timeout for the message.
   */
    public void setTime(long timeout){
      this.timeout=timeout;
    }

    /**
     * @return The timeout for the message.
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

    /** Number of times the message has already been resent
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

    /**
     *  Delete the Entry that corresponds to the Acknowledgement arrived at this node. */
    public void updateDest(long srcAck){
	Long src=new Long(srcAck);
	remove(src);
    }
}
