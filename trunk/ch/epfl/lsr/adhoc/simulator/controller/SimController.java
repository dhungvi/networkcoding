/*
 * $Revision: 1.25 $
 * 
 * $Date: 2004/08/05 15:31:16 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.controller;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.PropertyConfigurator;

import ch.epfl.lsr.adhoc.simulator.config.NodeConfig;
import ch.epfl.lsr.adhoc.simulator.events.ConnectEvent;
import ch.epfl.lsr.adhoc.simulator.events.EndSimulationEvent;
import ch.epfl.lsr.adhoc.simulator.events.IUpdateSimulationLayerEvent;
import ch.epfl.lsr.adhoc.simulator.events.InitEvent;
import ch.epfl.lsr.adhoc.simulator.events.SimulationWarningEvent;
import ch.epfl.lsr.adhoc.simulator.events.StopEvent;
import ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer_ControllerView;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * This class represents the controller of the simulation
 * 
 * @version $Revision: 1.25 $ $Date: 2004/08/05 15:31:16 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimController implements ISimController {

	public static final String codeRevision =
		"$Revision: 1.25 $ $Date: 2004/08/05 15:31:16 $ Author: Boris Danev and Aurelien Frossard";

	/* Implementation comment comes here */

	/** Private logger for the simulation clock */
	private static final SimLogger logger = new SimLogger(SimController.class);

	/** Timeouts for establishing a connection */
	private static long NEXT_ATTEMPT = 2000;
	private static int NUMBER_OF_ATTEMPTS = 5;

	/** Reference to the simulation clock */
	private ISimClock m_clock;
	/** Reference to the simulation layer controlled by the controller */
	private ISimulationLayer_ControllerView m_simLayer;
	/** The thread in which the controller is running */
	private Thread m_controllerThread;
	/** Reference to the remote server, to which the controller should connect */
	private Socket m_socket = null;
	/** Storage for the final StopEvent of the simulation */
	private StopEvent stopEvent = null;
	/** Stores the node config */
	private NodeConfig m_config;
	/** Indicates if the controller is running */
	private boolean m_running = false;
	/** Stores the discrete simulation step */
	private long m_currentStep = 0;
	/** Stores the final current step */
	private long m_finalCurrentStep;
	/** The start time of the simulation is set to 0 */
	private long m_realStartTime = 0;
	/** The end time of the simulation is set to MAX_VALUE */
	private long m_realEndTime = Long.MAX_VALUE;
	/** A reference to the clock speed for faster execution */
	private long m_clockSpeed;

	/**
	 * Default constructor
	 * 
	 * @param p_simLayer
	 *            the layer to be controlled
	 */
	public SimController(ISimulationLayer_ControllerView p_simLayer) {
		m_simLayer = p_simLayer;
		init();
	}

	/** Starts the controller thread */
	public void startup(int p_nodeID) {
		if (!m_running) {
			m_running = true; //The controller is running

			/* Creates connection to the main simulator */
			createConnection();
			try {
				ObjectOutputStream oos =
					new ObjectOutputStream(m_socket.getOutputStream());
				oos.writeObject(new ConnectEvent(p_nodeID));

				ObjectInputStream ois =
					new ObjectInputStream(m_socket.getInputStream());
				configure((InitEvent) ois.readObject());
			} catch (Exception config) {
				logger.debug(INIT_FAIL, config);
				terminate();
			}
			logger.debug(INIT_SUCCESS);
		}
	}

	/** Terminates the controller and frees all resources */
	public void terminate() {
		/* Terminates the simulation clock */
		m_clock.terminate();

		/* Terminates the simulation layer */
		m_simLayer.terminate();

		/* Frees controller resources */
		try {
			if (m_socket != null)
				m_socket.close();
		} catch (IOException io) {
			logger.debug(SOCKET_CLOSE_ERROR);
		}

		/* Exits the virtual machine */
		System.exit(-1);
	}

	/** When this method is called the Controller updates its state */
	public synchronized void update() {
		/* Increment the simulation step */
		m_currentStep += m_clockSpeed;

		if (m_currentStep < m_finalCurrentStep) {
			/* Create an array of events for layer update */
				IUpdateSimulationLayerEvent events[] = //TODO avoid recurent
		// creation of array
	new IUpdateSimulationLayerEvent[] {
		m_config.getSchedule().getScheduleAt(m_currentStep),
		m_config.getMobility().getPositionAt(m_currentStep)};

			/* Update the node's schedule state */
			m_simLayer.update(events);
		} else {
			/* Create an array of events with an EndSimulationEvent */
			IUpdateSimulationLayerEvent events[] =
				new IUpdateSimulationLayerEvent[] {
					m_config.getSchedule().getScheduleAt(m_currentStep),
					m_config.getMobility().getPositionAt(m_currentStep),
					new EndSimulationEvent(
						m_currentStep,
						System.currentTimeMillis())};
			/* Update the node's schedule state */
			m_simLayer.update(events);
		}
		try {
			if (m_socket.getInputStream().available() > 0) {
				Object event;
				ObjectInputStream ois =
					new ObjectInputStream(m_socket.getInputStream());
				event = ois.readObject();
				if (event instanceof StopEvent) {
					stopEvent = (StopEvent) event;
				}
			}
		} catch (IOException io_e) {
			logger.debug(SOCKET_ERROR, io_e);
		} catch (ClassNotFoundException cast_e) {
			logger.debug(CAST_ERROR, cast_e);
		}

		/*
		 * If this condition is satisfied we stop the simulation and test the
		 * measurements
		 */
		if (m_currentStep >= m_finalCurrentStep
			&& stopEvent != null
			&& m_realEndTime < Long.MAX_VALUE) {
			//Send all data back to the server
			sendLoggingInfo();
			//Delete the logging info
			deleteLoggingInfo();
			// Tests GLOBAL SYNCHRONIZATION between server and node
			if (Math.abs(stopEvent.getTime() - m_realEndTime) > m_clockSpeed) {
				logger.info(
					new SimulationWarningEvent(
						m_currentStep,
						stopEvent.getTime(),
						m_realEndTime,
						GLOBAL_SYNC_ERROR));
			}

			//Tests LOCAL SYNCHRONIZATION inside the node
			if (Math
				.abs((m_realStartTime + m_finalCurrentStep) - m_realEndTime)
				> m_clockSpeed) {
				logger.info(
					new SimulationWarningEvent(
						m_currentStep,
						stopEvent.getTime(),
						m_realEndTime,
						LOCAL_SYNC_ERROR));
			}
			//close all active ressources
			terminate();
		}
	}

	/** Initialize the simulation controller */
	private void init() {
		/* Initialize the clock */
		m_clock = new SimClock(this);
	}

	/**
	 * Configure the simulation node
	 * 
	 * @param p_event
	 *            Initialization Event
	 */
	private void configure(InitEvent p_event) {
		/* Reinitialize the logging package */
		PropertyConfigurator.configure(p_event.logging);

		/* Set the static simulation parameters */
		m_config = p_event.simConfig;

		m_simLayer.setTransmissionRange(m_config.getTransmissionRange());
		m_simLayer.setTransmissionQuality(m_config.getTransmissionQuality());

		m_finalCurrentStep =
			p_event.endTime.getTime() - p_event.startTime.getTime();

		/* Initialize and schedule the clock */
		m_clockSpeed = p_event.clockSpeed;
		m_clock.setSpeed(m_clockSpeed);
		new Timer().schedule(new StartClock(), p_event.startTime);

		/* Schedule the end of the simulation */
		new Timer().schedule(new StopSimulation(), p_event.endTime);
	}

	/** Creates a TCP Connection to the Main Simulator */
	private void createConnection() {
		int nb_attempts = NUMBER_OF_ATTEMPTS;

		/* Initialize the remote server information */
		String m_inetAddress = m_simLayer.getSimulatorAddress();
		int m_port = m_simLayer.getSimulatorPort();

		while (nb_attempts > 0) {
			try {
				Thread.sleep(NEXT_ATTEMPT);
				m_socket = new Socket(m_inetAddress, m_port);
				if (m_socket != null)
					break;
			} catch (Exception e) {
				nb_attempts--;
			}
		}

		if (m_socket != null) {
			logger.debug(CONNECT_SUCCESS);
		} else {
			logger.debug(CONNECT_FAIL);
			terminate();
		}
	}

	/** Sends the node's simulation information via TCP */
	private void sendLoggingInfo() {
		BufferedWriter bw;
		BufferedReader br;
		int ch;

		try {
			br =
				new BufferedReader(
					new FileReader(
						m_config.getSimulationLog() + m_config.getNodeID()));
			bw =
				new BufferedWriter(
					new OutputStreamWriter(m_socket.getOutputStream()));

			while ((ch = br.read()) >= 0) {
				bw.write(ch);
			}
			bw.flush();
			br.close();
			bw.close();
			logger.debug("Success transfer" + m_config.getNodeID());
		} catch (FileNotFoundException file_e) {
			logger.debug("Fail transfer" + m_config.getNodeID(), file_e);
		} catch (IOException io_e) {
			logger.debug("fail transfer" + m_config.getNodeID(), io_e);
		}

	}

	/** Deletes the simulation info of the node */
	private void deleteLoggingInfo() {
		File simulationLog =
			new File(m_config.getSimulationLog() + m_config.getNodeID());
		try {
			if (simulationLog.exists())
				simulationLog.delete();
		} catch (SecurityException se) {
			logger.debug(se.getMessage(), se);
		}
	}

	/** TimerTask which starts the simulation process */
	class StartClock extends TimerTask {
		public void run() {
			/* Create an array of intializable events */
			IUpdateSimulationLayerEvent events[] =
				new IUpdateSimulationLayerEvent[] {
					m_config.getSchedule().getScheduleAt(0),
					m_config.getMobility().getPositionAt(0)};

			/* Update the Simulation layer */
			m_simLayer.update(events);
			/* Mark the simulation layer as ready for simulation */
			m_simLayer.setReady();
			/* Register the real start time */
			m_realStartTime = System.currentTimeMillis();
			/* Start the simulation clock */
			m_clock.startup();
		}
	}

	/** TimerTask which register the theoretical end of the simulation */
	class StopSimulation extends TimerTask {
		public void run() {
			m_realEndTime = System.currentTimeMillis();
		}
	}

	/** String constants needed for logging */
	private static final String CONNECT_SUCCESS =
		"Connection has been established";
	private static final String CONNECT_FAIL =
		"Connection attempt to the simulation server has failed";
	private static final String INIT_SUCCESS = "Initialization succeeded";
	private static final String INIT_FAIL = "Initialization failed";
	private static final String SOCKET_ERROR = "Socket error occured";
	private static final String SOCKET_CLOSE_ERROR = "Socket closure failed";
	private static final String CAST_ERROR = "Cast error occured!";
	private static final String GLOBAL_SYNC_ERROR =
		"Precision between real endtime at server and real endtime at node exceeds the allowed value!";
	private static final String LOCAL_SYNC_ERROR =
		"Precision between theoretical endtime at node and real endtime at node exceeds the allowed value!";
}