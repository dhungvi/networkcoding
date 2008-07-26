package ch.epfl.lsr.adhoc.viscovery;


/**
 * This class is used to create a new Token by the means of a new thread, which is via the method
 * createToken enabled to pass the such created Token to the viscovery application for further
 * treatment, i.e. finding the next destination.
 * @see Viscovery
 * @see VisFrame
 * @author Stefan Thurnherr
 * @version 1.0
 */
class TokenCreator implements Runnable {
		/** The viscovery object related to the creation of this token */
		Viscovery visco = null;
		/** The algo that will be configured for creating a token */
		String algo = null;

		TokenCreator (Viscovery visco, String algo) {
				this.visco = visco;
				this.algo = algo;
		}


		/**
		 * This method gathers all the information necessary in order to create a new TokenMessage. The
		 * algorithm to be configured into the TokenMessage is being passed as a parameter to a new
		 * instance. The effecive creation of the object TokenMessage will only be done in the method
		 * send() of the Viscovery class, which is therefore called after having collected the information.
		 */
		public void run() {
				//visco.writeLog("---------------------\nCreating a new Token with algo " + algo);
				TokenMessage msg = null;
				try {
						long nodeID = visco.getNodeID();
						msg = (TokenMessage)visco.getMp().getMessage(visco.getMsgType());
						//msg.setTrace(tokenTrace);
						msg.setDstNode((new Long(-1)).longValue()); /* dst will be determined later on with getNextDest() */
						msg.setTTL(visco.getTTL());
						msg.setAlgo(algo);
						msg.setNbVisits((new Long(0)).longValue());
						msg.setCurrentNodeIndex(nodeID);
						//msg.addVisit(nodeID);
						//System.out.println("addVisit executed - now going to treatToken");
				} catch(Exception ex) {
						//System.out.println("# Could not create TokenMessage in createToken(): " + ex.getMessage());
						ex.printStackTrace();
				}
				visco.treatToken(msg);
		}
}
