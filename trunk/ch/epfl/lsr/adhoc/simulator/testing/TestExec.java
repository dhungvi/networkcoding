/*
 * Created on Mar 28, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.testing;
/**
 * @author aurelien
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class TestExec {
	public static void main(String[] args) throws Exception {
		//		FileWriter out = new FileWriter("");
		Process subProcess = Runtime
				.getRuntime()
				.exec(
						"java -jar simulator\\testbench\\MANETSimulation.jar -c:simulator\\testbench\\config\\nodes\\out\\00.xml");
	}
}