package ch.epfl.lsr.adhoc.simulator.testing;

import ch.epfl.lsr.adhoc.simulator.layer.SimulationLayer;

/**
 * Tests the communication between:
 * SimulationLayer - SimulationController - SimulationClock
 */
public class TestController {
    public static void main(String args[]) {

        SimulationLayer sLayer = new SimulationLayer();
        sLayer.startup();
    }
}
