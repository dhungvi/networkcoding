package ch.epfl.lsr.adhoc.simulator.testing;

import javax.xml.parsers.FactoryConfigurationError;

import ch.epfl.lsr.adhoc.simulator.ManetSimulator;
import ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer;
import ch.epfl.lsr.adhoc.simulator.util.xml.ParserException;

/**
 * Tests the communication between:
 * SimulationLayer - SimulationController - SimulationClock
 */
public class TestSimulatorControllerCom {
    public static void main(String args[]) {

        try {
			ManetSimulator ms = new ManetSimulator(args);
			//ms.run();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Simulator started!");
        SimulationLayer sLayer = new SimulationLayer();
        sLayer.startup();
        System.out.println("All started!");
    }
}
