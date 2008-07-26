/*
 * $Revision: 1.3 $
 * 
 * $Date: 2004/06/13 14:21:21 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.layer;

/**
 * SimulationLayer's interface for the events
 * 
 * @version $Revision: 1.3 $ $Date: 2004/06/13 14:21:21 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface ISimulationLayer_EventView {
    /** called by a NewLocationEvent */
    public void setPosition(double x, double y);

    /** called by a NewScheduleEvent */
    public void setSchedule(boolean schedule);

    /** This method provides the current simulation time */
    public void setSimulationTime(long simulationTime);
    
	/** This method makes the node inactive */
    public void setInactive();
}