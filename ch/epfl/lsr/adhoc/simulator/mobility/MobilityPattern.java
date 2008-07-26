/*
 * $Workfile$
 *
 * $Revision: 1.11 $
 * 
 * $Date: 2004/07/13 13:32:34 $
 *
 * $Archive$
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.mobility;

import java.io.PrintWriter;
import java.io.Serializable;

import ch.epfl.lsr.adhoc.simulator.events.NewLocationEvent;

/**
 * Generates NewLocationEvents from an abstract path description
 * An abstract path description consists only of the route changes,
 * something like "at 16h00, new destination (x,y), new speed v" 
 * 
 * @version $Revision: 1.11 $ $Date: 2004/07/13 13:32:34 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public final class MobilityPattern implements IMobilityPattern, Serializable {
    public static final String codeRevision =
        "$Revision: 1.11 $ $Date: 2004/07/13 13:32:34 $ Author: Boris Danev and Aurelien Frossard";

    /** Used to store the segments. The segments are
     * sorted in ascending order of the start time. End time of a
     * segment equals start time of the following segment.
     * For any interval of time a segment exists  */
    private Segment[] m_segments;

    /** current position in the list */
    private int m_currentIndex;
    /** segment at the current index */
    private Segment m_currentSegment;
    /** time parameter of the elast call to getNext */
    private double m_lastTime;
    /** used for time conversion, see constructor for more details */
    private double m_file_timeUnit;

    /**
     * @param <b>p_segments</b> : an array of segments, see MobilityPattern_Factory
     * @param <b>p_fileTimeUnit</b> : base for converting time : the simulator
     * works with milliseconds (using type long) but the mobility file might have
     * a different unit. Let x be the unit used in the mobility file, then the
     * following equation must be satisfied : x * p_fileTimeUnit == millisecond
     * @throws IllegalArgumentException if (p_fileTimeUnit < 1)
     * or if (p_segments == null)
     * */
    MobilityPattern(Segment[] p_segments, int p_fileTimeUnit) {
        if (p_fileTimeUnit < 1)
            throw new IllegalArgumentException("p_fileTimeUnit < 1, should be >= 1");
        if (p_segments == null)
            throw new IllegalArgumentException("p_segments cannot be null");
        m_segments = p_segments;
        m_file_timeUnit = p_fileTimeUnit;
        m_currentIndex = 0;
        m_lastTime = 0;
        m_currentSegment = m_segments[m_currentIndex];
    }

    /** Returns a NewLocationEvent for the given time.<p>
     * 
     * @param p_simulationTime : used to compute the newLocation, in milliseconds
     * @throws IllegalStateException : Let two successive calls,
     * say call1(time1) and call2(time2).
     * call1 happens before call2, but time1 > time2.
     * Then call2 will throw IllegalStateException
     *  */
    public NewLocationEvent getPositionAt(long p_simulationTime)
        throws IllegalStateException {
        double time = ((double)p_simulationTime) / m_file_timeUnit;
        double[] newPos;
        if (time < m_lastTime)
            throw new IllegalStateException(
                "simulationTime < timeOfLastCall,"
                    + " cannot go back into the past!");
        m_lastTime = time;
        /* if currentSegment is not the right one, look in the following ones */
        while (!m_currentSegment.isCorrectSegment(time)) {
            m_currentIndex++;
            m_currentSegment = m_segments[m_currentIndex];
        }
        newPos = m_currentSegment.getPosition(time);
        return NewLocationEvent.getInstance(p_simulationTime, newPos[0], newPos[1]);
    }

    public void printSegment(PrintWriter out) {
        out.println(0 + "\t" + m_segments[0].toString());
        for (int i = 1; i < m_segments.length; i++) {
            out.println(i + "\t" + m_segments[i].toString());
        }
    }
}
