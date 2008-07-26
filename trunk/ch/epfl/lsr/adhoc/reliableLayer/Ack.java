package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * Message of type Ack (Acknowledgment), that is needed by the ReliableLayer.
 *
 * @ver June 2003
 * @author Leiggener Alain
 */
public class Ack extends Message {
    /** The Reliable Sequence Number from the message to acknowledge*/
    private int seqNbrToAck;
    /** The Source Node from the message to Ack */
    private long oldSource;

    /** Construct of the Ack Message.*/
    public Ack(char type) {
	super(type);
    }

    /** Serializes the body Data in the Ack Message */
    public void prepareData(){
	addInt(seqNbrToAck);
	addLong(oldSource);
    }

    /** Deserializes the body Data from the Ack Message */
    public void readData(){
	this.seqNbrToAck=getInt();
	this.oldSource=getLong();
    }
    /** Set the field that indicates the Reliable Sequence Number to acknowledge. */
    public void setSeqNbrToAck(int seqNbr){
	this.seqNbrToAck=seqNbr;
    }
    /**
     * @return The Reliable Sequence Number that is acknowledged by this message.
     */
    public int getSeqNbrToAck(){
	return seqNbrToAck;
    }
    /** Set the source node of the message to acknowledge.*/
    public void setOldSource(long oldSource){
	this.oldSource=oldSource;
    }

    /**
     * @return The Source that has sent the original message to acknowledge by this ack.
     */
    public long getOldSource(){
	return oldSource;
    }

    /** Reset the Data if the message is recycled */
    public void reset(){
	this.seqNbrToAck=-1;
    }
}
