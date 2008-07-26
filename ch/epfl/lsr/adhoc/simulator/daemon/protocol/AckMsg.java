/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/19 21:48:54 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon.protocol;

/**
 * Upon reception of this message, the daemon starts waiting for the XML file
 * to receive
 *  
 * @version $Revision: 1.5 $ $Date: 2004/06/19 21:48:54 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class AckMsg extends DaemonProtocolMsg {
	public static final String codeRevision =
			"$Revision: 1.5 $ $Date: 2004/06/19 21:48:54 $ Author: Boris Danev and Aurelien Frossard";

	/** @param p_sessionID */
	public AckMsg(int p_sessionID) {
		super(p_sessionID);
	}
		
	/** Gives the string representation of the object */
	public String toString(){
		return "AckMsg [sessionID = "+ m_sessionID + "]";
	}
}
