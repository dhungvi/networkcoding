/*
 * $Workfile$
 *
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:15 $
 *
 * $Archive$
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.mobility;

/**
 * Any mobility parser should comply with this interface.
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:15 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface IMobilityParser {
    public static final String codeRevision =
        "$Revision: 1.4 $ $Date: 2004/06/05 17:14:15 $ Author: Boris Danev and Aurelien Frossard";

    /** 
    * parses the file and returns an array of IParsedMobility.
    * one array index for each node. Returns null if an error occured */
    public IParsedMobility[] parse();

}