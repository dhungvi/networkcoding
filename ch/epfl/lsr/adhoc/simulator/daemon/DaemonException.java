/*
 * $Revision: 1.1 $
 * 
 * $Date: 2004/06/13 14:21:19 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import ch.epfl.lsr.adhoc.simulator.util.exceptions.SimulationException;

/**
 * Represents a daemon exception.
 *  
 * @version $Revision: 1.1 $ $Date: 2004/06/13 14:21:19 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class DaemonException extends SimulationException {
	public static final String codeRevision =
		"$Revision: 1.1 $ $Date: 2004/06/13 14:21:19 $ Author: Boris Danev and Aurelien Frossard";

	/** Private constructor.*/
	public DaemonException(String p_newErrorCode, Throwable p_exception) {
		super(p_newErrorCode, p_exception);
	}
}
