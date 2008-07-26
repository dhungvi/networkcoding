/*
 * $Revision: 1.52 $
 * 
 * $Date: 2005/09/21 22:10:49 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.layer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.apache.log4j.PropertyConfigurator;

import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer;
import ch.epfl.lsr.adhoc.communicationslayer.SendMessageFailedException;
import ch.epfl.lsr.adhoc.mhm.Message;
import ch.epfl.lsr.adhoc.mhm.MessagePool;
import ch.epfl.lsr.adhoc.mhm.UnknownMessageTypeException;
import ch.epfl.lsr.adhoc.simulator.controller.ISimController;
import ch.epfl.lsr.adhoc.simulator.controller.SimController;
import ch.epfl.lsr.adhoc.simulator.events.IUpdateSimulationLayerEvent;
import ch.epfl.lsr.adhoc.simulator.events.MessageReceivedEvent;
import ch.epfl.lsr.adhoc.simulator.events.MessageSentEvent;
import ch.epfl.lsr.adhoc.simulator.events.StartSimulationEvent;
import ch.epfl.lsr.adhoc.simulator.util.logging.DefaultLoggerConfig;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * This class enables the framework in simulation mode
 * <p> <b>Important note to developers</b>
 * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> have the same behaviour
 * as <b>ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast</b>
 * regarding return values, thrown exceptions and data writtern on System.out.
 * If for some reason this behaviour changes or is not needed anymore,
 * the changes should be propagated to this class as well.
 * 
 * @version $Revision: 1.52 $ $Date: 2005/09/21 22:10:49 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public final class SimulationLayer
	extends AsynchronousLayer
	implements ISimulationLayer_ControllerView, ISimulationLayer_EventView {

	public static final String codeRevision =
		"$Revision: 1.52 $ $Date: 2005/09/21 22:10:49 $ Author: Boris Danev and Aurelien Frossard";

	/** Logger for the simulation layer */
	private static final SimLogger logger =
		new SimLogger(SimulationLayer.class);

	/** Contains all the variables regarding simulation */
	private final SimVariables m_simVars = new SimulationLayer.SimVariables();

	/** Contains all the variables regarding communication */
	private final CommunicationVariables m_comVars =
		new CommunicationVariables();

	/** The default constructor */
	public SimulationLayer() {
		super();
		init_simGlobal();
	}

	/** Initialize global for the layer resources (simulator specific) */
	private void init_simGlobal() {
		/* Default initializer for log4j logging package */
		PropertyConfigurator.configure(
			DefaultLoggerConfig.getLoggerConfig("error.log", "simulation.log"));

		/* Initialize the controller of the simulation */
		m_simVars.simController = new SimController(this);
	}

	/**
	 * Initializes the layer.
	 * <p>
	 * This method reads the following configuration variables:
	 * <ul>
	 * <li><b>layers.AsyncMulticast.port </b>- The port to use for the
	 * multicast communication</li>
	 * <li><b>layers.AsyncMulticast.multicastgroup </b>- The multicast address
	 * to use</li>
	 * <li><b>layers.AsyncMulticast.networkInterface </b>- The address of the
	 * physical network adapter to use (optional, useful if a computer has
	 * several network interfaces)</li>
	 * <li><b>layers.AsyncMulticast.maxBufferSize </b>- The number of bytes
	 * reserved for sending/receiving messages (the maximum length of network
	 * messages)</li>
	 * </ul>
	 *
	 */
	public void initialize(FrancRuntime runtime) {
		m_comVars.messagePool = runtime.getMessagePool();
		m_comVars.runtime = runtime;
		int nodeID;
		try {
			/* Get the multicast port number */
			m_comVars.multicastPort =
				Integer.parseInt(runtime.returnParamValue("port"));

			/* Get the multicast group parameter */
			String mGroup = runtime.returnParamValue("multicastgroup");
			m_comVars.multicastGroup = InetAddress.getByName(mGroup);

			/* Get the network interface */
			m_comVars.networkInterface =
				runtime.returnParamValue("networkInterface");

			/* Get the max buffer size of the socket */
			m_comVars.maxSize =
				Integer.parseInt(runtime.returnParamValue("maxBufferSize"));

			/* Get the simulator port and ip address */
			m_comVars.simulatorServer = runtime.returnParamValue("server");
			m_comVars.simulatorPort =
				Integer.parseInt(runtime.returnParamValue("server_port"));
			/* Get the node id proposal */
			nodeID = Integer.parseInt(runtime.returnParamValue("nodeID"));
			m_comVars.nodeID = nodeID;
		} catch (Exception ex) {
			logger.fatal(ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}

		/* create the IO buffers */
		m_comVars.bufTX = new byte[m_comVars.maxSize];
		m_comVars.bufRX = new byte[m_comVars.maxSize];

		/*
		 * wrap byteBuffers around the newly created buffers for easier RW
		 * operations
		 */
		m_comVars.byteBufferTX = ByteBuffer.wrap(m_comVars.bufTX);
		m_comVars.byteBufferTX.order(ByteOrder.BIG_ENDIAN);
		m_comVars.byteBufferRX = ByteBuffer.wrap(m_comVars.bufRX);
		m_comVars.byteBufferRX.order(ByteOrder.BIG_ENDIAN);

		/* Init sim controller */
		m_simVars.simController.startup(nodeID);
	}

	/** Starts running the Simulation layer */
	public void startup() {
		/* Starts the network interface */
		startNetworkInterface();

		/* Starts the layer thread */
		start();
	}

	private void startNetworkInterface() {
		try {
			m_comVars.socket = new MulticastSocket(m_comVars.multicastPort);
			if (m_comVars.networkInterface != null) {
				m_comVars.socket.setInterface(
					InetAddress.getByName(m_comVars.networkInterface));
			} else
				throw new IllegalStateException("No  network interface defined");
			m_comVars.socket.joinGroup(m_comVars.multicastGroup);
		} catch (Exception e) {
			logger.fatal("Error starting network interface", e);
			throw new RuntimeException(e.getMessage());
		}
		m_comVars.packetTX =
			new DatagramPacket(
				m_comVars.bufTX,
				m_comVars.maxSize,
				m_comVars.multicastGroup,
				m_comVars.multicastPort);
		m_comVars.packetRX =
			new DatagramPacket(m_comVars.bufRX, m_comVars.maxSize);
		logger.debug(
			"Network interface started: MulticastSocket Host="
				+ m_comVars.networkInterface
				+ " Port="
				+ m_comVars.multicastPort
				+ " MulticastGroup="
				+ m_comVars.multicastGroup);
	}

	/**
	 * The Simulation layer is notified of any changes of its environment using
	 * the update method
	 */
	public void update(IUpdateSimulationLayerEvent[] events) {
		synchronized (m_simVars) {
			for (int i = 0; i < events.length; i++) {
				events[i].applyOn(this);
				//Events are logged if the flag isLoggable is set
				if (m_simVars.isLoggable) {
					logger.info(events[i]);
					if (!m_simVars.isActiv) {
						m_simVars.isLoggable = false;
					}
				}
			}
		}
	}

	/** Overloads the default toString() method */
	public String toString() {
		return getClass().getName();
	}

	/**
	 * Get the next message on the network.
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast#getNetworkMessage()
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer#getNetworkMessage()
     * <p> <b>Important note to developers</b>
     * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> have the same behaviour
     * as in <b>ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast</b>
     * regarding return values, thrown exceptions and data writtern on System.out.
     * If for some reason this behaviour changes or is not needed anymore,
     * the changes should be propagated to this class as well.
	 */
	protected Message getNetworkMessage() {
		int msg_length;
		byte[] fromMsg_simFooterHead = new byte[m_simVars.footer_HEAD.length];
		double fromMsg_xCoordinate, current_xCoordinate;
		double fromMsg_yCoordinate, current_yCoordinate;
		double fromMsg_transmissionRange, current_transmissionRange;
		double current_transmissionQuality;
		long current_simulationTime;
		double x_dst, y_dst;
		StringBuffer log = new StringBuffer();
		boolean drop;
        boolean current_schedule;

		if (!m_simVars.configured) {
			m_simVars.trigger.block();
		}
		synchronized (m_simVars) {
			if (!m_simVars.isActiv)
				return null;
		}
		try {
			m_comVars.socket.receive(m_comVars.packetRX);
			synchronized (m_simVars) {
				current_transmissionRange = m_simVars.transmissionRange;
				current_xCoordinate = m_simVars.xCoordinate;
				current_yCoordinate = m_simVars.yCoordinate;
				current_transmissionQuality = m_simVars.transmissionQuality;
				current_simulationTime = m_simVars.simulationTime;
                current_schedule = m_simVars.schedule;
			}

			/* This part reads the sim footer */
			msg_length = m_comVars.packetRX.getLength();
			m_comVars.byteBufferRX.position(
				msg_length - m_simVars.footer_LENGTH);
			m_comVars.byteBufferRX.get(
				fromMsg_simFooterHead,
				0,
				m_simVars.footer_HEAD.length);
			if (!Arrays.equals(m_simVars.footer_HEAD, fromMsg_simFooterHead)) {
				logger.debug(LOG_NONSIMMSG);
			} else {
				fromMsg_xCoordinate = m_comVars.byteBufferRX.getDouble();
				fromMsg_yCoordinate = m_comVars.byteBufferRX.getDouble();
				fromMsg_transmissionRange = m_comVars.byteBufferRX.getDouble();
				msg_length = msg_length - m_simVars.footer_LENGTH;
				
					Message msg =
						m_comVars.messagePool.createMessage(
							m_comVars.packetRX.getData(),
							(short) (msg_length));
					m_comVars.packetRX.setLength(m_comVars.maxSize);

					/*
					 * Tells whether the a message should be droped or not,
					 * depending on the provided params and on the transmission
					 * quality (which is a probability).
                     * If m_simVars.schedule == false, all messages are dropped
					 */
					x_dst = fromMsg_xCoordinate - current_xCoordinate;
					y_dst = fromMsg_yCoordinate - current_yCoordinate;
					drop =
					    (!current_schedule) ||
                        ((Math.sqrt(x_dst * x_dst + y_dst * y_dst)
                                                   > fromMsg_transmissionRange)
                            && (Math.random() < current_transmissionQuality));
					/* logging */
					logger.info(
						MessageReceivedEvent.getInstance(
							current_simulationTime,
							msg,
							fromMsg_xCoordinate,
							fromMsg_yCoordinate,
							fromMsg_transmissionRange,
							current_xCoordinate,
							current_yCoordinate,
							current_transmissionRange,
							current_transmissionQuality,
							drop));

					if (drop) {
						m_comVars.messagePool.freeMessage(msg);
						return null;
					} else
						return msg;
			}
        } catch (UnknownMessageTypeException umte) {
            logger.debug(
                "Unknown Message type: "
                    + ((int) umte.getUnknownType()),
                umte);
            /* This line is only for reproducing the behaviour of
             * ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast#getNetworkMessage() */
            System.out.println(">>> Unknown Message type: " + ((int)umte.getUnknownType()));
		} catch (IOException e) { //if receive fails
			logger.debug("Error reading socket: " + e.getMessage(), e);
            /* This line is only for reproducing the behaviour of
             * ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast#getNetworkMessage() */
            throw new RuntimeException("Error receiving message: " + e.getMessage());
		}
		return null;
	}

	/**
	 * NodeID -- currently from the config file
	 */
	public long proposeNodeID() {
		return m_comVars.nodeID;
	}

	/**
	 * Send message over the network
     * @return number of bytes in the message,
     * as ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast#sendMessage
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast#getNetworkMessage()
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer#getNetworkMessage()
     * <p> <b>Important note to developers</b>
     * <p> The methods <b>getNetworkMessage</b> and <b>sendMessage</b> have the same behaviour
     * as in <b>ch.epfl.lsr.adhoc.communicationslayer.AsyncMulticast</b>
     * regarding return values, thrown exceptions and data writtern on System.out.
     * If for some reason this behaviour changes or is not needed anymore,
     * the changes should be propagated to this class as well.
	 */
	public synchronized int sendMessage(Message msg)  throws SendMessageFailedException {
		int length = 0;

		if (!m_simVars.configured) {
			m_simVars.trigger.block();
		}
		/*
		 * This case occurs only when the simulation has finished or when the
		 * node is not available
		 */
		synchronized (m_simVars) {
			if (!m_simVars.isActiv)
				return length;
		}
		if (msg == null) {
            IllegalArgumentException ex =
                new IllegalArgumentException("The message cannot be null");
			logger.debug(LOG_MSGSENDERROR, ex);
            throw ex;
		} else {
			try {
				if (msg.getNextHop() == -1) {
					throw new SendMessageFailedException(
						msg,
						"Next hop not defined.");
				}
				if (msg.getDstNode() == -1) {
					throw new SendMessageFailedException(
						msg,
						"Destination not defined.");
				}
				if (msg.getSrcNode() == -1
					|| msg.getSrcNode() == m_comVars.nodeID) {
					msg.setSrcNode(m_comVars.nodeID);
					msg.setSequenceNumber(m_comVars.seqNumber);
					m_comVars.seqNumber = ((m_comVars.seqNumber + 1) & 0xFFFF);
				}
				msg.setPreviousHop(m_comVars.nodeID);
				length = msg.getByteArray(m_comVars.bufTX);

				/*
				 * append the flollowing float values to the message :
				 * m_sim_xCoordinate, m_sim_yCoordinate,
				 * m_sim_transmissionRange.
				 */
				m_comVars.byteBufferTX.position(length);
				synchronized (m_simVars) {
                    if (!m_simVars.schedule){ //message is dropped and not sent
                        logger.info(
                                    MessageSentEvent.getInstance(
                                        m_simVars.simulationTime,
                                        msg,
                                        m_simVars.xCoordinate,
                                        m_simVars.yCoordinate,
                                        true,
                                        m_simVars.transmissionRange));
                    }else{ //message is sent
                        m_comVars
						    .byteBufferTX
						    .put(m_simVars.footer_HEAD)
						    .putDouble(m_simVars.xCoordinate)
						    .putDouble(m_simVars.yCoordinate)
						    .putDouble(m_simVars.transmissionRange);
                        m_comVars.packetTX
                            .setLength(length + m_simVars.footer_LENGTH);
                        
                            m_comVars.socket.send(m_comVars.packetTX);
                            logger.info(
                                MessageSentEvent
                                    .getInstance(
                                        m_simVars.simulationTime,
                                        msg,
                                        m_simVars.xCoordinate,
                                        m_simVars.yCoordinate,
                                        false,
                                        m_simVars.transmissionRange));
                    }
				}
            } catch (IOException e) {
                logger.debug(LOG_MSGSENDERROR, e);
                throw new SendMessageFailedException(
                              msg,
                              "Sending the message failed: "
                                + e.getMessage());
            }catch (SendMessageFailedException smfe){
                logger.debug(LOG_MSGSENDERROR, smfe);
                throw smfe;
			} finally {
				m_comVars.messagePool.freeMessage(msg);
			}
		}
		return length;
	}

	/** Terminates all threads in the framework */
	public void terminate() {
		if (m_comVars.socket != null)
			m_comVars.socket.close();
		logger.debug("Datagram socket closed");
		logger.debug("************SIMULATION LAYER TERMINATED***********");
	}

	/** Getter for the simulator IP address */
	public String getSimulatorAddress() {
		return m_comVars.simulatorServer;
	}

	/** Getter for the simulator port */
	public int getSimulatorPort() {
		return m_comVars.simulatorPort;
	}

	/** called by a NewLocationEvent */
	public void setPosition(double x, double y) {
		m_simVars.xCoordinate = x;
		m_simVars.yCoordinate = y;
	}

	/** called by a NewTransmissionRangeEvent */
	public void setTransmissionRange(double p_range) {
		m_simVars.transmissionRange = p_range;
	}

	/** called by a NewTransmissionQualityEvent */
	public void setTransmissionQuality(double quality) {
		m_simVars.transmissionQuality = quality;
	}

	/** called by a NewScheduleEvent */
	public void setSchedule(boolean p_schedule) {
		m_simVars.schedule = p_schedule;
	}

	/** called by controller to indicate that all layer is well configured */
	public void setReady() {
		logger.info(
			new StartSimulationEvent(
				m_simVars.simulationTime,
				System.currentTimeMillis()));
		synchronized (m_simVars) {
			m_simVars.configured = true;
			m_simVars.trigger.allow();
		}
	}

	/** called by the Controller */
	public void setInactive() {
		m_simVars.isActiv = false;
	}

	/** Sets the current simulation time in the layer */
	public void setSimulationTime(long p_simulationTime) {
		m_simVars.simulationTime = p_simulationTime;
	}

	/** Contains all the variables regarding communication */
	static private class CommunicationVariables {
		/**
		 * Network interface for Multicast (needed if the computer has several
		 * physical netwok interfaces)
		 */
		String networkInterface;
		/** The socket used for sending/receiving IP multicast packets. */
		MulticastSocket socket;
		/**
		 * The InetAdress object which contains the multicast group (class D IP
		 * adress)
		 */
		InetAddress multicastGroup;
		/** contains the multicast port */
		int multicastPort;
		/** maximum size of the socket buffer */
		int maxSize;
		/** IP address of the simulator server */
		String simulatorServer;
		/** Port of the simulator server */
		int simulatorPort;
		/** the socket buffer for transmission */
		byte[] bufTX;
		/** the socket buffer for reception */
		byte[] bufRX;
		/** The datagram packet object for transmission */
		DatagramPacket packetTX;
		/** The datagram packet object for reception */
		DatagramPacket packetRX;
		/** A reference to the message pool of this node */
		MessagePool messagePool;
		/** A reference to the FRANC runtime for this node */
		FrancRuntime runtime;
		/** The unique node id for this node (obtained from CommunicationsLayer) */
		long nodeID;
		/** The sequence number for the next message to be sent */
		int seqNumber = 0;

		/** Used for easier acces to bufTX */
		ByteBuffer byteBufferTX;
		/** Used for easier acces to bufRX */
		ByteBuffer byteBufferRX;
	}

	/** Contains all the variables regarding simulation */
	static private class SimVariables {
		SimVariables() {
			super();
			try {
				footer_HEAD = "SimParams".getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
				logger.fatal(
					"Init SimulationLayer failed : Encoding of the simFooter",
					e1);
				throw new IllegalStateException(
					"Init SimulationLayer failed : Encoding of the simFooter"
						+ e1.getMessage());
			}
			footer_PARAM_COUNT = 3;
			footer_LENGTH = footer_HEAD.length + footer_PARAM_COUNT * 8;
		}

		/** x coordinate of the node */
		double xCoordinate;
		/** y coordinate of the node */
		double yCoordinate;
		/** transmission range of the node */
		double transmissionRange;
		/** how much packets are droped by the node, in terms of probabilty */
		double transmissionQuality;
		/** stores the current state of the node */
		boolean schedule;
		/** stores the simulation time */
		long simulationTime;
		/** is the simulation process configured */
		boolean configured = false;
		/** Allows to log messages */
		boolean isLoggable = true;
		/** Is the layer active */
		boolean isActiv = true;
		/** The sim footer begins by this byte[], DO NOT MODIFY AT RUN TIME */
		byte[] footer_HEAD;
		/** Number of params in the sim footer. DO NOT MODIFY AT RUN TIME */
		int footer_PARAM_COUNT;
		/**
		 * size in bytes of the footer appended to each message the node sends.
		 * Used in appendSimfooter. DO NOT MODIFY AT RUN TIME
		 */
		int footer_LENGTH;
		/**
		 * Reference to the controller of the simulation. DO NOT MODIFY AT RUN
		 * TIME
		 */
		ISimController simController;
		/**
		 * reference to the simulation trigger, used for blocking the framework
		 * unitl the simulation layer is fully configured and opertational
		 */
		Trigger trigger = new Trigger();
	}

	/** Used for temporarily blocking access to a method */
	static private class Trigger {
		private boolean blocking = true;

		/** unblocks every blocked thread */
		synchronized void allow() {
			blocking = false;
			notifyAll();
		}

		/** blocks calling threads */
		synchronized void block() {
			while (blocking) {
				try {
					wait();
				} catch (InterruptedException ie) {
					logger.debug("Trigger wait was interrupted", ie);
				}
			}
		}
	}

	/* Messages for the logger */
	private static final String LOG_NONSIMMSG =
		"non-simulation message received, discarded";
	private static final String LOG_MSGSENDERROR = "Could not send message";
	private static final String LOG_SIMPARAMSNOTCONFIGURED =
		"Simulation parameters not yet configured";
}
