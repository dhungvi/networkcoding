/*
 * $Revision: 1.13 $
 * 
 * $Date: 2004/08/09 13:22:53 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;

import ch.epfl.lsr.adhoc.simulator.daemon.protocol.*;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * The class represents a session object
 *  
 * @version $Revision: 1.13 $ $Date: 2004/08/09 13:22:53 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SessionManager {
	public static final String codeRevision =
		"$Revision: 1.13 $ $Date: 2004/08/09 13:22:53 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger = new SimLogger(SessionManager.class);

	/** Singleton pattern */
	private static final SessionManager INSTANCE = new SessionManager();

	/** Hash map containing all sessions */
	private HashMap sessions;

	/** Indicates the next available session ID to use */
	private int lastSessionID = 0;

	/** Indicates the source executable Jar file */
	private String executableJar;

	/** Indicates the path to java for remote execution */
	private String javapath;
	 
	/** Default constructor */
	private SessionManager() {
		sessions = new HashMap();
	}

	/** @return the only one instance of the manager */
	public static SessionManager getInstance() {
		return INSTANCE;
	}

	/** 
	 * Allows to set the path to an executable Jar file to be used by the
	 * manager
	 * @param p_executableJar path to a executable Jar file
	 */
	public static void setExecutableJar(String p_executableJar, String p_javapath)
		throws DaemonException {

		/* Tests the existence of the executable Jar file */
		File file = new File(p_executableJar);
		File path = new File(p_javapath);
		if (!file.exists()) {
			throw new DaemonException(INVALID_EXECJAR, null);
		}
		if (!path.exists()) {
			throw new DaemonException(INVALID_JAVAPATH, null);
		}
		INSTANCE.executableJar = p_executableJar;
		INSTANCE.javapath = p_javapath;
	}

	/** 
	 * Method should be called only from the Server
	 * @param p_incomingMesg the network message received
	 * @param p_thread the thread which served the message
	 */
	public synchronized Object handle(
		Object p_incomingMesg,
		SessionThread p_thread) {
		DaemonProtocolMsg inMesg = null;
		DaemonProtocolMsg outMesg = null;
		SessionObject so = null;

		/* Test if the mesg is really of type daemon */
		if (p_incomingMesg instanceof DaemonProtocolMsg) {
			inMesg = (DaemonProtocolMsg) p_incomingMesg;

			/* If broadcast message of type CloseAllSessionsMsg */
			if (inMesg instanceof AbortSessionMsg) {
				logger.info(
					"Aborted request for session ID = "
						+ inMesg.getMesgSessionID());
				removeSession(inMesg.getMesgSessionID());
				outMesg = new AckMsg(inMesg.getMesgSessionID());
			} else {
				/* Gets the session object which concerns the incoming message*/
				so =
					(SessionObject) sessions.get(
						new Integer(inMesg.getMesgSessionID()));

				/* Create a new session object if needed */
				if (so == null) {
					so = new SessionObject(executableJar,javapath);
				}

				so.setThread(p_thread);

				/* The finate state machine of the transaction */
				switch (so.m_currentState) {
					case 0 :
						/* The session is the Init state, so the message should 
						 * be of type CreateSessionMsg */
						if (inMesg instanceof CreateSessionMsg) {
							lastSessionID++;
							so.m_jsessionID = lastSessionID;
							so.m_currentState = 1;
							so.m_simulatorIP =
								((CreateSessionMsg) inMesg).getServerIP();
							so.setTimeout(
								((CreateSessionMsg) inMesg).getTimeout());
							sessions.put(new Integer(so.m_jsessionID), so);
							outMesg = new AckMsg(so.m_jsessionID);
							logger.info(
								"Created session with ID = "
									+ lastSessionID
									+ " for serverIP = "
									+ so.m_simulatorIP 
									+ "with MAX_TIME_TO_LIVE = "
									+ ((CreateSessionMsg) inMesg).getTimeout());
						}
						break;
					case 1 :
						SentNodeMsg temp = null;
						if (inMesg instanceof SentNodeMsg) {
							temp = (SentNodeMsg) inMesg;
							char[] file = temp.getFile();
							so.addDocument(file);
							so.m_currentState =
								(temp.getTotalNbOfNodes() > 0 ? 1 : 2);
							outMesg = new AckMsg(so.m_jsessionID);
						}
						break;
					case 2 :
						if (inMesg instanceof StartSessionMsg) {
							so.m_currentState = 3;
							so.start();
							outMesg = new AckMsg(so.m_jsessionID);
							logger.info(
								"Started session with ID = "
									+ lastSessionID
									+ " for serverIP = "
									+ so.m_simulatorIP);
						}
						break;
					case 3 :
						if (inMesg instanceof CloseSessionMsg) {
							removeSession(so.m_jsessionID);
							outMesg = new AckMsg(so.m_jsessionID);
							logger.info(
								"Closed session with ID = "
									+ (lastSessionID + 1)
									+ " for serverIP = "
									+ so.m_simulatorIP);
						}
						break;

					default :
						logger.warn("UNKNOWN MESSAGE RECEIVED by daemon");
						break;
				}
			}
		}
		return outMesg;
	}

	/**
	 * Starts a new session thread to serve a connected socket
	 * @param p_socket a newly opened socket
	 */
	public void handleConnection(Socket p_socket) {
		new SessionThread(p_socket).start();
	}

	/**
	 * Remove a session object from the central storage
	 * @param p_sessionID the session id of the session to be removed
	 */
	public synchronized void removeSession(int p_sessionID) {
		SessionObject so =
			(SessionObject) sessions.remove(new Integer(p_sessionID));
		if (so != null) {
			logger.info(
				"Session ID = "
					+ p_sessionID
					+ " created by server IP = "
					+ so.m_simulatorIP
					+ " has been successfully closed");
			lastSessionID--;
			so.close();
			so = null;
		}
	}

	/**
	 * Remove a timeouted session object
	 * @param p_sessionID the session id of the session to be removed
	 */
	public synchronized void removeTimeoutedSession(int p_sessionID) {
		SessionObject so =
			(SessionObject) sessions.remove(new Integer(p_sessionID));
		if (so != null) {
			logger.info(
				"Session ID = "
					+ p_sessionID
					+ " created by server IP = "
					+ so.m_simulatorIP
					+ " has TIMEOUTED and successfully closed");
			so.close();
			so = null;
			lastSessionID--;
		}
	}
	private static String INVALID_EXECJAR =
		"The executable jar file is does not exist!";
	private static String INVALID_JAVAPATH = 
		"The path to the java binary is not valid!";
}