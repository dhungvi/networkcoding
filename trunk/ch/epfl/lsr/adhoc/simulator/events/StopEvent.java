/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/05 17:14:16 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

/**
 * The class represents a Stop simulation event
 * 
 * @version $Revision: 1.5 $ $Date: 2004/06/05 17:14:16 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class StopEvent implements java.io.Serializable {
    public static final String codeRevision =
        "$Revision: 1.5 $ $Date: 2004/06/05 17:14:16 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores the time when the event was fired */
    private long m_time;

    /** 
     * Default constructor
     * @param p_time indicates the time at which the event has been fired
     */
    public StopEvent(long p_time) {
        m_time = p_time;
    }

    public long getTime() {
        return m_time;
    }
}
