/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/13 14:21:19 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import java.io.*;
import java.net.Socket;

import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * The class represents a thread which manages the session connection to the 
 * main simulator
 *  
 * @version $Revision: 1.5 $ $Date: 2004/06/13 14:21:19 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SessionThread extends Thread {
	public static final String codeRevision =
		"$Revision: 1.5 $ $Date: 2004/06/13 14:21:19 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger = new SimLogger(SessionThread.class);

	/** Waits this time before trying to read new data again */
	private static long WAITING_TIME = 100; //milliseconds

	/** Reference to the socket on which the thread is acting */
	private Socket m_socket = null;

	/** Indicates the status of the thread */
	private boolean m_running = false;

	/** Indicates the active flag of the thread */
	private boolean m_terminate = false;

	/** Default constructor */
	public SessionThread(Socket p_socket) {
		m_socket = p_socket;
	}

	/** The main run method */
	public void run() {
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		Object inMesg = null, outMesg = null;
		SessionManager manager = SessionManager.getInstance();

		if (!m_running) {
			m_running = true;
			while (m_running) {
				try {
					InputStream is = m_socket.getInputStream();
					if (is.available() > 0) {
						ois = new ObjectInputStream(is);
						inMesg = ois.readObject();
						outMesg = manager.handle(inMesg, this);
						oos =
							new ObjectOutputStream(m_socket.getOutputStream());
						oos.writeObject(outMesg);

						if (m_terminate)
							cleanup();
					}
					Thread.sleep(WAITING_TIME);
				} catch (IOException io_e) {
					logger.debug(io_e.getMessage(), io_e);
					cleanup();
				} catch (ClassNotFoundException cnf_e) {
					logger.debug(cnf_e.getMessage(), cnf_e);
					cleanup();
				} catch (InterruptedException i_e) {
					logger.debug(i_e.getMessage(), i_e);
					cleanup();
				}
			}
			cleanup();
		}
	}

	/** Nicely terminates the thread */
	private synchronized void cleanup() {
		try {
			if (m_socket != null && !m_socket.isClosed())
				m_socket.close();
		} catch (IOException io_e) {
			logger.debug(io_e.getMessage(), io_e);
		}
		m_running = false;
	}

	/** 
	 * Schedules this thread for termination
	 * the termination always takes place after the last mesg has been
	 * sent  
	 */
	public synchronized void scheduleForTermination() {
		m_terminate = true;
		m_running = false;
	}
}