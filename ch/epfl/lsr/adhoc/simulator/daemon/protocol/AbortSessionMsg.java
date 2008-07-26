/*
 * $Revision: 1.1 $
 * 
 * $Date: 2004/06/19 21:48:55 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon.protocol;

/**
 * Upon reception of this message, the daemon closes all simulation sessions.
 *  
 * @version $Revision: 1.1 $ $Date: 2004/06/19 21:48:55 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class AbortSessionMsg extends DaemonProtocolMsg {
	public static final String codeRevision =
			"$Revision: 1.1 $ $Date: 2004/06/19 21:48:55 $ Author: Boris Danev and Aurelien Frossard";

	/** @param p_sessionID */
	public AbortSessionMsg(int p_sessionID) {
		super(p_sessionID);
	}
	
	/** Gives the string representation of the object */
	public String toString(){
		return "CloseAllSessionsMsg [sessionID = "+ m_sessionID + "]";
	}
}