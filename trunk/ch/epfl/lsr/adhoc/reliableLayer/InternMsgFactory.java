package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * Title:        Message Factory of the Intern messagetype.
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      EPFL- LSR
 * @author       Leiggener Alain
 * @version 1.0
 */

public class InternMsgFactory implements IMessageFactory {

  public InternMsgFactory() {
  }

  /* Creates and return a new InternMsg Message */
  public Message createMessage(char type){
	return new InternMsg(type);
   }
}
