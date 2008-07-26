/*
 * $Revision: 1.10 $
 * 
 * $Date: 2004/06/05 17:14:22 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.config;

/**
 * Stores config information for the simulation clock speend and 
 * simulation duration 
 * 
 * @version $Revision: 1.10 $ $Date: 2004/06/05 17:14:22 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimulationConfig extends AbstractConfig {
    public static final String codeRevision =
        "$Revision: 1.10 $ $Date: 2004/06/05 17:14:22 $ Author: Boris Danev and Aurelien Frossard";

    /** Reference to the object's state */
    private String m_servIP;
    private int m_servPort;
    private int m_servTimeout;
    private int m_servQueue;

    private long m_clockSpeed;
    private long m_simDuration;
    private String m_outputDir;

    public void setServIP(String p_servIP) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_servIP = p_servIP;
    }

    public void setServPort(int p_servPort) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_servPort = p_servPort;
    }

    public void setServTimeout(int p_servTimeout) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_servTimeout = p_servTimeout;
    }

    public void setServQueue(int p_servQueue) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_servQueue = p_servQueue;
    }

    public void setClockSpeed(long p_clockSpeed) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_clockSpeed = p_clockSpeed;
    }

    public void setDuration(long p_simDuration) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_simDuration = p_simDuration;
    }

    public void setOutputDir(String p_outputDir) {
        if (configured)
            throw new IllegalStateException(ERROR_MESG);
        m_outputDir = p_outputDir;
    }
    public String getServIP() {
        return m_servIP;
    }

    public int getServPort() {
        return m_servPort;
    }

    public int getServTimeout() {
        return m_servTimeout;
    }

    public int getServQueue() {
        return m_servQueue;
    }

    public long getClockSpeed() {
        return m_clockSpeed;
    }

    public long getDuration() {
        return m_simDuration;
    }

    public String getOutputDir() {
        return m_outputDir;
    }
    /** Returns a String representation of this object. */
    public String toString() {
        StringBuffer sb = new StringBuffer("SimulationConfig[");
        sb.append("ip=");
        sb.append(m_servIP);
        sb.append(",port=");
        sb.append(m_servPort);
        sb.append(",timeout=");
        sb.append(m_servTimeout);
        sb.append(",queue=");
        sb.append(m_servQueue);
        sb.append(",clock=");
        sb.append(m_clockSpeed);
        sb.append(",duration=");
        sb.append(m_simDuration);
        sb.append(",outputDir=");
        sb.append(m_outputDir);
        sb.append("]");
        return (sb.toString());
    }
}
