/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/05 17:14:17 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer_EventView;

/**
 * Interface for the simulation layer
 * 
 * @version $Revision: 1.5 $ $Date: 2004/06/05 17:14:17 $
 * @author Author: Boris Danev and Aurelien Frossard
 */

public interface IUpdateSimulationLayerEvent {
    public static final String codeRevision = "$Revision: 1.5 $ $Date: 2004/06/05 17:14:17 $ Author: Boris Danev and Aurelien Frossard";

    public void applyOn(ISimulationLayer_EventView p_simLayer);
}