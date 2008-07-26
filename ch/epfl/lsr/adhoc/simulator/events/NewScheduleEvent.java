/*
 * $Revision: 1.10 $
 * 
 * $Date: 2004/07/13 13:32:35 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer_EventView;

/**PATTERN : Singleton
 * This event represents a new scheduled state of the simulation node
 * Currently there are to possible schedules: online and offline
 * 
 * @version $Revision: 1.10 $ $Date: 2004/07/13 13:32:35 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class NewScheduleEvent
    extends AbstractLoggableEvent
    implements IUpdateSimulationLayerEvent {
    public static final String codeRevision =
        "$Revision: 1.10 $ $Date: 2004/07/13 13:32:35 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores the schedule of the event */
    private boolean m_schedule;
    
    /** Singleton pattern : unique instance allowed */
    static private NewScheduleEvent instance = null;

    /**Singlton pattern : private constructor 
     * @param p_simTime represents the simulation time
     *  @param p_schedule represents off = false, on = true */
    private NewScheduleEvent(long p_simTime, boolean p_schedule) {
        super(p_simTime);
        m_schedule = p_schedule;
    }
    
    /** @see AbstractLoggableEvent#setAll(long) */
    private void setAll(long p_simTime, boolean p_schedule){
        super.setAll(p_simTime);
        m_schedule = p_schedule;
    }
    
    /** Singleton pattern : returns the unique instance of this class and sets its content */
    static public NewScheduleEvent getInstance(long p_simTime, boolean p_schedule){
        if (instance == null){
            instance = new NewScheduleEvent(p_simTime, p_schedule);
        }else{
            instance.setAll(p_simTime, p_schedule);
        }
        return instance;
    }

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
    public String[][] parameters() {
        String[][] params = new String[2][1];
        params[0][0] = "schedule";
        params[1][0] = "" + m_schedule;
        return params;
    }

    /**
     * @see ch.epfl.lsr.adhoc.simulator.events.IUpdateSimulationLayerEvent#applyOn(ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer)
     */
    public void applyOn(ISimulationLayer_EventView p_simLayer) {
        p_simLayer.setSimulationTime(m_simTime);
        p_simLayer.setSchedule(m_schedule);
    }
}
