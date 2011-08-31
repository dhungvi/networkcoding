package ch.epfl.lsr.adhoc.routing.ncode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

public class CoefEltPool {
    	private Vector coefElts;

    	public CoefEltPool() {
    		coefElts= new Vector(NCGlobals.pool);
    	}
    	
    	/**
    	 * This method creates a Coef_Elt.
    	 * <p>
    	 * @return An empty Coef_Elt object 
    	 * @see NCdatagram
    	 */
	    public synchronized Coef_Elt createCoefElt(int val, int index)  {
	    	if (!(coefElts.isEmpty())){
		        Coef_Elt coef = (Coef_Elt)coefElts.elementAt(0);
		        coef.setCoef(val);
		        coef.setIndex(index);
//		        try {
//				  coef.saddr=InetAddress.getLocalHost();
//		        }
//		        catch(UnknownHostException e) {
//				  System.err.println(e);
//		        }
		        coef.nodeID_=NCGlobals.nodeID_;
//		        coef.id_=new Pkt_ID(index);
		        coef.length=0;
		        return coef;
	        } else {
	    	    Coef_Elt coef= new Coef_Elt(val,index);
		        return coef;
	        }
	    }
	    /**
	     * This method is called to free a Coef_Elt object that is no longer used.
	     * <p>
	     * The Coef_Elt object becomes available for further use.
	     * <p>
	     * @param coef The Coef_Elt object that we want to reuse
	     * @throws IllegalArgumentException
	     */

	    public synchronized void freeCoef(Coef_Elt coef){
    		  if (coef == null){
    			  throw new IllegalArgumentException("error: the msg is null");
    		  }
    		  coef.free();
    		  coefElts.add(coef);
    	  }

    	  public synchronized Coef_Elt getCoefElt(int val, int index) {
    		  if (!coefElts.isEmpty()) {
  		        Coef_Elt coef = (Coef_Elt)coefElts.elementAt(0);
		        coef.setCoef(val);
		        coef.setIndex(index);
//	        	coef.saddr=Globals.localAdd;
		        coef.nodeID_=NCGlobals.nodeID_;
		        coef.length=0;
				coefElts.removeElementAt(0);
		        return coef;
	        } else {
	        	Coef_Elt coef= createCoefElt(val,index);
				return coef;
			  }
		  }
    	  
    	  public synchronized Coef_Elt coefclone(Coef_Elt coef) {
    		  
    		  Coef_Elt clone=getCoefElt(coef.getCoef(),coef.index_);
//    		  clone.saddr=coef.getSaddr();
    		  clone.nodeID_=coef.getnodeID();
    		  return clone;
    	  }
    }

