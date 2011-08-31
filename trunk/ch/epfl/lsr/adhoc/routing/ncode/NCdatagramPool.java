package ch.epfl.lsr.adhoc.routing.ncode;

import java.util.Enumeration;
import java.util.Vector;

public class NCdatagramPool {

	protected Vector NCdatagrams;
	protected CoefEltPool cfpool;

	public NCdatagramPool(CoefEltPool cfpool) {
		NCdatagrams= new Vector(NCGlobals.pool);
		this.cfpool=cfpool;
	}

	/**
	 * This method creates a NCdatagram.
	 * <p>
	 * @return An empty NCdatagram object 
	 * @see NCdatagram
	 */
    
	private NCdatagram createNCdatagram()  {
    	if (!(NCdatagrams.isEmpty())){
	        NCdatagram nc = (NCdatagram)NCdatagrams.elementAt(0);
	        nc.setLength(0);
	        nc.setIndex(0);
	        nc.setBuf(null,0);
	        nc.coefs_list.clear();
	        return nc;
        } else {
    	    NCdatagram nc = new NCdatagram(0);
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

    public synchronized void free(NCdatagram nc){
		  if (nc == null){
			  throw new IllegalArgumentException("error: the msg is null");
		  }
		  nc.free(cfpool);
		  NCdatagrams.add(nc);
	  }

//	  public synchronized NCdatagram getNCdatagram() {
	  public NCdatagram get() {
		  if (!NCdatagrams.isEmpty()) {
			  NCdatagram nc = (NCdatagram)NCdatagrams.elementAt(0);
			  NCdatagrams.removeElementAt(0);
			  return nc;
		  } else {
			  NCdatagram nc = (NCdatagram)createNCdatagram();
			  return nc;
		  }
	  }    	
	  
	  public NCdatagram clone(NCdatagram nc) { {		
		  int i;
		  Coef_Elt coef,coefp;

		  NCdatagram clone=get();
		  clone.setIndex(nc.getIndex());
		  clone.setLength(nc.getLength());
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
			
//		    System.arraycopy(Buf,0,g.Buf,0,dataLength);		
			return clone;
		}
	  }
	  
}

