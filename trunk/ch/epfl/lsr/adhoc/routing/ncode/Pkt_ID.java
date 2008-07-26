package ch.epfl.lsr.adhoc.routing.ncode;
/**
 * Class Pkt_ID
 * 
 */
import java.net.*;


public class Pkt_ID {

	public int index_;
	public InetAddress saddr_;
	
	public Pkt_ID (int index, InetAddress saddr ) {
		index_=index;
		saddr_=saddr;
	}

	/**
   * Get the value of time_
   * 
   * @return the value of time_
   */
	public int getIndex (  ) {
		return index_;
  }
  /**
   * Set the value of time_
   * 
   * 
   */
	public void setIndex ( int value  ) {
		index_ = value;
  }
  /**
   * Get the value of saddr
   * 
   * @return the value of saddr
   */
	public InetAddress getSaddr (  ) {
		return saddr_;
	}
  /**
   * Set the value of saddr
   * 
   * 
   */
	public void setSaddr ( InetAddress value  ) {
		saddr_ = value;
	}

//	public Pkt_ID clone() {
  	public Object clone() {
		Pkt_ID id = new Pkt_ID(index_,saddr_);
		return (Object) id;
	}

	public boolean equals(Object o) {
	    if (o instanceof Pkt_ID) {
	    	Pkt_ID id = (Pkt_ID) o;
	    	if( (id.index_ ==index_ ) && (saddr_.equals(id.saddr_)) ) {
	    		return true;
	    	}
	    }
	    return false;
	}
}

