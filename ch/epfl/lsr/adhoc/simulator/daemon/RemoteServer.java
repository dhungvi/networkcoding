/*
 * $Revision: 1.10 $
 * 
 * $Date: 2004/08/09 13:22:53 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The class represents a remote daemon which waits for incoming connections
 * from the main simulator
 *  
 * @version $Revision: 1.10 $ $Date: 2004/08/09 13:22:53 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class RemoteServer {
	public static final String codeRevision =
		"$Revision: 1.10 $ $Date: 2004/08/09 13:22:53 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger = new SimLogger(RemoteServer.class);

	/** Reference to the session manager object */
	private SessionManager m_manager = SessionManager.getInstance();

	/** Connection parameters */
	private String m_hostname;
	private int m_portNumber;

	/** 
	 * Default constructor 
	 * @param p_hostname host IP address
	 * @param p_portNumber number of the port 
	 * @param p_execJar the executable Jar to be launched
	 */
	public RemoteServer(String p_hostname, int p_portNumber, 
		String p_execJar, String p_javapath) throws DaemonException {
		m_hostname = p_hostname;
		m_portNumber = p_portNumber;

		/* Configure the manager */
		SessionManager.setExecutableJar(p_execJar,p_javapath);
		m_manager = SessionManager.getInstance();
	}

	/** The method starts the server */
	public void startup() {
		ServerSocket servSocket = null;
		SocketAddress sockAddr =
			new InetSocketAddress(m_hostname, m_portNumber);

		try {
			servSocket = new ServerSocket();
			servSocket.bind(sockAddr);
		} catch (IOException e) {
			logger.fatal(
				SOCKET_INSTANTIATION_ERROR + m_hostname + ":" + m_portNumber);
			System.exit(EXIT_FAILURE);
		}

		while (true) {
			try {
				Socket socket = servSocket.accept();
				m_manager.handleConnection(socket);
			} catch (IOException io_e) {
				logger.debug(io_e.getMessage(), io_e);
			}
		}
	}

	/** Starts a new server  */
	public static void main(String[] args) {
		/* Configure the logging package */
		PropertyConfigurator.configure(DaemonLoggerConfig.getLoggerConfig());

		/* Extracts the IP address and port number */
		String[] cmds = parseCmdLine(args);

		try {
			/* Starts the daemon */
			new RemoteServer(
				cmds[0],
				Integer.parseInt(cmds[1]),
				cmds[2],
				cmds[3])
				.startup();
		} catch (NumberFormatException nfe_e) {
			logger.fatal(nfe_e.getMessage(), nfe_e);
		} catch (DaemonException d_e) {
			logger.fatal(d_e.getMessageKey(), d_e);
		}
	}

	/** Parses the command line */
	private static final String[] parseCmdLine(String[] args) {
		if (!(args.length == 8
			&& args[0].equals(ARG1)
			&& args[2].equals(ARG2)
			&& args[4].equals(ARG3)
			&& args[6].equals(ARG4))) {
			System.err.println(USAGE);
			System.exit(EXIT_FAILURE);
		}
		return new String[] { args[1], args[3], args[5], args[7] };
	}

	/** Command line arguments specification */
	private static final String ARG1 = "-host";
	private static final String ARG2 = "-port";
	private static final String ARG3 = "-execjar";
	private static final String ARG4 = "-javapath";
	private static final String USAGE =
		"Usage: RemoteServer -host <hostname> -port <number> "
			+ "-execjar <executable jar> -javapath <path>";

	/** Exit failure return value */
	private static final int EXIT_FAILURE = -1;
	private static final String SOCKET_INSTANTIATION_ERROR =
		"Unable to create server socket on: ";
}