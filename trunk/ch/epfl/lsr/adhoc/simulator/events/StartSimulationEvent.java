/*
 * $Revision: 1.9 $
 * 
 * $Date: 2004/07/13 13:32:35 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import java.util.Date;

/**
 * The class represents a Start simulation event
 * 
 * @version $Revision: 1.9 $ $Date: 2004/07/13 13:32:35 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class StartSimulationEvent extends AbstractLoggableEvent {
    public static final String codeRevision =
        "$Revision: 1.9 $ $Date: 2004/07/13 13:32:35 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores the discrete simulation time */
    private long m_currentSystemTime;

    /** Default constructor */
    public StartSimulationEvent(long p_simTime, long p_currentSystemTime) {
        super(p_simTime);
        m_currentSystemTime = p_currentSystemTime;
    }

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
    String[][] parameters() {
        String[][] params = new String[2][1];
        params[0][0] = "currentSystemTime";
        params[1][0] = new Date(m_currentSystemTime).toString();
        return params;
    }
}
