package ch.epfl.lsr.adhoc.wab;

import ch.epfl.lsr.adhoc.runtime.Message;

public class WabMessage extends Message {

  public WabMessage(char type) {
    super(type);
  }
  
  public void readData() {   
  }


  public void prepareData() {
    addString("Blabla");
  }
}
