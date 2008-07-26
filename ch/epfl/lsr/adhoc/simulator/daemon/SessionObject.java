/*
 * $Revision: 1.17 $
 * 
 * $Date: 2004/08/09 13:22:53 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import java.io.*;
import java.util.*;

import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * The class represents a session object
 *  
 * @version $Revision: 1.17 $ $Date: 2004/08/09 13:22:53 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SessionObject {
	public static final String codeRevision =
		"$Revision: 1.17 $ $Date: 2004/08/09 13:22:53 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger = new SimLogger(SessionObject.class);

	/** The session timeout at start is fixed to 60 seconds */
	private static final long TIMEOUT = 60000;

	/** The command to use exec is */
	private static String[] execJava = { " -jar ", " -c:" };

	/** The ID of this session object */
	public int m_jsessionID;

	/** The current state of the session */
	public int m_currentState = 0;

	/** The simulator server which made the request */
	public String m_simulatorIP;

	/** Reference to the thread serving the session */
	private SessionThread m_thread;

	/** Pointer to the executable Jar to be used by this session object */
	private String m_execJar;

	/** Pointer to the javapath for execution */
	private String m_javapath;
	
	/** List of the XML documents that the session manages */
	private List m_documents = new ArrayList();

	/** List of the processes connected to this session */
	private List m_processes = new ArrayList();

	/** Reference to the start timer timeout */
	private Timer m_startTimer;

	/** Reference to the end timer */
	private Timer m_endTimer;



	/** 
	 * Default constructor 
	 * Upon construction, a session timeout is set
	 */
	public SessionObject(String p_execJar, String p_javapath) {
		m_execJar = p_execJar;
		m_javapath = p_javapath;
		/* Set the timer for starting the session */
		m_startTimer = new Timer();
		m_startTimer.schedule(new SessionTimeout(), TIMEOUT);
	}

	/** Make this session object actif */
	public void start() {
		String cmd = m_javapath + execJava[0] + m_execJar + execJava[1];

		for (Iterator i = m_documents.iterator(); i.hasNext();) {
			String node = (String) i.next();
			new ProcessThread(cmd + node, node).start();
		}
		scheduleForTermination();
	}

	/**
	 * Adds file in the list of files managed by this session object
	 * @param p_file the file name 
	 */
	public void addDocument(char[] p_file) {
		String localName =
			"xmlNodeSession_"
				+ m_jsessionID
				+ "_"
				+ m_documents.size()
				+ ".xml";
		saveFile(localName, p_file);
		m_documents.add(localName);
	}

	/** 
	 * Set the session thread in the session object
	 * @param  p_thread set the thread
	 */
	public void setThread(SessionThread p_thread) {
		m_thread = p_thread;
	}

	/** @param p_timeout the maximum life of this session */
	public void setTimeout(long p_timeout) {
		/* Set the timer for terminating the session */
		m_endTimer = new Timer();
		m_endTimer.schedule(new SessionTimeout(), p_timeout);
	}

	/** Schedule active components for termination */
	public void scheduleForTermination() {
		m_thread.scheduleForTermination();
		m_startTimer.cancel();
	}

	/** Closes this session object */
	public void close() {
		/* Delete any created xml file */
		for (Iterator i = m_documents.iterator(); i.hasNext();) {
			File xmlFile = new File((String) i.next());
			try {
				if (xmlFile.exists())
					xmlFile.delete();
			} catch (SecurityException se) {
				logger.debug(se.getMessage(), se);
			}
		}

		/* Closes the listening thread of the session */
		scheduleForTermination();

		/* Cancel the terminating timeout */
		m_endTimer.cancel();

		/* Stops any processes launched by this session */
		for (Iterator i = m_processes.iterator(); i.hasNext();) {
			Process proc = (Process) i.next();
			proc.destroy();
		}
	}

	/** Writes an array of characters on the file system */
	private void saveFile(String p_fileName, char[] p_file) {
		BufferedWriter br;
		try {
			br = new BufferedWriter(new FileWriter(p_fileName));
			br.write(p_file);
			br.close();
		} catch (IOException e) {
			logger.debug(SAVING_FILE_ERROR, e);
		}
	}

	/** TimerTask which stops the simulation */
	class SessionTimeout extends TimerTask {
		public void run() {
			SessionManager manager = SessionManager.getInstance();
			manager.removeTimeoutedSession(m_jsessionID);
		}
	}

	/** 
	 * The class allows to process the output of the subprocesses
	 * otherwise the exec is blocked
	 */
	class ProcessThread extends Thread {
		private String m_command;
		private String m_node;

		public ProcessThread(String p_command, String p_node) {
			m_command = p_command;
			m_node = p_node;
		}

		public void run() {
			Process proc = null;
			BufferedReader in;
			int ch;

			try {
				proc = Runtime.getRuntime().exec(m_command);
				m_processes.add(proc);
			} catch (IOException io_e) {
				logger.error(EXEC_ERROR, io_e);
			}

			try {
				File f = new File(m_node + ".stdout");
				FileWriter fw = new FileWriter(f);

				if (proc != null) {
					in =
						new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					while (in != null && (ch = in.read()) >= 0) {
						//Process any output of the framework
						fw.write(ch);
					}
					fw.close();
				} else {
					logger.error(EXEC_ERROR + m_command);
				}
			} catch (IOException e) {
			}
		}
	}

	/** Global message strings */
	private static String EXEC_ERROR = "Unable to execute the command: ";
	private static String SAVING_FILE_ERROR = "Unable to save the file";
}
