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
package ch.epfl.lsr.adhoc.simulator.util.xml;

import ch.epfl.lsr.adhoc.simulator.util.exceptions.SimulationException;

/**
 * This kind of exception is used inside the simulator parser code
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:20 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class ParserException extends SimulationException {

    public static final String codeRevision =
        "$Revision: 1.4 $ $Date: 2004/06/05 17:14:20 $ Author: Boris Danev and Aurelien Frossard";

    public static final String PARSE_ERROR = "XML_PARSER_ERROR";

    /** The controller doesn't found the appropriate event */
    public static ParserException parseError(Throwable p_throwable) {
        return new ParserException(PARSE_ERROR, p_throwable);
    }

    /** The controller doesn't found the appropriate event */
    public static ParserException parseError(String p_message) {
        return new ParserException(PARSE_ERROR + ":" + p_message, null);
    }

    /** Private constructor.*/
    private ParserException(String p_newErrorCode, Throwable p_exception) {
        super(p_newErrorCode, p_exception);
    }
}