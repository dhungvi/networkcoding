/*
 * $Revision: 1.6 $
 * 
 * $Date: 2004/06/20 13:01:14 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon.protocol;

/**
 * Upon reception of this message, the daemon starts a new simulation session.
 *  
 * @version $Revision: 1.6 $ $Date: 2004/06/20 13:01:14 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class CreateSessionMsg extends DaemonProtocolMsg {
	public static final String codeRevision =
			"$Revision: 1.6 $ $Date: 2004/06/20 13:01:14 $ Author: Boris Danev and Aurelien Frossard";

	/** Stores the timeout of the session */
	private long m_timeout;
	
	/** Stores the server IP */
	private String m_serverIP;
	
	/** @param p_sessionID */
	public CreateSessionMsg(int p_sessionID) {
		super(p_sessionID);
	}
	
	/** @param p_timeout timeout in milliseconds */
	public void setTimeout(long p_timeout){
		m_timeout = p_timeout;	
	}
	
	/** Getter for the session timeout */
	public long getTimeout(){
		return m_timeout;
	}
	
	/** @param p_ip the IP address of the server who sends the request */
	public void setServerIP(String p_serverIP){
		m_serverIP = p_serverIP; 
	}	
	
	/** Getter for the server IP who made the request */
	public String getServerIP(){
		return m_serverIP;
	}
	
	/** Gives the string representation of the object */
	public String toString(){
		return "CreateSessionMsg [sessionID = "+ m_sessionID + "]";
	}
}