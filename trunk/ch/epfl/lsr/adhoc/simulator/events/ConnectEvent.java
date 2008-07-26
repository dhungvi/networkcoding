/*
 * $Revision: 1.1 $
 * 
 * $Date: 2004/06/20 13:01:13 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import java.io.Serializable;

/**
 * THe class represent a simulation Init event
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/20 13:01:13 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class ConnectEvent implements Serializable {
	
	public static final String codeRevision =
			"$Revision: 1.1 $ $Date: 2004/06/20 13:01:13 $ Author: Boris Danev and Aurelien Frossard";

	/** Stores the node ID of this connect event*/
	private int m_nodeID;
	
	public ConnectEvent(int p_nodeID){
		m_nodeID = p_nodeID;
	}
	
	/** Getter for the node ID */
	public int getNodeID(){
		return m_nodeID;
	} 
}
