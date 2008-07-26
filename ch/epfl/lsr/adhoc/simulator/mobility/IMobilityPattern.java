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
package ch.epfl.lsr.adhoc.simulator.mobility;

import java.io.Serializable;

import ch.epfl.lsr.adhoc.simulator.events.NewLocationEvent;

/**
 * Generates NewLocationEvents from an abstract path description
 * An abstract path description consists only of the route changes,
 * something like "at 16h00, new destination (x,y), new speed v" 
 * 
 * @version $Revision: 1.5 $ $Date: 2004/06/05 17:14:16 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface IMobilityPattern extends Serializable {
    public static final String codeRevision =
        "$Revision: 1.5 $ $Date: 2004/06/05 17:14:16 $ Author: Boris Danev and Aurelien Frossard";

    /** Creates a new location event for the current simulation time */
    public NewLocationEvent getPositionAt(long p_simulationTime);
}
