/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:20 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

import ch.epfl.lsr.adhoc.simulator.util.exceptions.SimulationException;

/**
 * Represents a Distributed exception.
 * This exception is launched by the simulation during distribution of the 
 * information
 *  
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:20 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class DistributionException extends SimulationException {
	public static final String codeRevision =
		"$Revision: 1.4 $ $Date: 2004/06/05 17:14:20 $ Author: Boris Danev and Aurelien Frossard";

	/** Private constructor.*/
	public DistributionException(String p_newErrorCode, Throwable p_exception) {
		super(p_newErrorCode, p_exception);
	}
}