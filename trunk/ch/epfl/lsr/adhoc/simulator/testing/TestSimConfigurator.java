/*
 * Created on Dec 28, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.testing;

import ch.epfl.lsr.adhoc.simulator.config.*;
import ch.epfl.lsr.adhoc.simulator.modules.ContextKey;
import ch.epfl.lsr.adhoc.simulator.modules.SimulatorContext;
import ch.epfl.lsr.adhoc.simulator.util.xml.ParserException;
import ch.epfl.lsr.adhoc.simulator.util.xml.SimConfigurator;

/**
 * @author Boris
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestSimConfigurator {

    private static SimulatorContext ms = SimulatorContext.getInstance();
    public static void main(String[] args) {
        try {
            /* Configure the simulator */
            SimConfigurator.configure(args[0], ms);
            System.out.println(
                ((SimulationConfig)ms.get(ContextKey.SIMULATION_KEY))
                    .toString());
			System.out.println(
							((DaemonsConfig)ms.get(ContextKey.DEAMONS_KEY))
								.toString());
            System.out.println(
                ((LayerConfig)ms.get(ContextKey.LAYER_KEY)).toString());
            System.out.println(
                ((NetworkConfig)ms.get(ContextKey.NETWORK_KEY)).toString());
        }
        catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
