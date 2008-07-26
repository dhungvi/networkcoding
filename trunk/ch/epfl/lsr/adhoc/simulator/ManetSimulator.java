/*
 * $Revision: 1.22 $
 * 
 * $Date: 2004/08/05 15:31:15 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.xml.DOMConfigurator;

import ch.epfl.lsr.adhoc.simulator.modules.*;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;
import ch.epfl.lsr.adhoc.simulator.util.xml.ParserException;
import ch.epfl.lsr.adhoc.simulator.util.xml.SimConfigurator;

/**
 * This class represents the simulator
 * 
 * @version $Revision: 1.22 $ $Date: 2004/08/05 15:31:15 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class ManetSimulator {
	public static final String codeRevision =
		"$Revision: 1.22 $ $Date: 2004/08/05 15:31:15 $ Author: Boris Danev and Aurelien Frossard";

	/** Reference to the logger */
	private static final SimLogger logger = new SimLogger(ManetSimulator.class);

	/** Global storage for the simulator's global context */
	private SimulatorContext m_context;

	/** Reference to the server component of the simulator */
	private SimulatorServer m_server;

	/** Reference to the constructor of simulation nodes */
	private NodeConstructor m_nodeConstructor;

	/** Reference to the distributor of the simulation nodes */
	private NodeDistributor m_nodeDistributor;

	/** Default constructor for the simulator */
	public ManetSimulator(String[] p_configFiles)
		throws FactoryConfigurationError, ParserException {

		/* Configure the logging package */
		DOMConfigurator.configure(p_configFiles[1]);

		/* Initialize the simulator context */
		m_context = SimulatorContext.getInstance();
		m_context.clear();

		/* Fill up the simulator context */
		SimConfigurator.configure(p_configFiles[0], m_context);

		/* Initialize simulator components */
		init(m_context);
	}

	/** Starts the simulator */
	public static void main(String[] args) {
		ManetSimulator ms = null;

		try {
			/* Parse the command line arguments */
			String files[] = parseCmdLine(args);

			/* Create the simulator */
			ms = new ManetSimulator(files);

		} catch (FactoryConfigurationError fce) {
			logger.fatal(null, fce);
			System.exit(EXIT_FAILURE);
		} catch (ParserException pe) {
			logger.fatal(null, pe);
			System.exit(EXIT_FAILURE);
		}

		/* Start the simulator */
		ms.start();
	}

	/** This method starts the simulator */
	public void start() {
		/* Start the distributor */
		try {
			m_nodeDistributor.distribute();
		} catch (DistributionException de) {
			abort(DISTRIBUTION_FATAL_ERROR,de);
		}

		/* Start the server */
		try {
			m_server.startup();
		} catch (ServerException se) {
			abort(SERVER_INIT_ERROR,se);
		}

		/* Start all nodes on the remote daemons */
		try {
			m_nodeDistributor.startSimulation();
		} catch (DistributionException de) {
			abort(DISTRIBUTION_FATAL_ERROR,de);
		}
		
		/* Main simulator runs */
		while (m_server.running > 0) {
			try {
				Thread.sleep(CHECK_DELAY);
				/* Check if all connections are still alive */
				m_server.checkTopologyConnections();
			} catch (InterruptedException ie) {
				logger.debug(null, ie);
			} catch (ServerException se) {
				logger.fatal(SERVER_CONNECTION_ERROR, se);
				m_server.running = -1;
			}
		}

		/* If the server has crashed, abort the simulation */
		if (m_server.running < 0) {
			abort(FAILURE_SIMULATION,null);
		} else {
			/* Finish the simulation process */
			try {
				m_server.finishSimulation();
			} catch (ServerException s_e) {
				abort(FAIL_NODES + FAILURE_SIMULATION, s_e);
			}

			try {
				m_nodeDistributor.finishSimulation();
			} catch (DistributionException d_e) {
				abort(FAILURE_SIMULATION, d_e);
			}

			/* Indicates that all simulation process has finished with success */
			logger.info(SUCCESS_SIMULATION);
		}
	}

	/** Overrides the default method */
	public String toString() {
		return getClass().getName();
	}

	private static final String[] parseCmdLine(String[] args) {
		if (!(args.length == 4
			&& args[0].equals(ARG1)
			&& args[2].equals(ARG2))) {
			System.out.println(USAGE);
			System.exit(EXIT_FAILURE);
		}
		return new String[] { args[1], args[3] };
	}

	/** Initialize the modules of the simulator */
	private void init(SimulatorContext p_context) {

		/* Initialize the node constructor module */
		m_nodeConstructor = NodeConstructor.getInstance();
		NodeConstructor.setContext(m_context);

		/* Initialize the server module */
		m_server = SimulatorServer.getInstance();
		SimulatorServer.setContext(m_context);
		SimulatorServer.setConstructor(m_nodeConstructor);

		/* Initialize the node distributor module */
		m_nodeDistributor = NodeDistributor.getInstance();
		NodeDistributor.setContext(m_context);
	}
	
	/** Aborts the simulator */
	private void abort(String p_mesg, Throwable p_cause){
		m_nodeDistributor.abort();
		m_server.abort();
		logger.fatal(p_mesg,p_cause);
		System.exit(EXIT_FAILURE);
	}
	
	/** Next check for connection update */
	private static long CHECK_DELAY = 5000;
	
	/** Exit failure return value */
	private static final int EXIT_FAILURE = -1;

	/** Command line arguments specification */
	private static final String ARG1 = "-config";
	private static final String ARG2 = "-log4j";
	private static final String USAGE =
		"Usage: Main -config <file1.xml> -log4j <file2.xml>";

	/** Other string constants */
	private static final String FAILURE_SIMULATION =
		"The simulation process has unfortunately failed";
		
	private static final String SUCCESS_SIMULATION =
		"The simulation process has successfully finished";

	private static final String FAIL_DAEMONS =
		"Some daemons were not correctly updated. Please check this manually";

	private static final String FAIL_NODES =
		"Fatal error: Some node has failed during finalization.";

	private static final String DISTRIBUTION_FATAL_ERROR =
		"Fatal error occurred during distribution of the simulation process";

	private static final String SERVER_INIT_ERROR =
		"Fatal error occured during initialization of the simulation server";

	private static final String SERVER_CONNECTION_ERROR =
		"A simulation node has DIED inexpectedly";
}