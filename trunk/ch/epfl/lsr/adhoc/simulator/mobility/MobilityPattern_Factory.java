/*
 * $Workfile$
 *
 * $Revision: 1.9 $
 * 
 * $Date: 2004/08/05 18:45:40 $
 *
 * $Archive$
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.mobility;

import java.util.ArrayList;
import java.util.Enumeration;

/**Translates a mobility file (IParsedMobility) into a more practical form :
 * IMobility Pattern
 * 
 * @version $Revision: 1.9 $ $Date: 2004/08/05 18:45:40 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class MobilityPattern_Factory {
    public static final String codeRevision =
        "$Revision: 1.9 $ $Date: 2004/08/05 18:45:40 $ Author: Boris Danev and Aurelien Frossard";

    public static IMobilityPattern getMobilityPattern(
        IParsedMobility p_mobility,
        int p_fileTimeUnit) {
        return (IMobilityPattern)new MobilityPattern(
            makeSegmentList(p_mobility),
            p_fileTimeUnit);
    }

    /** The heart of the class, translates a mobility file (IParsedMobility)
     * into a more practical form a list of segments, which is then encapsulated
     * in an IMobilityPattern instance.*/
    private static Segment[] makeSegmentList(IParsedMobility p_mobility) {
        ArrayList segmentList = new ArrayList();
        boolean loop = true;
        Enumeration enumeration = p_mobility.enumeration();
        Segment previousSeg;
        double[] currentRC, nextRC; //RouteChanges from the parsed file
        double[] startPoint;
        double[] speed;
        double[] destVect;
        double destVectLength;
        double currentRC_speed;
        double startTime, endTime, currentRC_time, nextRC_time;

        if (!enumeration.hasMoreElements()) {
            throw new RuntimeException("Fatal Error : no information provided");
        }
        /* read the initial position and init some the variables */
        currentRC = (double[])enumeration.nextElement();
        currentRC_speed = 0;
        currentRC_time = 0;
        startPoint = new double[] { currentRC[1], currentRC[2] };
        startTime = 0;
        endTime = -1;
        speed = new double[] { 0, 0 };
        /* if no RouteChange provided, the node just stays forever 
         * at the same place */
        if (!enumeration.hasMoreElements()) {
            endTime = Double.MAX_VALUE;
            segmentList.add(
                new Segment(
                    startTime,
                    endTime,
                    startPoint[0],
                    startPoint[1],
                    speed[0],
                    speed[1]));
            return (Segment[])segmentList.toArray(
                new Segment[segmentList.size()]);
        }
        /* read the next element */
        currentRC = (double[])enumeration.nextElement();
        currentRC_time = currentRC[0];
        currentRC_speed = currentRC[3];
        /* if the first move is a pause */
        if (currentRC_time != 0) {
            endTime = currentRC_time;
            speed = new double[] { 0, 0 };
            previousSeg =
                new Segment(
                    startTime,
                    endTime,
                    startPoint[0],
                    startPoint[1],
                    speed[0],
                    speed[1]);
            segmentList.add(previousSeg);
            startTime = previousSeg.endTime();
        }
        /* main loop */
        while (loop) {
            speed = new double[2];
            /* computes the destination as a vector from current position
             * and computes the lenght of this vector */
            destVect =
                new double[] {
                    currentRC[1] - startPoint[0],
                    currentRC[2] - startPoint[1] };
            destVectLength =
                Math.sqrt(
                    destVect[0] * destVect[0] + destVect[1] * destVect[1]);
            /* normalise destVect and multiply by speed to obtain
             * the speed vector */
            speed[0] = currentRC_speed * destVect[0] / destVectLength;
            speed[1] = currentRC_speed * destVect[1] / destVectLength;
            /* if (destVect == 0 || destVectLength == 0) speed = 0; */
            speed[0] =
                (Double.isNaN(speed[0]) || Double.isInfinite(speed[0]))
                    ? 0
                    : speed[0];
            speed[1] =
                (Double.isNaN(speed[1]) || Double.isInfinite(speed[1]))
                    ? 0
                    : speed[1];
            /* compute endTime, ie time when destination is reached */
            endTime = startTime + (destVectLength / currentRC_speed);
            /* read the next element and put it into nextRC
             * used to compute endTime if route is interrupted */
            if (enumeration.hasMoreElements()) {
                nextRC = (double[])enumeration.nextElement();
                nextRC_time = nextRC[0];
                /* if route interrupted or endTime is infinite or NaN */
                endTime =
                    (endTime < nextRC_time
                        || Double.isInfinite(endTime)
                        || Double.isNaN(endTime))
                        ? nextRC_time
                        : endTime;
                /* prepare data for next iteration */
                currentRC = nextRC;
                currentRC_speed = currentRC[3];
                currentRC_time = currentRC[0];
            }
            else {
                /* no more iteration, last element reached */
                loop = false;
                /* if endTime is NaN or infinite */
                endTime =
                    (Double.isInfinite(endTime) || Double.isNaN(endTime))
                        ? Double.MAX_VALUE
                        : endTime;
            }
            /* end of iteration : new segment with the computed data
             * add it to the segment list */
            previousSeg =
                new Segment(
                    startTime,
                    endTime,
                    startPoint[0],
                    startPoint[1],
                    speed[0],
                    speed[1]);
            segmentList.add(previousSeg);
            //prepare data for the next iteration
            startTime = previousSeg.endTime();
            startPoint = previousSeg.endPoint();
        }
        /* no more route changes, compute and build the last segment */
        endTime = Double.MAX_VALUE;
        speed = new double[] { 0, 0 };
        previousSeg =
            new Segment(
                startTime,
                endTime,
                startPoint[0],
                startPoint[1],
                speed[0],
                speed[1]);
        segmentList.add(previousSeg);
        return (Segment[])segmentList.toArray(new Segment[segmentList.size()]);
    }
}
