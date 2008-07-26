package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * Title:        Multi-hop Acknowledge
 * Description:  It's used by the ReliableMultihop Layer. It corresponds to the Ack Message used by the ReliableLayer.
 * Copyright:    Copyright (c) 2003
 * Company:      EPFL - LSR
 * @author       Leiggener Alain
 * @version 1.0
 */

public class MHAck extends Message{

  /** The Sequence Number of the message to acknowledge  (it's not suffisant for identifier)*/
    private int seqNbrToAck;
    /** The Source Node of the message to Ack */
    private long oldSource;

  public MHAck(char type){
      super(type);
  }

    /** Serialize the Data in the Ack Message */

    public void prepareData(){
	addInt(seqNbrToAck);
	addLong(oldSource);
    }

    /** Deserialize de Data from the Ack Message */
    public void readData(){
	this.seqNbrToAck=getInt();
	this.oldSource=getLong();
    }

    public void setSeqNbrToAck(int seqNbr){
	this.seqNbrToAck=seqNbr;
    }
    public int getSeqNbrToAck(){
	return seqNbrToAck;
    }
    /** Set the source Node */
    public void setOldSource(long oldSource){
	this.oldSource=oldSource;
    }
    public long getOldSource(){
	return oldSource;
    }

    /** Reset the Data when the message is recycled */
    public void reset(){
	this.seqNbrToAck=-1;
    }
}
