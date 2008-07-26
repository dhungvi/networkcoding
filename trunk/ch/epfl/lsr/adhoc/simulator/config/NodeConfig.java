/*
 * $Revision: 1.12 $
 * 
 * $Date: 2004/06/05 17:14:22 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.config;

import ch.epfl.lsr.adhoc.simulator.mobility.IMobilityPattern;
import ch.epfl.lsr.adhoc.simulator.mobility.ITimeSchedulePattern;

/**
 * Stores config information for one node
 * 
 * @version $Revision: 1.12 $ $Date: 2004/06/05 17:14:22 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class NodeConfig extends AbstractConfig {
    public static final String codeRevision =
        "$Revision: 1.12 $ $Date: 2004/06/05 17:14:22 $ Author: Boris Danev and Aurelien Frossard";

    private int m_nodeID;
    private String m_configInFile;
    private String m_configOutFile;
    private String m_errorLog;
    private String m_simulationLog;
    private double m_transmissionRange;
    private double m_transmissionQuality;
    private IMobilityPattern m_mobility;
    private ITimeSchedulePattern m_schedule;

    public int getNodeID() {
        return m_nodeID;
    }
    public String getConfigInFile() {
        return m_configInFile;
    }
    public String getConfigOutFile() {
        return m_configOutFile;
    }
    public String getErrorLog() {
        return m_errorLog;
    }
    public String getSimulationLog() {
        return m_simulationLog;
    }
    public IMobilityPattern getMobility() {
        return m_mobility;
    }
    public double getTransmissionRange() {
        return m_transmissionRange;
    }
    public double getTransmissionQuality() {
        return m_transmissionQuality;
    }
    public ITimeSchedulePattern getSchedule() {
        return m_schedule;
    }

    public void setNodeID(int p_nodeID) {
        m_nodeID = p_nodeID;
    }
    public void setConfigInFile(String p_configInFile) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_configInFile = p_configInFile;
    }
    public void setConfigOutFile(String p_configOutFile) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_configOutFile = p_configOutFile;
    }
    public void setErrorLog(String p_errorLog) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_errorLog = p_errorLog;

    }
    public void setSimulationLog(String p_simulationLog) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_simulationLog = p_simulationLog;
    }
    public void setTransmissionQuality(double p_transmissionQuality) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        if (StrictMath.abs(p_transmissionQuality) > 1)
            throw new IllegalArgumentException("arg quality must be <= 1");
        m_transmissionQuality = p_transmissionQuality;
    }
    public void setTransmissionRange(double p_transmissionRange) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_transmissionRange = p_transmissionRange;
    }
    public void setMobility(IMobilityPattern p_mobility) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_mobility = p_mobility;
    }

    public void setSchedule(ITimeSchedulePattern p_schedule) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_schedule = p_schedule;
    }

    /** Returns a String representation of this object. */
    public String toString() {
        StringBuffer sb = new StringBuffer("NodeConfig[");
        sb.append("nodeID=");
        sb.append(m_nodeID);
        sb.append(",configInFile=");
        sb.append(m_configInFile);
        sb.append(",configOutFile=");
        sb.append(m_configOutFile);
        sb.append(",errorLog=");
        sb.append(m_errorLog);
        sb.append(",simulationLog=");
        sb.append(m_simulationLog);
        sb.append(",nodeRange=");
        sb.append(m_transmissionRange);
        sb.append(",nodeQuality=");
        sb.append(m_transmissionQuality);
        sb.append(",schedule=");
        sb.append(m_schedule.toString());
        sb.append("]");
        return (sb.toString());
    }
}
