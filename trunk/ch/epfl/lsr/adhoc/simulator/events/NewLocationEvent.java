/*
 * $Revision: 1.11 $
 * 
 * $Date: 2004/07/13 13:32:35 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.simulator.layer.ISimulationLayer_EventView;

/**PATTERN : Singleton
 * 
 * @version $Revision: 1.11 $ $Date: 2004/07/13 13:32:35 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public final class NewLocationEvent
    extends AbstractLoggableEvent
    implements IUpdateSimulationLayerEvent {
    public static final String codeRevision =
        "$Revision: 1.11 $ $Date: 2004/07/13 13:32:35 $ Author: Boris Danev and Aurelien Frossard";

    /** Singleton pattern : unique instance allowed */
    static private NewLocationEvent instance = null;
    
    private double x;
    private double y;

    /** Singlton pattern : private constructor */
    private NewLocationEvent(long p_simTime, double p_x, double p_y) {
        super(p_simTime);
        x = p_x;
        y = p_y;
    }
    
    /** @see AbstractLoggableEvent#setAll(long) */
    private void setAll(long p_simTime, double p_x, double p_y){
        super.setAll(p_simTime);
        x = p_x;
        y = p_y;
    }
    
    /**
     * Singleton pattern : returns the unique instance of this class and sets
     * its content
     */
    static public NewLocationEvent getInstance(long p_simTime, double p_x, double p_y){
        if (instance == null){
            instance = new NewLocationEvent(p_simTime, p_x, p_y);
        }else{
            instance.setAll(p_simTime, p_x, p_y);
        }
        return instance;
    }
    
    /**@see IUpdateSimulationLayerEvent#applyOn(ISimulationLayer_EventView) */
    public void applyOn(ISimulationLayer_EventView p_simLayer) {
        p_simLayer.setSimulationTime(m_simTime);
        p_simLayer.setPosition(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
    public String[][] parameters() {
        String[][] params = new String[2][2];
        params[0][0] = "x";
        params[1][0] = Double.toString(x);
        params[0][1] = "y";
        params[1][1] = Double.toString(y);
        return params;
    }
}
