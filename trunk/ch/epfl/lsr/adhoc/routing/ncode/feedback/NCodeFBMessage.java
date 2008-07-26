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
public class NCodeFBMessage extends Message {
  /** Encapsulate the actual message */
	private FeedbackData Fbdata;
	private int length;
	private long gen_;
//	private int reduced_coef_list;
	private boolean decoded;
	private boolean list_extract; // to note wheither the coef_list is extracted from the received payload
	private int MDU;
  /**
   * Creates a new instance of NCodeMessage.
   * <p>
   * The type of this message object is initialized at creation time and cannot
   * be changed later (for better use in a MessagePool).
   * <p>
   * @param type The type of service for this message
   */
  public NCodeFBMessage(char type) {
    super(type);
  }

  /**
   * Write the text (for this message) with the addString() method to the
   * buffer (for sending the message on the network).
   */
  public void prepareData() {
	  int s=Fbdata.decoded_list.size();
	  Pkt_ID id;
	  /** Adding Number of variable and Number of Equation **/
	  addInt(Fbdata.NumbVar);
	  addInt(Fbdata.NumbEq);
	  /** Adding list size **/
	  addInt(s);
	  /** Adding Coefs_list**/
	  for (int i=0; i < s; i++) {
		  	id= (Pkt_ID) Fbdata.decoded_list.elementAt(i);			
		  	addInt(id.index_);
//			addBytes(id.saddr.getAddress());
		}
  }

  /**
   * Read the text (for this message) from the buffer with the getString() method.
   */
  public void readData() {
	  byte[] add=new byte[4];  
	  Fbdata =new FeedbackData();

	  // A corriger !!!
//	  Pkt_ID id=new Pkt_ID(0);
	  Fbdata.NumbVar=getInt();
	  Fbdata.NumbEq=getInt();
	  /** Getting Coefs_list size **/
	  int  s=getInt();
	  Fbdata.decoded_list= new Vector(s);
	  /** Adding Coefs_list**/
	  for (int i=0; i < s; i++) {
//		  id.index_=getInt();
//		  try {
			int l=getBytes(add);
//			id.saddr=InetAddress.getByAddress(add);
//		  } catch (UnknownHostException e) {
//				e.printStackTrace();
//		  }
//		 Fbdata.decoded_list.add(id);
	  }
  }

  /**
   * Changes the textual message in this message object.
   * <p>
   * @param text The new textual message
   */
  public void setFbdata(FeedbackData fbdata) {
    this.Fbdata = fbdata;
  }

  /**
   * Returns the textual message contained whithin this message object.
   * <p>
   * @return The textual message in this message object
   */
  public FeedbackData getFbdata() {
    return Fbdata;
  }

}
