/*
 * $Workfile$
 *
 * $Revision: 1.6 $
 * 
 * $Date: 2004/06/05 17:14:25 $
 *
 * $Archive$
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.util.ns2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import ch.epfl.lsr.adhoc.simulator.mobility.IParsedMobility;

/**
 * A class used for communication between the mobility parser
 * and the MobilityPattern_Generator.
 * The parser should produce an istance of this class for each node.
 * Thoses instances are then used by MobilityPattern_Generator to
 * build a MobilityPattern for each node.
 *   
 * @version $Revision: 1.6 $ $Date: 2004/06/05 17:14:25 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
class ParsedMobility implements IParsedMobility {
    public static final String codeRevision =
        "$Revision: 1.6 $ $Date: 2004/06/05 17:14:25 $ Author: Boris Danev and Aurelien Frossard";

    private boolean m_freezed = false;
    private boolean m_startPosCalled = false;
    private ArrayList routeChanges;

    ParsedMobility() {
        routeChanges = new ArrayList();
    }

    /** appends a route change.<p>
    * <b>Important note :</b><p>
    * @throws IllegalStateException once freeze() has been called or
    * if startPos has not been called yet*/
    public void add(
        double timeOfChange,
        double destination_x,
        double destination_y,
        double speed)
        throws IllegalStateException {
        if (m_freezed || (!m_startPosCalled))
            throw new IllegalStateException(
                "Cannot be called after freeze()" + " or before startPos()");
        routeChanges.add(
            new double[] { timeOfChange, destination_x, destination_y, speed });
    }

    /**
    * returns an enumeration of the routeChanges. The elements are double[4]<p>
    * [0] = timeOfChange<p>
    * [1] = destinationPoint_X<p>
    * [2] = destinationPoint_Y<p>
    * [3] = speed<p>
    * The very first element is the start position : <p>
    * [0] = 0<p>
    * [1] = startPos_X<p>
    * [2] = startPos_Y<p>
    * [3] = 0<p>
    * @throws IllegalStateException if freeze() has not been called
    * (same if startPos() has  not been called) */
    public Enumeration enumeration() throws IllegalStateException {
        if (!m_freezed)
            throw new IllegalStateException(
                "cannot be called" + " before the freeze() method");
        return Collections.enumeration(routeChanges);
    }

    /** Set the initial position
     * @throws IllegalStateException if called twice */
    public void startPos(double p_x, double p_y) throws IllegalStateException {
        if (m_startPosCalled)
            throw new IllegalStateException("cannot be called twice");
        m_startPosCalled = true;
        add(0, p_x, p_y, 0);
    }

    /** After calling freeze(), add() throws IllegalStateException, but
    * enumeration can be used. */
    public void freeze() {
        m_freezed = true;
    }

}