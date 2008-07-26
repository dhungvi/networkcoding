package ch.epfl.lsr.adhoc.routing.ncode;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Class Coef_Elt
 * 
 */
public class Coef_Elt extends Object {
  // Fields
  // 
  public int coef_;
  public int index_;
  public InetAddress saddr;
  public int length;
  public final static int coef_size = 13;


  // Methods
  // Constructors
  // Empty Constructor
  public Coef_Elt (int coef,int index) {
	  coef_=coef;
	  index_=index;
	  saddr=Globals.localAdd;
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

  public InetAddress getSaddr (  ) {
    return saddr;
  }
  /**
   * Set the value of saddr
   * 
   * 
   */
  public void setSaddr (InetAddress add  ) {
    saddr = add;
  }


		
	public void updateID( int value  ) {
		index_ = value;
		try{
			String str=saddr.getHostAddress();
			saddr = InetAddress.getByName(str);
		}		
		catch(UnknownHostException e) {
			System.err.println(e);
		}
	}
  
 
  
//	public Coef_Elt clone() {
  	public Object clone() {
  		Coef_Elt coef_elt = new Coef_Elt(coef_,index_);
  		coef_elt.length=length;
		try{
			String str=saddr.getHostAddress();
			saddr = InetAddress.getByName(str);
		}
		catch(UnknownHostException e) {
			System.err.println(e);
			return null;
		}
		return coef_elt;
	}
  	
  	public void free() {
  	  coef_=0;
  	  index_=0;
  	  saddr=null;
  	  length=0;
  	}

  	public boolean equals(Coef_Elt coef) {
		if( (index_ == coef.index_) && (saddr.equals(saddr)) ) {
			return true;
		} else {
			return false;
		}
  	}
  	
  	public String key() {
  		String str=index_+saddr.getHostAddress();
  		return str;
  	}
  	
  	public Pkt_ID getID(){
  		return new Pkt_ID(index_, saddr);
  		
  	}

}

