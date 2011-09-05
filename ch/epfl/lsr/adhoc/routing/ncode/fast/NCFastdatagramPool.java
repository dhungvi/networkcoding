package ch.epfl.lsr.adhoc.routing.ncode.fast;

import java.util.Enumeration;
import java.util.Vector;

import ch.epfl.lsr.adhoc.routing.ncode.CoefEltPool;
import ch.epfl.lsr.adhoc.routing.ncode.Coef_Elt;

public class NCFastdatagramPool {

	private Vector NCdatagrams;
	CoefEltPool cfpool;
	GField GF;

	public NCFastdatagramPool(CoefEltPool cfpool, GField GF) {
		NCdatagrams= new Vector(NCGlobals.pool);
		this.cfpool=cfpool;
		this.GF=GF;
	}

	/**
	 * This method creates a NCFastdatagram.
	 * <p>
	 * @return An empty NCFastdatagram object 
	 * @see NCFastdatagram
	 */
    
	private synchronized NCFastdatagram create()  {
    	if (!(NCdatagrams.isEmpty())){
	        NCFastdatagram nc = (NCFastdatagram)NCdatagrams.elementAt(0);
	        nc.setLength(0);
	        nc.setIndex(0);
	        nc.setBuf(null,0);
	        nc.coefs_list.clear();
	        return nc;
        } else {
    	    NCFastdatagram nc = new NCFastdatagram(0, GF);
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

    public synchronized void free(NCFastdatagram nc){
		  if (nc == null){
			  throw new IllegalArgumentException("error: the msg is null");
		  }
		  nc.free(cfpool);
		  NCdatagrams.add(nc);
	  }

	  public synchronized NCFastdatagram get() {
		  if (!NCdatagrams.isEmpty()) {
			  NCFastdatagram nc = (NCFastdatagram)NCdatagrams.elementAt(0);
			  NCdatagrams.removeElementAt(0);
			  return nc;
		  } else {
			  NCFastdatagram nc = (NCFastdatagram)create();
			  return nc;
		  }
	  }    	
	  
	  public NCFastdatagram clone(NCFastdatagram nc) { {		
		  int i;
		  Coef_Elt coef,coefp;

		  NCFastdatagram clone=get();
		  clone.setIndex(nc.getIndex());
		  clone.setLength(nc.getLength());
			if (clone.Buf.length<nc.getLength()) {
				nc.Buf=new int[nc.getNInt()];
			}			
			System.arraycopy(nc.Buf,0,clone.Buf,0,nc.getNInt());
			Enumeration ecoef=nc.coefs_list.elements();
			while (ecoef.hasMoreElements()) {
				coef=(Coef_Elt) ecoef.nextElement();
				coefp= cfpool.coefclone(coef);
				clone.coefs_list.put(coefp.key(),coefp);
			}
			return clone;
		}
	  }
	  
}

