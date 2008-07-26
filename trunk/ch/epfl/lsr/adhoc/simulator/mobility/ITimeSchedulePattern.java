/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:16 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.mobility;

import java.io.Serializable;

import ch.epfl.lsr.adhoc.simulator.events.NewScheduleEvent;

/**
 * Interface for all possible node scheduling events 
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:16 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface ITimeSchedulePattern extends Serializable {
    public static final String codeRevision =
        "$Revision: 1.4 $ $Date: 2004/06/05 17:14:16 $ Author: Boris Danev and Aurelien Frossard";

    /** Get the scheduled state of the node depending on the time */
    public NewScheduleEvent getScheduleAt(long p_SimulationTime);
}
