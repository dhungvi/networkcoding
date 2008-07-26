package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;
/**
 * Title:        Interne message.
 * Description:  It serves as inter-communication message between the ReliableMultihop and the ReliableBroadcast Layer.
 *               It should never leave the node, where it was created. The ReliableBroadcast Layer has to inform the
 *               above placed ReliableMultihopLayer about the RelSeqNbr that he has set to the messages.
 *
 * Copyright:    Copyright (c) 2003
 * Company:      EPFL- LSR
 * @author       Leiggener Alain
 * @version 1.0
 */

public class InternMsg extends Message {
  private int oldSeqNbr;
  private int newSeqNbr;

  /**
   * Public constructor.
   */
  public InternMsg(char type) {
      super(type);
  }

    /** Serializes the Data in the Message */
    public void prepareData(){
	addInt(oldSeqNbr);
	addInt(newSeqNbr);
    }

    /* Deserializes the Data from the Message */
    public void readData(){
	this.oldSeqNbr=getInt();
	this.newSeqNbr=getInt();
    }

    public void setOldRelSeqNbr(int old){
        this.oldSeqNbr=old;
    }
    public void setNewRelSeqNbr(int neu){
        this.newSeqNbr=neu;
    }
    public int getOldRelSeqNbr(){
        return this.oldSeqNbr;
    }
    public int getNewRelSeqNbr(){
        return this.newSeqNbr;
    }

}
