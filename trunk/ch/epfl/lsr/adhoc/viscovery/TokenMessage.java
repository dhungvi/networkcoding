package ch.epfl.lsr.adhoc.viscovery;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.*;

/**
 * Instances of this class represent a Message object that carries around the
 * information needed by the Viscovery layer.
 * <p>
 * This class TokenMessage is an implementation of Message, which allows to
 * set and get a textual message. Such a TokenMessage is created with the appropriate
 * message factory class, that is TokenMessageFactory.Besides the getter/setter methods,
 * the TokenMessage class provides methods for 
 * .
 * <p>
 * @see TokenMessageFactory
 * @see Message
 * @see Viscovery
 * @author Stefan Thurnherr
 * @version 1.0
 */
public class TokenMessage extends Message {
		/**
		 * Contains the name of the algorithm used to determine to which neighbor node
		 * this token should be sent next (LRV or LFV)
		 */
		private String tokenAlgo;
		/** contains in the first column the ID's of the nodes which this token has
		 * already visited and in the second column
		 */
		private List tokenTrace;
		/**
		 * stores the tokenTrace-index (as an int) of the node that this tokenMessage
		 * is currently visiting. This field is set when the method readData of an instance of
		 * TokenMessage is called. It is set to -1 if the current node has no entry yet in the
		 * tokenTrace
		*/
		private int currentNodeIndex;
		/**
		 * counts the number of nodes that this token already visited. This counter is
		 * automatically increased each time a TokenMessage is serialized, that is each
		 * time its method prepareData() is called. So the nbVisits of this TokenMessage
		 * eventually being displayed within the Viscovery application does NOT include
		 * yet the fact that this node is being visited as nbVisits will only be increased
		 * just before a node transmits its TokenMessage to another node (i.e. after the
		 * whole treating done by the viscovery layer)
		 */ 
		private long nbVisits;
		/**
		 * stores a hashtable that maps a nodeID into the corresponding index in the tokenTrace. If
		 * a nodeID is present in the hashtable, the associated value (type Long) is the index into
		 * tokenTrace of the line which holds information about the nbVisits and neighbors of this node.
		 */
		private Hashtable id2index;

		/**
		 * Creates a new instance of TokenMessage.
		 * <p>
		 * The type of this message object is initialized at creation time (creation normally
		 * happens in the TokenMessageFactory class) and cannot be changed later (for better
		 * use in a MessagePool).
		 * <p>
		 * @param type The type of service for this message
		 */
		public TokenMessage(char type) {
				super(type);
				this.tokenTrace = Collections.synchronizedList(new ArrayList());
				this.id2index = new Hashtable(tokenTrace.size());
				this.reset();
		}
		
		/**
		 * Write the content of this tokenMessage to the buffer (in order to be
		 * able to send the message to the network). The tokenTrace is serialized
		 * by writing to the buffer first the number of rows, then for each row the number of
		 * columns and then the value of each column.
		 */
		public void prepareData() {
				addString(tokenAlgo);
				this.nbVisits++; // to include the visit on this node that is going on
				addLong(nbVisits);
				addInt(tokenTrace.size());
				List traceRow = Collections.synchronizedList(new ArrayList());
				long rowEntry= -1;
				synchronized(tokenTrace) {
						for (Iterator it1 = tokenTrace.iterator(); it1.hasNext();) {
								traceRow = (List)(it1.next());
								addInt(traceRow.size()); //serialize the size of the i-th row of the tokenTrace
								for (Iterator it2 = traceRow.iterator(); it2.hasNext();) {
										rowEntry = ((Long)(it2.next())).longValue();
										addLong(rowEntry);
								}
						}
				}
		}

