/*
 * $Revision: 1.10 $
 * 
 * $Date: 2004/07/13 13:32:36 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import java.util.Date;

import ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer_EventView;

/**
 * The class represents an End simulation event
 * 
 * @version $Revision: 1.10 $ $Date: 2004/07/13 13:32:36 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class EndSimulationEvent
	extends AbstractLoggableEvent
	implements IUpdateSimulationLayerEvent {
	public static final String codeRevision =
		"$Revision: 1.10 $ $Date: 2004/07/13 13:32:36 $ Author: Boris Danev and Aurelien Frossard";

	/** Stores the discrete simulation time */
	private long m_currentSystemTime;

	/** Default constructor */
	public EndSimulationEvent(long p_simTime, long p_currentSystemTime) {
		super(p_simTime);
		m_currentSystemTime = p_currentSystemTime;
	}

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
	String[][] parameters() {
		String[][] params = new String[2][1];
		params[0][0] = "currentSystemTime";
		params[1][0] = new Date(m_currentSystemTime).toString();
		return params;
	}

	/** @see IUpdateSimulationLayerEvent#applyOn(ISimulationLayer_EventView)*/
	public void applyOn(ISimulationLayer_EventView p_simLayer) {
		p_simLayer.setInactive();
	}
}
