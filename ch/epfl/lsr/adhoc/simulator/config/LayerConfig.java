/*
 * $Revision: 1.7 $
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
 * Simulation Layer configuration module
 * 
 * @version $Revision: 1.7 $ $Date: 2004/06/05 17:14:23 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class LayerConfig extends AbstractConfig {
    public static final String codeRevision =
        "$Revision: 1.7 $ $Date: 2004/06/05 17:14:23 $ Author: Boris Danev and Aurelien Frossard";

    private String m_name;
    private String m_class;

    public void setName(String p_name) {
        m_name = p_name;
    }
    public void setClass(String p_class) {
        m_class = p_class;
    }
    public String getName() {
        return m_name;
    }
    public String getClassName() {
        return m_class;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("LayerConfig[");
        sb.append("name=");
        sb.append(m_name);
        sb.append(",class=");
        sb.append(m_class);
        sb.append("]");
        return (sb.toString());
    }
}
