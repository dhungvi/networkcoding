package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
/**
 * Title:       Message Factory of the Acknowledge Type from the ReliableMultihop (Unicast) Layer.
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      EPFL - LSR
 * @author       Leiggener Alain
 * @version 1.0
 */

public class MHAckMsgFactory implements IMessageFactory{

  public MHAckMsgFactory() {
  }

    /* Creates and return a new MHAck Message */
    public Message createMessage(char type){
	return new MHAck(type);
    }
}
