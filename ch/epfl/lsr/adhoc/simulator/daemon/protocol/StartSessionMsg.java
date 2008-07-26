/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/19 21:48:56 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon.protocol;

/**
 * Upon reception of this message, the daemon starts the nodes for which it is
 * in charge of.
 *  
 * @version $Revision: 1.5 $ $Date: 2004/06/19 21:48:56 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class StartSessionMsg extends DaemonProtocolMsg {
	public static final String codeRevision =
			"$Revision: 1.5 $ $Date: 2004/06/19 21:48:56 $ Author: Boris Danev and Aurelien Frossard";

	/** @param p_sessionID */
	public StartSessionMsg(int p_sessionID) {
		super(p_sessionID);
	}
		
	/** Gives the string representation of the object */
	public String toString(){
		return "StartSessionMsg [sessionID = "+ m_sessionID + "]";
	}
}