		/**
		 * Clear this instance of TokenMessage by resetting all fields and then
		 * read the content of this message from the buffer by using the appropriate
		 * getXXX() methods for each field. Based on the read data, the tokenTrace of
		 * this tokenMessage is then constructed as a 2-dimensional ArrayList (wrapped
		 * by Collection.synchronizedList() to prevent concurrent structural modifications)
		 */
		public void readData() {
				/**
				 * first of all clear/reset all fields of this token;if we don't do this, the deserialized
				 * values will only be added to the last visits list and the tokenTrace will grow
				 * linearly with nbVisits! :-(
				 */
				this.reset();
				long aLong = -1;
				this.tokenAlgo = getString();
				this.nbVisits = getLong(); 
				int nbTraceRows = getInt();				
				int rowCapacity = -1;
				synchronized(tokenTrace) {
						for (int i = 0; i < nbTraceRows; i++) {
								rowCapacity = getInt();
								tokenTrace.add(i, Collections.synchronizedList(new ArrayList(rowCapacity)));
								for (int j = 0; j < rowCapacity; j++) {
										aLong = getLong();
										((List)(tokenTrace.get(i))).add(j, new Long(aLong));
								}
						}
				}
		}

		/**
		 * fills in the id2index hashtable of this token and sets currentNodeIndex of this
		 * tokenMessage to the nodeID of the communicationsLayer given as parameter
		 *<p>
		 * @param nodeID the nodeID of the node which is the current holder of this token. This
		 * method will set the token's currentNodeIndex to the corresponding tokenTrace-index
		*/
		protected void setCurrentNodeIndex(long nodeID) {
				List traceRow;
				id2index = new Hashtable(tokenTrace.size());
				int i = 0;
				synchronized(tokenTrace) {
						for (Iterator it1 = tokenTrace.iterator(); it1.hasNext();i++) {
								traceRow = (List)(it1.next());
								id2index.put((Long)traceRow.get(0), new Integer(i)); //add ein key/value pair - aber WIE?
								if (((Long)traceRow.get(0)).longValue() == nodeID) {
										this.currentNodeIndex = tokenTrace.indexOf(traceRow);
										break;
								}
						}
				}
				if (this.currentNodeIndex < 0) { //this node's ID not present in tokenTrace's 0th column
						List newRow = Collections.synchronizedList(new ArrayList(2));
						tokenTrace.add(newRow);
						newRow.add(0, new Long(nodeID));
						newRow.add(1, new Long(-1));
						this.currentNodeIndex = tokenTrace.indexOf(newRow);
				}
				//System.out.println("setCurrentNodeIndex: currentNodeIndex set to " + this.currentNodeIndex);
				//System.out.println(" id2index is:\n" + id2index); 
		}

		/**
		 * gets currentNodeIndex of this tokenMessage
		 *<p>
		 * @return the tokenTrace-index of the List containing the data about the visits and
		 * neighbors of the node which this token is currently visiting. Returns -1 if the
		 * currently visited node has no line associated to him yet (i.e. no List in the 
		 * tokenTrace containing this nodes ID in the first field
		 */
		protected int getCurrentNodeIndex() {
				if (currentNodeIndex < 0 || currentNodeIndex >= tokenTrace.size()) {
						currentNodeIndex = -1;
				}
				return currentNodeIndex;
		}

		/**
		 * Reset all fields of this message to an initial value. If a TokenMessage is obtained
		 * via the MessagePool, it is MANDATORY to call this method. Therefor this method is
		 * called in the first line of readData (before the deserialisation)
		 * <p>
		 * The following fields of this TokenMessage are cleared:
		 * - tokenTrace is cleared
		 * - nbVisits is set to 0
		 * - currentNodeIndex is set to -1
		 * - tokenAlgo is set to 'NUL'
		 */
		public void reset() {
				super.reset();
				tokenTrace.clear();
				nbVisits = 0;
				currentNodeIndex = -1;
				tokenAlgo = "NUL";
				id2index.clear();
		}

		/**
		 * Changes the content of this TokenMessage.
		 * <p>
		 * @param tokenTrace The Tokens new content
		 */
		protected void setTrace(List  tokenTrace) {
				this.tokenTrace = tokenTrace;
		}

