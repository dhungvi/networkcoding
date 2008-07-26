/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/20 13:01:11 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.layer;

import ch.epfl.lsr.adhoc.simulator.events.IUpdateSimulationLayerEvent;

/**
 * SimulationLayer's interface for the controller component
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/20 13:01:11 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface ISimulationLayer_ControllerView {
    /**
     * Method inspired from observer/observable pattern. It updates the layer
     * with a given event
     * 
     * @param event
     *            which will update the simulation layer
     */
    public void update(IUpdateSimulationLayerEvent[] events);

    /**
     * This method allows the terminate the simulation of the framework A
     * typical usage is to recursively terminate all layers running
     */
    public void terminate();

    /** called by a NewScheduleEvent */
    public void setReady();

    /** Gets the simulator address */
    public String getSimulatorAddress();

    /** Gets the simulator port */
    public int getSimulatorPort();

    /** called by a NewTransmissionRangeEvent */
    public void setTransmissionRange(double range);

    /** called by a NewTransmissionQualityEvent */
    public void setTransmissionQuality(double quality);
}