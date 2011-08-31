package ch.epfl.lsr.adhoc.routing.ncode.feedback;

//import Coef_Elt;

import java.util.Vector;
import java.net.*;

import ch.epfl.lsr.adhoc.routing.ncode.*;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is a simple implementation of network coded packet.
 * <p>
 * This Message is a very simple implementation of Message, which allows to
 * encapsulate another message in a network coded message. It is created 
 * through the NCodeFactory class (also a simple test class).
 * <p>
 * @see NCodeFactory
 * @see Message
 * @author Kavé Salamatian
 * @version 1.0
 */
public class NCodeFBMessage extends NCodeMessage {	
  /**
   * Creates a new instance of NFBCodeMessage.
   * <p>
   * The type of this message object is initialized at creation time and cannot
   * be changed later (for better use in a MessagePool).
   * <p>
   * @param type The type of service for this message
   */
  public NCodeFBMessage(char type) {
    super(type);
  }

   public void prepareData() {
		  addBytes(outbuf,outLength);
   }


  /**
   * Read the text (for this message) from the buffer with the getString() method.
   */
  public void readData() {
	  
	  inLength=getBytes(inbuf);    
          
  }
	  
  
  /**
   * Changes the textual message in this message object.
   * <p>
   * @param text The new textual message
   */
 
}
