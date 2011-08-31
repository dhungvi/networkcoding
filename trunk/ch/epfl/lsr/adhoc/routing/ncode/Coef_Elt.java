package ch.epfl.lsr.adhoc.routing.ncode;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Class Coef_Elt
 * 
 */
public class Coef_Elt {
  // Fields
  // 
  public int coef_;
  public Integer index_;
//  public InetAddress saddr;
  public Long nodeID_;
  public int length;
  //public final static int coef_size = 13;


  // Methods
  // Constructors
  // Empty Constructor
  public Coef_Elt (int coef,int index) {
	  coef_=coef;
	  index_=index;
	  nodeID_=NCGlobals.nodeID_;
//	  saddr=Globals.localAdd;
  }
  // Accessor Methods
  /**
   * Get the value of coef_
   * 
   * @return the value of coef_
   */
  public int getCoef(  ) {
    return coef_;
  }
  /**
   * Set the value of coef_
   * 
   * 
   */
  public void setCoef( int value  ) {
    coef_ = value;
  }

  /**
   * Set the value of id_
   * 
   * 
   */

  public int getIndex(  ) {
	    return index_;
	  }

  public void setIndex( int value  ) {
    index_ = value;
  }

  /**
   * Get the value of saddr
   * 
   * @return the value of saddr
   */

  //public InetAddress getSaddr (  ) {
  //  return saddr;
  //}
  public long getnodeID( ){
	  return nodeID_;
  }
  
  /**
   * Set the value of saddr
   * 
   * 
   */
//  public void setSaddr (InetAddress add) {
//    saddr = add;
//  }
  public void setnodeID_(long nodeID) {
	  nodeID_=nodeID;
  }


		
//	public void updateID( int value  ) {
//		index_ = value;
//		try{
//			String str=saddr.getHostAddress();			
//			saddr = InetAddress.getByName(str);
//		}		
//		catch(UnknownHostException e) {
//			System.err.println(e);
//		}
//	}
  
 
  
//	public Coef_Elt clone() {
  	public Object clone() {
  		Coef_Elt coef_elt = new Coef_Elt(coef_,index_);
  		coef_elt.length=length;
//		try{
//			String str=saddr.getHostAddress();
//			saddr = InetAddress.getByName(str);
//		}
//		catch(UnknownHostException e) {
//			System.err.println(e);
//			return null;
//		}
  		coef_elt.nodeID_=nodeID_;
		return coef_elt;
	}
  	
  	public void free() {
  	  coef_=0;
  	  index_=0;
//  	  saddr=null;
  	  nodeID_=(long)0;
  	  length=0;
  	}

  	public boolean equals(Coef_Elt coef) {
//		if( (index_ == coef.index_) && (saddr.equals(saddr)) ) {
  		if( (index_ == coef.index_) && (nodeID_==coef.nodeID_) ) {
  		
			return true;
		} else {
			return false;
		}
  	}
  	
  	public String key() {
//		String str=index_+saddr.getHostAddress();
		String str=index_.toString()+nodeID_.toString();
  		return str;
  	}
  	
  	public Pkt_ID getID(){
  		return new Pkt_ID(index_, nodeID_);
  		
  	}

}

