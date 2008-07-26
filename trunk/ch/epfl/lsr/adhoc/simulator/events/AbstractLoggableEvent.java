/*
 * $Revision: 1.13 $
 * 
 * $Date: 2004/08/05 08:07:59 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.events;

import java.util.Date;

/**
 * Abstract class describing a loggable simulation event.
 * 
 * @version $Revision: 1.13 $ $Date: 2004/08/05 08:07:59 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public abstract class AbstractLoggableEvent implements java.io.Serializable {
    public static final String codeRevision = "$Revision: 1.13 $ $Date: 2004/08/05 08:07:59 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores the simulation time at which the event has been fired */
    protected long m_simTime;
    /** Stores the system time at which the event has been fired */
    protected long m_systemTime;

    AbstractLoggableEvent(long p_simTime) {
        m_simTime = p_simTime;
        m_systemTime = System.currentTimeMillis();
    }

    /** the simulation time at which the event has been fired */
    public long getSimTime() {
        return m_simTime;
    }

    /** the system time at which the event has been fired */
    public long getSystemTime() {
        return m_systemTime;
    }

    /** sets the member variables. Records the szstem time of the call */
    protected void setAll(long p_simTime){
        m_simTime = p_simTime;
        m_systemTime = System.currentTimeMillis();
    }
    
    /**
     * A String[2][x] is returned. Element [0][x] is the parameter name, element
     * [1][x] is the parameter value. Elements [0][0] and [0][1] correspond to
     * the name of the event and the number of parameters respectively.
     */
    abstract String[][] parameters();

    /**XML-like format used for logging the event:<p>
     * <pre>
     * &lt;SimulationEvent <p>
     *     name="</pre><b>this.class</b><pre>" <p>
     *     simTime="</pre><b>current simulation time</b><pre>" <p>
     *     systemTime="</pre><b>current system time</b><pre>"&gt;<p>
     * 
     *           &lt;param name=</pre><b>param name</b><pre>&gt;</pre><b>param value</b><pre>&lt;/param&gt;<p>
     *           &lt;param name=</pre><b>param name</b><pre>&gt;</pre><b>param value</b><pre>&lt;/param&gt;<p>
     *           ...any number of param nodes, depending on the event...
     * &lt;/SimulationEvent&gt;<p>
     * </pre>*/
    public String toString() {
        StringBuffer str = new StringBuffer();
        String[][] params = parameters();
        str.append(TITLETAG_OPEN).append(
                NAMETAG + QUOTE + this.getClass().getName() + QUOTE).append(
                SIMTIME_TAG + QUOTE + m_simTime + QUOTE);

        str.append(SYSTEMTIME_TAG + QUOTE + (new Date(m_systemTime)).toString()
                + QUOTE);
        str.append(CLOSETAG);
        for (int i = 0; i < params[0].length; i++) {
            str.append("\n" + INDENT + PARAMTAG_OPEN);
            str.append(params[0][i] + CLOSETAG);
            str.append(params[1][i] + PARAMTAG_CLOSE);
        }
        str.append("\n" + TITLETAG_CLOSE);
        return str.toString();
    }

    /* Variables used by the toString() method */
    private static final String TITLETAG_OPEN = "<SimulationEvent";
    private static final String TITLETAG_CLOSE = "</SimulationEvent>";
    private static final String NAMETAG = " name=";
    private static final String SIMTIME_TAG = " simTime=";
    private static final String SYSTEMTIME_TAG = " systemTime=";
    private static final String QUOTE = "\"";
    private static final String PARAMTAG_OPEN = "<param name=";
    private static final String PARAMTAG_CLOSE = "</param>";
    private static final String CLOSETAG = ">";
    private static final String INDENT = "    ";
}