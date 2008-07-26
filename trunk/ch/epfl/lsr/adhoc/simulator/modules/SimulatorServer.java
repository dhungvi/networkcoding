/*
 * $Revision: 1.14 $
 * 
 * $Date: 2004/08/09 13:22:54 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.lsr.adhoc.simulator.config.NetworkConfig;
import ch.epfl.lsr.adhoc.simulator.config.SimulationConfig;
import ch.epfl.lsr.adhoc.simulator.events.CheckConnectionEvent;
import ch.epfl.lsr.adhoc.simulator.events.ConnectEvent;
import ch.epfl.lsr.adhoc.simulator.events.InitEvent;
import ch.epfl.lsr.adhoc.simulator.events.StopEvent;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * This module represents the simulation server
 * 
 * @version $Revision: 1.14 $ $Date: 2004/08/09 13:22:54 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimulatorServer implements Runnable {
	public static final String codeRevision =
		"$Revision: 1.14 $ $Date: 2004/08/09 13:22:54 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger =
		new SimLogger(SimulatorServer.class);

	/** The socket timeout */
	private static final int ACCEPT_TIMEOUT = 5;
	/** The delay before the simulation starts */
	private static final long START_DELAY = 5000;

	public int running = 1; //By default the server is running
	private ServerSocket servSocket;
	private ControlSocket[] controllerSockets = null;
	private SimulatorContext context;
	private NodeConstructor constructor;
	private Thread serverThread;
	private long m_realEndSimTime;
	private boolean contextConfigured = false;
	private boolean constructorConfigured = false;
	private boolean isTopologyConnected = false;

	/** Singleton implementation of the server */
	private static final SimulatorServer server = new SimulatorServer();
	public static SimulatorServer getInstance() {
		return server;
	}
	private SimulatorServer() {
	}

	/**
	 * Sets the context in the server's scope
	 * @param p_context
	 */
	public static void setContext(SimulatorContext p_context) {
		if (!server.contextConfigured) {
			server.context = p_context;
			server.contextConfigured = true;
		}
	}

	/**
	 * Sets the constructor in the server's scope
	 * @param p_constructor
	 */
	public static void setConstructor(NodeConstructor p_constructor) {
		if (!server.constructorConfigured) {
			server.constructor = p_constructor;
			server.constructorConfigured = true;
		}
	}

	/** 
	 * Creates a server thread and starts the server
	 * @throws ServerException if the server was enable to start 
	 */
	public void startup() throws ServerException {
		/* Initialize the server */
		init();

		/* Start the server thread */
		serverThread = new Thread(server);
		serverThread.start();
	}

	/** Default method for running the server as a thread */
	public void run() {
		if (!contextConfigured && !constructorConfigured)
			throw new IllegalStateException(CONTEXT_ERROR);

		Socket temp = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		int timeout = ACCEPT_TIMEOUT, i = 0;

		/* Connect to all nodes in the simulation network */
		while (i < server.controllerSockets.length) {
			while (timeout > 0) {
				try {
					temp = server.servSocket.accept();
					if (temp != null)
						break;
				} catch (IOException io) {
					timeout--;
				}
			}
			if (temp == null) {
				logger.fatal(FAIL_CONNECT + i);
				running = -1;
				return;
			}
			timeout = ACCEPT_TIMEOUT;
			server.controllerSockets[i] = new ControlSocket();
			server.controllerSockets[i].nodeSocket = temp;
			temp = null;
			i++;
		}

		/* Construct nodes and send them via the network */
		long startTime = normalize(System.currentTimeMillis() + START_DELAY);
		constructor.setStartTime(startTime);
		long endTime =
			startTime
				+ ((SimulationConfig) context.get(ContextKey.SIMULATION_KEY))
					.getDuration();

		/* Sets the stop timer for the simulation process */
		new Timer().schedule(new StopSimulation(), new Date(endTime));

		for (int k = 0; k < server.controllerSockets.length; k++) {
			ControlSocket control;
			InitEvent initE;
			ConnectEvent connectE;
			Object event;

			//InitEvent z = 
			/* Waits for ConnectEvent from each node */
			try {
				control = server.controllerSockets[k];
				ois =
					new ObjectInputStream(control.nodeSocket.getInputStream());
				event = ois.readObject();
				if (!(event instanceof ConnectEvent)) {
					logger.fatal(FAIL_CONNECT_MESG);
					running = -1;
					return;
				}

				connectE = (ConnectEvent) event;
				initE = constructor.createNode(connectE.getNodeID());
				control.nodeID = connectE.getNodeID();
				oos =
					new ObjectOutputStream(
						control.nodeSocket.getOutputStream());
				oos.writeObject(initE);

			} catch (IOException initMesg_e) {
				logger.fatal(FAIL_INIT_MESG, initMesg_e);
				running = -1;
				return;
			} catch (ClassNotFoundException e) {
				logger.fatal(FAIL_CONNECT_MESG);
				running = -1;
				return;
			}
		}
		isTopologyConnected = true;
		logger.info(SUCC_NETWORK);
	}

	/** 
	 * The function closes the simulation on the server side 
	 * 1) Sends a Stop event to each node in the simulation topology 
	 * 2) Gets simulation data from each node
	 * 2) Closes all nodes nicely 
	 */
	public void finishSimulation() throws ServerException {
		if (!contextConfigured && !constructorConfigured)
			throw new IllegalStateException(CONTEXT_ERROR);

		ObjectOutputStream oos;
		StopEvent stopEvent;
		ControlSocket control;

		/* Send a stop event to all nodes and get each node simulation log */
		constructor.setEndTime(m_realEndSimTime);
		for (int i = 0; i < server.controllerSockets.length; i++) {
			stopEvent = constructor.destroyNode();
			control = server.controllerSockets[i];
			try {
				oos =
					new ObjectOutputStream(
						control.nodeSocket.getOutputStream());
				oos.writeObject(stopEvent);
				fileTransfer(control);
				control.nodeSocket.close();
			} catch (IOException termIO_e) {
				throw new ServerException(FAIL_TERMINATE_NODE, termIO_e);
			}
		}
		//Closes the server socket also
		try {
			if (server.servSocket != null)
				server.servSocket.close();
		} catch (IOException io_e) {
			throw new ServerException(FAIL_TERMINATE_NODE, io_e);
		}
	}

	/** Check if a connection to a node has been disconnected */
	public void checkTopologyConnections() throws ServerException {
		if (isTopologyConnected)
			for (int i = 0; i < server.controllerSockets.length; i++) {
				if (!server.controllerSockets[i].nodeSocket.isClosed())
					try {
						ObjectOutputStream is =
							new ObjectOutputStream(
								server
									.controllerSockets[i]
									.nodeSocket
									.getOutputStream());
						is.writeObject(new CheckConnectionEvent());
					} catch (IOException e) {
						logger.fatal(DISCONNECT_ERROR + i, e);
						throw new ServerException(DISCONNECT_ERROR, e);
					}
			}
	}

	/** Terminates any open sockets */
	public void abort() {
		/* Close any open connections to nodes */
		if (server.controllerSockets != null) {
			for (int i = 0; i < server.controllerSockets.length; i++) {
				if (server.controllerSockets[i] != null) {
					try {
						controllerSockets[i].nodeSocket.close();
					} catch (IOException co) {
						controllerSockets[i] = null;
						logger.warn(FAIL_CLOSE_NODE + i, co);
					}
				}
			}
		}
		/* Close the server socket */
		try {
			if (server.servSocket != null)
				server.servSocket.close();
		} catch (IOException so) {
			logger.warn(FAIL_CLOSE_SERVER, so);
		}
	}

	/** Initialize the server
	 * @throws ServerException if impossible to bind a server socket
	 */
	private void init() throws ServerException {
		if (!contextConfigured && !constructorConfigured)
			throw new IllegalStateException(CONTEXT_ERROR);

		SimulationConfig sc =
			(SimulationConfig) context.get(ContextKey.SIMULATION_KEY);
		NetworkConfig nc = (NetworkConfig) context.get(ContextKey.NETWORK_KEY);

		/* Initialize the number of controllers which should connect */
		controllerSockets = new ControlSocket[nc.getNetworkSize()];

		SocketAddress sockAddr =
			new InetSocketAddress(sc.getServIP(), sc.getServPort());

		try {
			servSocket = new ServerSocket();
			servSocket.bind(sockAddr, sc.getServQueue());
			servSocket.setSoTimeout(sc.getServTimeout());
		} catch (IOException io_e) {
			throw new ServerException(SERVER_INIT_ERROR, io_e);
		}
	}

	/** 
	 * Transfer a socket content in a file content
	 * @param p_control ControSocket data object
	 */
	private void fileTransfer(ControlSocket p_control) throws ServerException {
		BufferedReader is;
		BufferedWriter fos;
		int ch;
		SimulationConfig sc =
			(SimulationConfig) context.get(ContextKey.SIMULATION_KEY);
		try {
			fos =
				new BufferedWriter(
					new FileWriter(
						sc.getOutputDir()
							+ OUTPUT_FILE_NAME
							+ p_control.nodeID));
			is =
				new BufferedReader(
					new InputStreamReader(
						p_control.nodeSocket.getInputStream()));
			while ((ch = is.read()) >= 0) {
				fos.write(ch);
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException file_e) {
			throw new ServerException(
				FAIL_FILE_TRANSFER + p_control.nodeID,
				file_e);
		} catch (IOException io_e) {
			throw new ServerException(
				FAIL_FILE_TRANSFER + p_control.nodeID,
				io_e);
		}

	}

	/** 
	 * Gets the ceiling of the time in milliseconds 
	 * @param p_time the time to be transformed 
	 */
	private long normalize(long p_time) {
		return ((long) Math.ceil((double) p_time / 1000.0)) * 1000;
	}

	/** Data structure for controlling sockets */
	class ControlSocket {
		Socket nodeSocket;
		int nodeID;
	}

	/** This class represents a Task which could stop the simulation at
	 *  a given time */
	class StopSimulation extends TimerTask {
		public void run() {
			synchronized (this) {
				running = 0;
				m_realEndSimTime = System.currentTimeMillis();
			}
		}
	}

	private static final int EXIT_FAILURE = -1;
	private static final String CONTEXT_ERROR =
		"Context or Constructor has not been set";
	private static final String SERVER_INIT_ERROR =
		"Initialization of the server failed";
	private static final String FAIL_CONNECT = "Fail to connect NODE ";
	private static final String FAIL_INIT_MESG = "Failed to send a InitMessage";
	private static final String FAIL_CONNECT_MESG =
		"Failed to receive connect message";
	private static final String SUCC_NETWORK =
		"NETWORK IS SUCCESSFULLY CONNECTED";
	private static final String FAIL_CLOSE_NODE = "Failed to close node ";
	private static final String FAIL_CLOSE_SERVER =
		"Failed to close the server socket!";
	private static final String SUCC_FILE_TRANSFER =
		"Success file transfer from NODE ";
	private static final String FAIL_FILE_TRANSFER =
		"Fail file transfer from NODE ";
	private static final String FAIL_TERMINATE_NODE =
		"Failed to terminate NODE ";
	private static final String SUCC_TERMINATE_NODE =
		"Succeeded to terminate NODE ";
	private static final String SIMULATION_ERROR = "Simulation has failed!";
	private static final String DISCONNECT_ERROR =
		"Disconnecting event occurred from NODE ";
	private static final String OUTPUT_FILE_NAME = "/simulationNode.log";
}
