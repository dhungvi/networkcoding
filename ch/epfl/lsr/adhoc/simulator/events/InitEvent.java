/*
 * $Revision: 1.12 $
 * 
 * $Date: 2004/06/05 17:14:18 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import ch.epfl.lsr.adhoc.simulator.config.NodeConfig;

/**
 * THe class represent a simulation Init event
 * 
 * @version $Revision: 1.12 $ $Date: 2004/06/05 17:14:18 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class InitEvent implements Serializable {
    public static final String codeRevision =
        "$Revision: 1.12 $ $Date: 2004/06/05 17:14:18 $ Author: Boris Danev and Aurelien Frossard";

    /** Stores the start time of the simulation */
    public Date startTime;
    /** Stores the end time of the simulation */
    public Date endTime;
    /** Stores the speed of the simulation in milliseconds */
    public long clockSpeed;
    /** Stores the logging configuration information */
    public Properties logging;
    /** Stores the mobility pattern and other configuration info for this node */
    public NodeConfig simConfig;

    /** Default constructor */
    public InitEvent(
        Date sTime,
        Date eTime,
        long cSpeed,
        Properties log,
        NodeConfig sc) {
        startTime = sTime;
        endTime = eTime;
        clockSpeed = cSpeed;
        logging = log;
        simConfig = sc;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("InitEvent[");
        sb.append("startTime=");
        sb.append(startTime);
        sb.append(",endTime=");
        sb.append(endTime);
        sb.append(",clockSpeed=");
        sb.append(clockSpeed);
        sb.append(",log props=");
        sb.append(logging.toString());
        sb.append(",node config=");
        sb.append(simConfig.toString());
        sb.append("]");
        return (sb.toString());
    }
}
