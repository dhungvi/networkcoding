/*
 * $Revision: 1.12 $
 * 
 * $Date: 2004/06/20 13:01:15 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

import java.util.Date;
import java.util.Properties;

import ch.epfl.lsr.adhoc.simulator.config.NetworkConfig;
import ch.epfl.lsr.adhoc.simulator.config.NodeConfig;
import ch.epfl.lsr.adhoc.simulator.config.SimulationConfig;
import ch.epfl.lsr.adhoc.simulator.events.InitEvent;
import ch.epfl.lsr.adhoc.simulator.events.StopEvent;
import ch.epfl.lsr.adhoc.simulator.util.logging.DefaultLoggerConfig;

/**
 * This module constructs nodes based on the simulation configuration
 * 
 * @version $Revision: 1.12 $ $Date: 2004/06/20 13:01:15 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class NodeConstructor {
    public static final String codeRevision =
        "$Revision: 1.12 $ $Date: 2004/06/20 13:01:15 $ Author: Boris Danev and Aurelien Frossard";

    /** Reference to the simulation configuration information */
    private SimulatorContext m_context;
    /** Is the constructor correctly configured */
    private boolean m_configured = false;
    /** Stores the start simulation time  */
    private long m_startTime = 0;
    /** Stores the end time of simulation */
    private long m_endTime = 0;

    /** Is the time set? */
    private boolean m_startTimeSet = false;
    private boolean m_endTimeSet = false;

    private static final NodeConstructor constructor = new NodeConstructor();
    private NodeConstructor() {}

    /** Getter for the only one instance of the NodeConstructor */
    public static NodeConstructor getInstance() {
        return constructor;
    }

    /** 
     * Sets the context in the scope of the constructor
     * @param p_context SimulatorContext 
     */
    public static void setContext(SimulatorContext p_context) {
        if (!constructor.m_configured) {
            constructor.m_context = p_context;
            constructor.m_configured = true;
        }
    }

    /**
     * Sets the start time in the scope of the constructor
     * @param p_startTime the start time of the simulation
     * NB! Should be set before any call of createNode()
     */
    public void setStartTime(long p_startTime) {
        m_startTime = p_startTime;
        m_startTimeSet = true;
    }

    /**
     * Sets the end time in the scope of the constructor
     * NB! Should be set before any call of destroyNode()
     * @param p_endTime : the start time of the simulation
     */
    public void setEndTime(long p_endTime) {
        m_endTime = p_endTime;
        m_endTimeSet = true;
    }

    /** 
     * Creates a simulation node
     * @param p_id indicates the id of the node (p_id >= 1)
     * @return a InitEvent whihc represents a node init information
     */
    public InitEvent createNode(int p_id) {
        if (!m_startTimeSet) {
            throw new IllegalStateException(TIME_SET_ERROR);
        }

        SimulationConfig sc =
            (SimulationConfig)m_context.get(ContextKey.SIMULATION_KEY);
        NetworkConfig nc = (NetworkConfig)m_context.get(ContextKey.NETWORK_KEY);
        NodeConfig simConfig = nc.getNodeConfig(p_id - 1);

        /* Set simulation configuration for the node  */
        Properties p =
            DefaultLoggerConfig.getLoggerConfig(
                simConfig.getErrorLog() + simConfig.getNodeID(),
                simConfig.getSimulationLog() + simConfig.getNodeID());
        return new InitEvent(
            new Date(m_startTime),
            new Date(m_startTime + sc.getDuration()),
            sc.getClockSpeed(),
            p,
            simConfig);
    }

    /** 
     * Destroys a simulation node
     * @return A StopEvent
     */
    public StopEvent destroyNode() {
        if (!m_endTimeSet) {
            throw new IllegalStateException(TIME_SET_ERROR);
        }
        return new StopEvent(m_endTime);
    }

    /** ERROR indicating that the time has not been set */
    private static final String TIME_SET_ERROR = "Time has not been set";
}