package ch.epfl.lsr.adhoc.tools;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

public class DebugMessageFactory implements IMessageFactory {

  public DebugMessageFactory() {
  }

  public Message createMessage(char type) {
    return new DebugMessage(type);
  }
}
