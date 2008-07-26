/*
 * $Revision: 1.12 $
 * 
 * $Date: 2004/07/13 13:32:34 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.mobility;

import ch.epfl.lsr.adhoc.simulator.events.NewScheduleEvent;

/**
 * The pattern for the different time schedules of a simulation node
 * 
 * @version $Revision: 1.12 $ $Date: 2004/07/13 13:32:34 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class TimeSchedulePattern implements ITimeSchedulePattern {
    public static final String codeRevision =
        "$Revision: 1.12 $ $Date: 2004/07/13 13:32:34 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores times to go offline */
    private long m_off[];
    /** Stores times to go online */
    private long m_on[];

    /** Current event argument */
    private boolean m_state = true;

    /** For optimizing performance */
    private int currOnId = 0;
    private int currOffId = 0;

    /**
     * Default constructor
     * @param p_off offline times 
     * @param p_on online times 
     */
    public TimeSchedulePattern(long p_off[], long p_on[]) {
        m_off = p_off;
        m_on = p_on;
    }

    /** 
     * Get a new possible schedule for the node
     * @param p_simulationTime
     * @return a NewScheduleEvent or null if no event to provide
     */
    public NewScheduleEvent getScheduleAt(long p_simulationTime) {
        NewScheduleEvent r_event =
            NewScheduleEvent.getInstance(p_simulationTime, m_state);
        int index;

        if (currOffId <= m_off.length - 1
            && m_off[currOffId] == p_simulationTime) {
            r_event = NewScheduleEvent.getInstance(p_simulationTime, false);
            currOffId++;
            m_state = false;
        }
        else if (
            currOnId <= m_on.length - 1
                && m_on[currOnId] == p_simulationTime) {
            r_event = NewScheduleEvent.getInstance(p_simulationTime, true);
            currOnId++;
            m_state = true;
        }
        return r_event;
    }

    /** Prints the node schedule characteristics */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < m_off.length; i++)
            sb.append(m_off[i] + ",");
        sb.append("|");
        for (int i = 0; i < m_on.length; i++)
            sb.append(m_on[i] + ",");
        return sb.toString();
    }

    /** Test the schedule pattern with a small instance */
    public static void main(String argc[]) {
        long off[] = new long[] { 0, 4 };
        long on[] = new long[] { 2, 5 };
        TimeSchedulePattern t = new TimeSchedulePattern(off, on);
        for (int i = 0; i < 6; i++) {
            NewScheduleEvent e = t.getScheduleAt(i);
            System.out.println(e.toString());
        }
    }
}