		/**
		 * updates the information contained in the token about node nodeID
		 * by increasing either its nbVisits-count by 1 (if the node has already
		 * been visited at least once by this token) or adds a new line to the
		 * tokenTrace-array (if this node has never been visited by this token)
		 * @param nodeID the nodeID which is currently being visited and should
		 * therefore be increased by 1
		 */
		protected void addVisit(long nodeID) {
				//System.out.println("addVisit: adding visit for this node (" + nodeID + "):");
				List traceRow = Collections.synchronizedList(new ArrayList());
				synchronized(tokenTrace) {
						/* if token was just created,its tokenTrace is still empty and would generate
						 * an IndexOutOfBound error. therefore we create a first row with a minimum
						 * of data
						 */
						if (tokenTrace.isEmpty()) {
								//System.out.println("addVisit: tokenTrace is empty(T.S.N.H)..filling in first row");
								traceRow.add(new Long(nodeID));

								//init count-value to -1 to clearly indicate that this is an invalid value
								traceRow.add(new Long(-1));
								tokenTrace.add(traceRow);
								this.currentNodeIndex = tokenTrace.indexOf(traceRow);
						}

						if (this.currentNodeIndex > -1) { //tokenTrace's 0th column already contains this node's ID 
								traceRow = (List)tokenTrace.get(this.currentNodeIndex);
								if ( tokenAlgo.equals("LRV")) {
										/**
										 * for LRV we have to set to the current nbVisits the
										 * value in the tokenTrace of the node which the token is
										 * currently visiting.
										 */
										traceRow.set(1, new Long(nbVisits)); //set the count of this node's row to nbVisits
								}
								/**
								 * in both (LRV/LFV) cases, we have to increment by one the value
								 * in the tokenTrace of the node which the token is currently visiting
								 * (for LRV: because the nbVisits will only be incremented by one before
								 * the message is sent over the net, via this.prepareData(). So at the
								 * moment we're in the (nbVisits+1)th visit (we started with 1)
								 * In case that the count-value is < 0 (invalid value), set it to 1
								 * regardless of the actual value (because this means that this row has
								 * just been added, but has never left this node yet
								 */
								long tmp = ((Long)(traceRow.get(1))).longValue();
								if (tmp < 0)
										tmp = 1;
								else
										tmp++;
								traceRow.set(1, new Long(tmp));
						}
						else { //this node's ID not yet present in tokenTrace's 0th column
								//System.out.println("addVisit:nodeID not yet present(T.S.N.H)");
								List newRow = Collections.synchronizedList(new ArrayList(2));
								tokenTrace.add(newRow);
								newRow.add(0, new Long(nodeID));
								if (tokenAlgo.equals("LRV")){
										newRow.add(1, new Long( nbVisits + 1));
								}
								else {
										newRow.add(1, new Long(1));
								}
						}
				}
				//System.out.println("addVisit: at the end of addVisit, trace is:\n" + this);
		}
		
		/**
		 * updates the indices in the tokenTrace representing the neighbors of this node
		 * to reflect the current neighborhood
		 * @param neighbor the list of the current neighbors of this node obtained from
		 * the NeighborService
		 */
		protected void updateNeighbors(long[] neighbor) {
				long nodeID;
				nodeID = ((Long)((List)tokenTrace.get(this.currentNodeIndex)).get(0)).longValue();
				int neighborIndex = -1;
				List traceRow = (List)(tokenTrace.get(this.currentNodeIndex));
				//System.out.println("at beginning of updateNeighbors, traceRow for this node (" + nodeID + ") is:\n" + traceRow.toString());
				boolean[] entryOK = new boolean[traceRow.size()];
				/**
				 * for each value in the traceRow, we need to test whether this nodeID is still valid, i.e. is still a neighbor
				 * of this node. We init all values to false
				 */
				for (int i = 0; i < entryOK.length; i++){
						entryOK[i] = false;
				}
				entryOK[0] = true;
				entryOK[1] = true;
				for (int i = 0; i < neighbor.length; i++) {
						synchronized(traceRow) {
								neighborIndex = traceRow.indexOf(new Long(neighbor[i]));
								/**
								 * neighborIndex -1:this neighbor not yet present in this nodes traceRow
								 * neighborIndex  1:at index 1 we store the count of this node,not a nodeID
								 */
								if (neighborIndex < 0){
										traceRow.add(new Long(neighbor[i]));
										;
								}
								else {
										entryOK[neighborIndex] = true;
								}	
						}				
				}
				/**
				 * we have now a table consisting of true or false, depending on whether traceRow.get(i)
				 * is still a valid neighbor for this node. We can now simply remove all entries i in the
				 * traceRow for which entryOK[i] is false. We do this from right to left in the traceRow
				 * because every time we remove an element from a list, all subsequent elements are
				 * shifted to the left one position (newIndex = oldIndex - 1)
				 */
				for ( int i = entryOK.length - 1; i > -1; i--) {
						if(!entryOK[i]) {
								traceRow.remove(i);
						}
				}
				//System.out.println("at end of updateNeighbors, tokenTrace is:\n" + tokenTrace);
		}

