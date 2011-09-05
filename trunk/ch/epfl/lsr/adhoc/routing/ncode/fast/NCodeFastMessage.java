package ch.epfl.lsr.adhoc.routing.ncode.fast;

//import Coef_Elt;

import java.util.Vector;
import java.net.*;

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
public class NCodeFastMessage extends Message {
  /** Encapsulate the actual message */
	byte[] inbuf=new byte[Globals.MaxBufLength];
	int inLength=0;
	byte[] outbuf=new byte[Globals.MaxBufLength];;
	int outLength=0;
//	private NCdatagram payload;
//	private NCdatagram rcvNCdatagram;
//	private GaloisField base;
//	private ExtendedGaloisField GF;

  /**
   * Creates a new instance of NCodeMessage.
   * <p>
   * The type of this message object is initialized at creation time and cannot
   * be changed later (for better use in a MessagePool).
   * <p>
   * @param type The type of service for this message
   */
  public NCodeFastMessage(char type) {
    super(type);
//    rcvNCdatagram=new NCdatagram(0);
  }

  /**
   * Write the text (for this message) with the addString() method to the
   * buffer (for sending the message on the network).
   */
  public synchronized void prepareData() {
	  addBytes(outbuf,outLength);
	  if ((data[54]==98) && (data[55] > 5) ){
		  if ((data[161]!=4)) {
	    	int i=0;
		  }
	  }

//	  int s ;
//	  Coef_Elt coef_elt;
////	  int sum=0;
//	  
////	  for (int i=0; i < payload.coefs_list.size();i++ ){
////		  sum=GF.sum(((Coef_Elt)payload.coefs_list.elementAt(i)).coef_,sum);
////	  }
////	  sum=GF.product(sum,32);
////	  try {
////		  if (GF.divide(sum,byte2int(payload.Buf[1]))!=1 && sum !=0) {
////			  System.out.println("CHECK Prepare Data");
////		  }
////	  } catch (GaloisException e4) {
////			// TODO Auto-generated catch block
////		  e4.printStackTrace();
////	  }
//	  synchronized(payload){
//		  payload.validate();
//		  s = payload.coefs_list.size();
//		  // Adding the Packet Index
//		  addInt(payload.getIndex());
//		  /** Adding Coefs_list size **/
//		  addByte(int2byte(s));
//		  /** Adding Coefs_list**/
//		  for (int i=0; i < s; i++) {
//			  coef_elt = (Coef_Elt) payload.coefs_list.elementAt(i);			
//			  addByte(int2byte(coef_elt.coef_));
//			  addInt(coef_elt.id_.index_);
//			  addInt(coef_elt.length);
//			  addBytes(coef_elt.id_.saddr.getAddress(),4);
//		  }
//	  	/** Adding Payload data **/	  
//		 addBytes(payload.Buf,payload.dataLength);
//	  }
  }

  /**
   * Read the text (for this message) from the buffer with the getString() method.
   */
  public synchronized void readData() {
//	  if ((data[55] > 5) && (data[161]!=4)){
//	    	int i=0;
//	  }
	  inLength=getBytes(inbuf);

//	  byte coef_;
//	  byte[] add=new byte[4];  
//	  
//	  Coef_Elt coef_elt;
//	  // Getting PktIndex
//	  int index;
//	  /** Getting Coefs_list size **/
//	  byte s;
//	  
////	  byte[] backdata=new byte[1000];	  
////	  System.arraycopy(this.data,0,backdata,0,1000);
//	  synchronized(rcvNCdatagram) {
//		  index=getInt();;
//		  rcvNCdatagram.setIndex(index);
//		  s=getByte();
//	//	  Vector coefs_list= new Vector(s);
//		  /** Adding Coefs_list**/
//		  int CC=0;
//		  for (int i=0; i < s; i++) {
//			  coef_=getByte();
//			  CC=byte2int(coef_);
//			  if (i >= rcvNCdatagram.coefs_list.size()) {
//				  coef_elt=new Coef_Elt(CC,0);
//				  rcvNCdatagram.coefs_list.add(coef_elt);
//			  }
//			  coef_elt=(Coef_Elt)rcvNCdatagram.coefs_list.elementAt(i);
//			  coef_elt.id_.index_=getInt();
//			  coef_elt.length=getInt();
//			  try {
//				  int l=getBytes(add);
//				  coef_elt.id_.saddr=InetAddress.getByAddress(add);
//			  } catch (UnknownHostException e) {
//				  e.printStackTrace();
//			  }
//		  }
//		  rcvNCdatagram.coefs_list.setSize(s);
//		  /** getting Payload data **/	  
//		  rcvNCdatagram.dataLength=getBytes(rcvNCdatagram.Buf);
//		  rcvNCdatagram.validate();
//		  CC=0;
//	  }
  }

  /**
   * Changes the textual message in this message object.
   * <p>
   * @param text The new textual message
   */
  public synchronized void setOutbuf(byte[] buf, int bufLength) {
//    this.payload = (NCdatagram) Payload.clone();
	  outbuf=buf;
	  outLength=bufLength; 
  }

  /**
   * Returns the textual message contained whithin this message object.
   * <p>
   * @return The textual message in this message object
   */
  public synchronized int getInBuf(byte[] buf) {
//    return rcvNCdatagram;
	  System.arraycopy(inbuf,0,buf,0,inLength);
	  return inLength;
  }

  /**
   * Overwrites Object.toString().
   * <p>
   * This method allows this message object to be printed in a statement such
   * as System.out.println().
   * <p>
   * @return A String representation of this object (the text containted in this message)
   */
	public byte int2byte(int val) {
		return (byte) val;
	}

	public int byte2int(byte val){
		int i=val;
		if (i <0 ) {
			i=i+256;
			return i;
		} else {
			return i;
		}
	}
}
