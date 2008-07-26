/*
 * $Revision: 1.13 $
 * 
 * $Date: 2004/08/25 12:09:48 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.epfl.lsr.adhoc.simulator.config.DaemonsConfig;
import ch.epfl.lsr.adhoc.simulator.config.NetworkConfig;
import ch.epfl.lsr.adhoc.simulator.config.SimulationConfig;
import ch.epfl.lsr.adhoc.simulator.daemon.protocol.*;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * The class represents a session object
 *  
 * @version $Revision: 1.13 $ $Date: 2004/08/25 12:09:48 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class NodeDistributor {
	public static final String codeRevision =
		"$Revision: 1.13 $ $Date: 2004/08/25 12:09:48 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger =
		new SimLogger(NodeDistributor.class);

	/** Singleton pattern implementation */
	private static final NodeDistributor distributor = new NodeDistributor();

	/** Session timeout delay */
	private static long TIMEOUT = 4*172800000; //4*48 hours is maximum life time

	/** Stores a reference to the context */
	private SimulatorContext m_context = null;

	/** Data structure to manage the daemons' connections */
	private Daemon[] m_daemons;

	/** Default private constructor */
	private NodeDistributor() {
	}

	/** @return the only one instance of the class */
	public static NodeDistributor getInstance() {
		return distributor;
	}

	/** @param m_context2 */
	public static void setContext(SimulatorContext p_context) {
		distributor.m_context = p_context;

	}

	/** Starts the distributor */
	public void distribute() throws DistributionException {
		if (m_context == null) {
			throw new IllegalStateException(CONTEXT_ERROR);
		}
		init();
		connectToDaemons();
		sendData();
	}

	/** Sends a StartSessionMsg to the daemons */
	public void startSimulation() throws DistributionException {
		ObjectOutputStream oos = null;
		ObjectInputStream ios = null;

		try {
			for (int i = 0; i < m_daemons.length; i++) {
				oos =
					new ObjectOutputStream(
						m_daemons[i].socket.getOutputStream());
				oos.writeObject(new StartSessionMsg(m_daemons[i].sessionID));

				//Wait for an ACK msg
				ios =
					new ObjectInputStream(m_daemons[i].socket.getInputStream());
				Object o = ios.readObject();
				if (!(o instanceof AckMsg)) {
					throw new DistributionException(
						"DAEMON "
							+ m_daemons[i].toString()
							+ DAEMON_ILLEGAL_MSG,
						null);
				}
				logger.info(
					"DAEMON "
						+ m_daemons[i].toString()
						+ "acknowledged START OF SIMULATION");
			}
		} catch (IOException io_e) {
			throw new DistributionException(io_e.getMessage(), io_e);
		} catch (ClassNotFoundException e) {
			throw new DistributionException(e.getMessage(), e);
		}

		//Close temporally all open connections to daemons
		closeConnections();
	}

	/**
	 *  This method is called only when one wants to close all
	 *  open sessions on the remote daemons
	 * 
	 * @throws DistributionException if errors during termination of daemons
	 */
	public void finishSimulation() throws DistributionException {
		ObjectOutputStream oos = null;
		ObjectInputStream ios = null;

		/** Tries to connect again to the daemons */
		connectToDaemons();

		try {
			for (int i = 0; i < m_daemons.length; i++) {
				oos =
					new ObjectOutputStream(
						m_daemons[i].socket.getOutputStream());
				oos.writeObject(new CloseSessionMsg(m_daemons[i].sessionID));

				//Wait for an ACK msg
				ios =
					new ObjectInputStream(m_daemons[i].socket.getInputStream());
				Object o = ios.readObject();
				if (!(o instanceof AckMsg)) {
					throw new DistributionException(
						"DAEMON "
							+ m_daemons[i].toString()
							+ DAEMON_ILLEGAL_MSG,
						null);
				}
				logger.info(
					"DAEMON "
						+ m_daemons[i].toString()
						+ "acknowledged END OF SIMULATION");
			}

			//Close any open connections to daemons
			closeConnections();
		} catch (IOException io_e) {
			throw new DistributionException(io_e.getMessage(), io_e);
		} catch (ClassNotFoundException cnf_e) {
			throw new DistributionException(cnf_e.getMessage(), cnf_e);
		}
	}

	/** 
	 * Aborts the distribution process by trying to clean as many things as 
	 * possible
	 */
	public void abort() {
		ObjectOutputStream oos = null;
		ObjectInputStream ios = null;
		Daemon temp = null;

		/* First close all connections */
		closeConnections();

		/* Reconnect to all possible daemons */
		try {
			connectToDaemons();
		} catch (DistributionException e1) {
			logger.fatal(e1.getMessageKey());
		}

		for (int i = 0; i < m_daemons.length; i++) {
			try {
				if (m_daemons[i].socket != null
					&& !m_daemons[i].socket.isClosed()) {
					oos =
						new ObjectOutputStream(
							m_daemons[i].socket.getOutputStream());
					oos.writeObject(
						new AbortSessionMsg(m_daemons[i].sessionID));

					//Wait for an ACK msg
					ios =
						new ObjectInputStream(
							m_daemons[i].socket.getInputStream());
					Object o = ios.readObject();
					if (!(o instanceof AckMsg)) {
						logger.debug(DAEMON_ILLEGAL_MSG);
					} else {
						logger.info(
							"DAEMON "
								+ m_daemons[i].toString()
								+ "acknowledged ABORT OF SIMULATION");
					}
				}
			} catch (IOException io_e) {
				logger.fatal(
					"DAEMON "
						+ m_daemons[i].toString()
						+ " has failed to be closed! CHECK THIS DAEMON MANUALLY",
					io_e);
			} catch (ClassNotFoundException cnf_e) {
				logger.debug(cnf_e.getMessage(), cnf_e);
			}
		}

		//Close all open connections to daemons
		closeConnections();
	}

	/** 
	 * The methods attempts to connect to all remote daemons in order
	 * to start a successfull simulation 
	 */
	private void connectToDaemons() throws DistributionException {
		List crashedDaemons = new ArrayList();

		for (int i = 0; i < m_daemons.length; i++) {
			Daemon temp = m_daemons[i];
			Socket socket = null;
			try {
				socket = new Socket(temp.ip, temp.port);
			} catch (UnknownHostException uh_e) {
				crashedDaemons.add(temp);
			} catch (IOException io_e) {
				crashedDaemons.add(temp);
			}
			temp.socket = socket;
		}

		if (crashedDaemons.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (Iterator i = crashedDaemons.iterator(); i.hasNext();) {
				sb.append(((Daemon) i.next()).toString() + "\n");
			}
			throw new DistributionException(
				SOCKET_ONCONNECTION_ERROR + sb.toString(),
				null);
		}
		logger.info("ALL DAEMONS ARE SUCCESSFULLY CONNECTED");
	}

	/** Closes the simulator daemon's sessions */
	private void closeConnections() {
		for (int i = 0; i < m_daemons.length; i++) {
			try {
				if (m_daemons[i].socket != null) {
					m_daemons[i].socket.close();
					m_daemons[i].socket = null;
				}
			} catch (IOException io_e) {
				m_daemons[i].socket = null;
				logger.debug(SOCKET_ONCLOSE_ERROR, io_e);
			}
		}
	}

	/** Sends data to daemons */
	private void sendData() throws DistributionException {
		ObjectOutputStream oos = null;
		ObjectInputStream ios = null;
		Daemon temp = null;
		SimulationConfig sc =
			(SimulationConfig) m_context.get(ContextKey.SIMULATION_KEY);
		NetworkConfig nc =
			(NetworkConfig) m_context.get(ContextKey.NETWORK_KEY);

		int partition[] = distributeLoad();
		int nodeNumber = 0;
		try {
			for (int i = 0; i < m_daemons.length; i++) {
				temp = m_daemons[i];

				//Send a createSessionMsg
				CreateSessionMsg msg = new CreateSessionMsg(-1);
				msg.setServerIP(sc.getServIP());
				msg.setTimeout(sc.getDuration() + TIMEOUT);
				oos = new ObjectOutputStream(temp.socket.getOutputStream());
				oos.writeObject(msg);

				//Wait for an ACK msg with the new session ID
				ios = new ObjectInputStream(temp.socket.getInputStream());
				Object o = ios.readObject();

				if (o instanceof AckMsg) {
					m_daemons[i].sessionID = ((AckMsg) o).getMesgSessionID();
				} else {
					throw new DistributionException(
						"DAEMON "
							+ temp.toString()
							+ "failed to send session ID",
						null);
				}

				for (int j = partition[i]; j > 0; j--) {
					//Send a SentNodeMsg
					SentNodeMsg snm = new SentNodeMsg(temp.sessionID);
					String xmlFile =
						nc.getNodeConfig(nodeNumber).getConfigOutFile();
					snm.setFile(getFileData(xmlFile));
					snm.setTotalNbOfNodes(j - 1);

					oos = new ObjectOutputStream(temp.socket.getOutputStream());
					oos.writeObject(snm);

					//Wait for an ACK msg for having received the info
					ios = new ObjectInputStream(temp.socket.getInputStream());
					Object o1 = ios.readObject();
					if (!(o1 instanceof AckMsg)) {
						throw new DistributionException(
							"DAEMON "
								+ temp.toString()
								+ "failed to receive data",
							null);
					}
					nodeNumber++;
				}
				logger.info(
					"DAEMON "
						+ temp.toString()
						+ "acknowledged RECEPTION OF DATA");
			}

		} catch (IOException io_e) {
			throw new DistributionException(SOCKET_SEND_ERROR, io_e);
		} catch (ClassNotFoundException cnf_e) {
			throw new DistributionException(SOCKET_SEND_ERROR, cnf_e);
		}
	}

	/** Initialize the distributor by creating the Daemons datastructure */
	private void init() {
		DaemonsConfig dc =
			(DaemonsConfig) m_context.get(ContextKey.DEAMONS_KEY);
		List config = dc.getDeamonIPList();
		m_daemons = new Daemon[config.size()];
		int k = 0;
		for (Iterator i = config.iterator(); i.hasNext();) {
			Daemon temp = new Daemon();
			String[] s = (String[]) i.next();
			temp.ip = s[0];
			temp.port = Integer.parseInt(s[1]);
			m_daemons[k] = temp;
			k++;
		}
	}

	/** 
	 * Creates a possible load balancing between daemons 
	 * @return an array with the number of nodes per daemon 
	 */
	private int[] distributeLoad() {
		NetworkConfig nc =
			(NetworkConfig) m_context.get(ContextKey.NETWORK_KEY);
		int nbDaemons = m_daemons.length;
		int nbNodes = nc.getNetworkSize();

		int factor = nbNodes / nbDaemons;
		int result[] = new int[nbDaemons];

		for (int i = 0; i < result.length; i++) {
			if (nbNodes > 0) {
				if (factor == 0) {
					result[i] = 1;
					nbNodes--;
				} else {
					result[i] = (i < nbDaemons - 1 ? factor : nbNodes);
					nbNodes -= factor;
				}
			}
		}
		return result;
	}

	/** 
	 * Gets the content of the file in a array of characters
	 * @param String p_fileName the name of the file to load
	 */
	private char[] getFileData(String p_fileName)
		throws DistributionException {
		BufferedReader br;
		CharArrayWriter caw = new CharArrayWriter();
		int ch;

		try {
			br = new BufferedReader(new FileReader(p_fileName));
			caw = new CharArrayWriter();
			while ((ch = br.read()) >= 0) {
				caw.write(ch);
			}
			br.close();
		} catch (FileNotFoundException file_e) {
			throw new DistributionException(
				"Failed to get file: " + p_fileName,
				file_e);
		} catch (IOException io_e) {
			throw new DistributionException(
				"Failed to get file: " + p_fileName,
				io_e);
		}
		return caw.toCharArray();
	}

	/** 
	 * Data structure for the information the distributor 
	 * should keep for chaque
	 */
	class Daemon {
		/** The IP address of the daemon */
		public String ip;
		/** The port number of the daemon */
		public int port;
		/** The connection socket to this daemon */
		public Socket socket;
		/** The session number associated with this daemon */
		public int sessionID;
		/** Nodes associated with this daemon */
		public List nodes;

		/** get the string represenation of the daemon */
		public String toString() {
			return ip + ":" + port;
		}
	}

	private static String CONTEXT_ERROR = "The context has not been set";
	private static String SOCKET_ONCLOSE_ERROR =
		"Failed to close an opened socket connection";
	private static String SOCKET_ONCONNECTION_ERROR =
		"Failed to establish connection on daemons: ";
	private static String SOCKET_SEND_ERROR =
		"Problem occured while sending data to daemon";
	private static String DAEMON_ILLEGAL_MSG =
		"Daemon sent a corrupted message";
	private static String DAEMON_SUCC_START =
		"Daemon acknoledged successfull reception of the data";
}