/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/05 17:14:23 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.config;

/**
 * Abstract class for all configuration objects. 
 * It is designed to be subclassed.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/06/05 17:14:23 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public abstract class AbstractConfig implements java.io.Serializable {
    public static final String codeRevision =
        "$Revision: 1.5 $ $Date: 2004/06/05 17:14:23 $ Author: Boris Danev and Aurelien Frossard";

    /** Provides an error message for configured objects */
    protected static final String ERROR_MESG = "Configuration is frozen";

    /** Freeze the configuration of this component. */
    public void freeze() {
        configured = true;
    }
    protected boolean configured = false;
}
