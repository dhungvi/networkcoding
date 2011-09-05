package ch.epfl.lsr.adhoc.routing.ncode.feedback;

import java.util.Enumeration;
import java.util.Vector;

import ch.epfl.lsr.adhoc.routing.ncode.CoefEltPool;
import ch.epfl.lsr.adhoc.routing.ncode.Coef_Elt;
import ch.epfl.lsr.adhoc.routing.ncode.NCdatagram;
import ch.epfl.lsr.adhoc.routing.ncode.NCdatagramPool;

public class NCFBDatagramPool extends NCdatagramPool {
public NCFBDatagramPool(CoefEltPool cfpool) {
	super(cfpool);
}

/**
 * This method creates a NCFBdatagram.
 * <p>
 * @return An empty NCdatagram object 
 * @see NCdatagram
 */

private NCFBdatagram createNCFBdatagram()  {
	if (!(NCdatagrams.isEmpty())){
        NCFBdatagram nc = (NCFBdatagram)NCdatagrams.elementAt(0);
        nc.setLength(0);
        nc.setIndex(0);
        nc.setBuf(null,0);
        nc.coefs_list.clear();
        /// TO ADD BLOOM FILTER CLEAR 
        return nc;
    } else {
	    NCFBdatagram nc = new NCFBdatagram();
        return nc;
    }
}

/**
 * This method is called to free a NCdatagram object that is no longer used.
 * <p>
 * The NCdatagram object becomes available for further use.
 * <p>
 * @param nc The NCdatagram object that we want to reuse
 * @throws IllegalArgumentException
 */

public synchronized void freeNCFBdatagram(NCFBdatagram nc){
	  if (nc == null){
		  throw new IllegalArgumentException("error: the msg is null");
	  }
	  nc.free(cfpool);
	  NCdatagrams.add(nc);
  }

//  public synchronized NCdatagram getNCdatagram() {
  public NCFBdatagram getNCFBdatagram() {
	  if (!NCdatagrams.isEmpty()) {
		  NCFBdatagram ncfb = (NCFBdatagram)NCdatagrams.elementAt(0);
		  NCdatagrams.removeElementAt(0);
		  return ncfb;
	  } else {
		  NCFBdatagram ncfb = (NCFBdatagram)createNCFBdatagram();
		  return ncfb;
	  }
  }    	
  
 
  public NCFBdatagram clone(NCFBdatagram nc) { {		
	  int i;
	  Coef_Elt coef,coefp;

	  NCFBdatagram clone=getNCFBdatagram();
	  clone.setIndex(nc.getIndex());
	  clone.setLength(nc.getLength());
	  clone.fbData=nc.fbData.clone();
		if (clone.Buf.length<nc.getLength()) {
			nc.Buf=new byte[nc.getLength()];
		}			
		System.arraycopy(nc.Buf,0,clone.Buf,0,nc.getLength());
		Enumeration ecoef=nc.coefs_list.elements();
		while (ecoef.hasMoreElements()) {
			coef=(Coef_Elt) ecoef.nextElement();
			coefp= cfpool.coefclone(coef);
			clone.coefs_list.put(coefp.key(),coefp);
		}
		
//	    System.arraycopy(Buf,0,g.Buf,0,dataLength);		
		return clone;
	}
  }

 }
  
