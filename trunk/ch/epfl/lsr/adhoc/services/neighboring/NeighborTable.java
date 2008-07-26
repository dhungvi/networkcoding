package ch.epfl.lsr.adhoc.services.neighboring;

import java.util.*;

/**
 * This class implements the neighbor table.
 * <p>
 * This is a collection of information
 * about all neighbors reachable at a given moment. A collection consists of
 * several neighbor table entries
 *
 * @see NeighborTableEntry
 * @author Reto Krummenacher
 */
public class NeighborTable {

	protected Vector table;

    //CONSTRUCTORS
    NeighborTable() {
	 	table = new Vector();
    }

    //METHODS
	 /**
	 * Inserts a new NeighborTableEntry in the NeighborTable
	 *
	 * @param entry The NeighborTableEntry object to be added to the table.	 
	 */
	 public synchronized void addNeighbor(NeighborTableEntry entry) {
	 	table.addElement(entry);
	 }
	 
	 /**
	 * Removes the NeighborTableEntry at the specified position in this table and
	 * shifts any subsequent NeighborTableEntries to the left.
	 *
	 * @param index - the index of the NeighborTableEntry to remove.	 
	 */
	 public synchronized void removeNeighbor(int index) {
	 	table.removeElementAt(index);
	 }
	 
	 /**
	  * Returns the NeighborTableEntry at the specified position in this table
	  *
	  * @param index - index of NeighborTableEntry to return.
	  */
	 public synchronized NeighborTableEntry getNeighbor(int index) {
	 	return (NeighborTableEntry)table.elementAt(index);
	 }
	 
	/**
	 * Returns a list a neighbors ids. 
	 */
	public synchronized long[] getNeighborsIds() {
		long ids[] = new long[size()];
		for(int i=0;i<ids.length;i++) {
			ids[i] = getNeighbor(i).getID();
		}
		return ids;
	}
	
	/**
	 * Returns a list a neighbors names. 
	 */
	public synchronized String[] getNeighborsNames() {
		String names[] = new String[size()];
		for(int i=0;i<names.length;i++) {
			names[i] = getNeighbor(i).getNodeName();
		}
		return names;
	}

	 /**
	 * Returns the number of NeighborTableEntries in this table
	 *
	 * \return .the number of NeighborTableEntries in this table
	 */
	 public int size() {
	 	return table.size();
	 }
	 
    /**
     * This method transforms the table to a printable string.
     * <p>
     * Neighbor Table:<br>
     * ----------------------------<br>
     * ident1 -- 6245<br>
     * ident2 -- 6906<br>
     * ident3 -- 7802<br>
     * ----------------------------<br>
     *
     * @return The neighbor table transformed to a string
     */
    public String getString() {
        String entries = "\nNeighbor Table:\n------------------------------------------\n";
        for(int i=0; i<size();i++) {
            NeighborTableEntry n = getNeighbor(i);
            entries = entries + n.toString() + "\n";
        }
        entries = entries + "------------------------------------------";
        return entries;
    }
    
    public String [][] getTableString() {
    	String [][]liste = new String [size()][3];
    	for (int i=0; i<size();i++){
    		NeighborTableEntry nTE = getNeighbor (i);
    		liste [i][0] = nTE.getNodeName();
    		liste [i][1] = ""+nTE.getID ();
    		liste [i][2] = ""+(nTE.getLifetime() - System.currentTimeMillis());
    	}
    	return liste;	
    }
}
