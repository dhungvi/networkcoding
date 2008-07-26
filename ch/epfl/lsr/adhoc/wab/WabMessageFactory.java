package ch.epfl.lsr.adhoc.wab;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

public class WabMessageFactory implements IMessageFactory {
  /** Default constructor */
  public WabMessageFactory() {
  }

  public Message createMessage(char type) {
    return new WabMessage(type);
  }
}
