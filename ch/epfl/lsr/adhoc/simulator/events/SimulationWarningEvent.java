/*
 * Created on Jun 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.events;

/**
 * @author Boris
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SimulationWarningEvent extends AbstractLoggableEvent {

	/** Stores the discrete simulation time */
	private long m_theoreticalTime;
	private long m_realTime;
	private String m_description;

	/** Default constructor */
	public SimulationWarningEvent(
		long p_simTime,
		long p_theoreticalTime,
		long p_realTime,
		String p_description) {
		super(p_simTime);
		m_theoreticalTime = p_theoreticalTime;
		m_realTime = p_theoreticalTime;
		m_description = p_description;
	}

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
	String[][] parameters() {
		String[][] params = new String[2][4];
		params[0][0] = "theoreticalEndSimulationTime";
		params[1][0] = Long.toString(m_theoreticalTime);
		params[0][1] = "realEndSimulationTime";
		params[1][1] = Long.toString(m_realTime);
		params[0][2] = "deviationError";
		params[1][2] = Long.toString(Math.abs(m_theoreticalTime - m_realTime));
		params[0][3] = "description";
		params[1][3] = m_description;
		return params;
	}
}