		/**
		 * updates the neighborhood information in tokenTrace of all nodes except the node which is currently being
		 * visited by this token, because this is done by updateNeighbors(). For reasons of performance, one could
		 * comment out the call to this method in the method treatToken() of the viscovery application.
		 */
		protected void updateOthersNeighbors() {
				synchronized (tokenTrace) {
						List hostNodeRow = (List)tokenTrace.get(this.currentNodeIndex);
						List traceRow = null;
						ArrayList rows2remove = new ArrayList(tokenTrace.size());
						Long hostNode = (Long)hostNodeRow.get(0);
						for (Iterator it = tokenTrace.iterator(); it.hasNext();){
								traceRow = (List)it.next();
								if ( !(((Long)traceRow.get(0)).equals(hostNode))) {
										//don't do this whole treatment for the hostNodeRow
										if (traceRow.contains(hostNode)) {
												if (hostNodeRow.contains((Long)traceRow.get(0))) {
														// thats ok, hostNodeRow and traceRow.get(0) are still neighbors
														// and this fact is stored properly in the tokenTrace
												}
												else {
														// traceRow.get(0) is not neighbor of hostNode anymore
														traceRow.remove(hostNode);
												}
										}
										else if (hostNodeRow.contains((Long)traceRow.get(0))) {
												// traceRow.get(0) is now a neighbor of hostNode
												traceRow.add(hostNode);
										}
								}
								/**
								 * traceRow is now cleaned up; if traceRow has only 2 elements left, which means that it has no
								 * more neighbors and is thus not reachable anymore (isolated or dead node) -> delete this row
								 */
								if (traceRow.size() < 3){
										rows2remove.add(traceRow);
								}
						}
						tokenTrace.removeAll(rows2remove);
						//System.out.println("updateOthersNeighbors: at end of method, tokenTrace is:\n " + tokenTrace);
				}
		}


		/**
		 * Sets the nbVisits (total number of nodes this token has visited so far)
		 * to nb
		 * @param nb the new nbVisits of this token
		 */
		protected void setNbVisits(long nb) {
				this.nbVisits = nb;
		}

		/**
		 * Returns the number of nodes that this token has visited so far
		 * @return the number of nodes this token has visited so far
		 */
		protected long getNbVisits() {
				return this.nbVisits;
		}

		/**
		 * Returns the textual message contained whithin this message object.
		 * <p>
		 * @return An array of node ID's that were visited by this tokenMessage
		 */
		protected List getTrace() {
				return tokenTrace;
		}


		/**
		 * Returns the algorithm used to determine the next node that will receive this token
		 * @return The name of the algorithm
		 */
		protected String getAlgo() {
				return tokenAlgo;
		}

		/**
		 * Sets the algorithm used to determine the next node that will receive this token
		 * @param The name of the algorithm
		 */
		protected void setAlgo(String algo) {
				this.tokenAlgo = algo;
		} 
		
		/**
		 * Overwrites Object.toString().
		 * <p>
		 * This method allows a message object to be printed in a statement such
		 * as System.out.println().
		 * <p>
		 * @return A String representation of this object (the text contained in this message)
		 */
		public String toString() {
				String s = "";
				synchronized (tokenTrace) {
						if (tokenTrace.isEmpty()){
								s = "E_M_P_T_Y";
						}
						else {
								for (Iterator it = tokenTrace.iterator(); it.hasNext();) {
										//call the toString()-method of each ArrayList
										s = s + it.next().toString() + "\n";
								}
						}
				}
				return s;
		}
}
