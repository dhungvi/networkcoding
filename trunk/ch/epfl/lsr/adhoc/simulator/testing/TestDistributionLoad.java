/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.testing;

/**
 * @author Boris
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestDistributionLoad {

	public static void main(String[] args) {
		getLoadDistr(1,3);
		getLoadDistr(2,3);
		getLoadDistr(3,4);
		getLoadDistr(10, 10);
		getLoadDistr(10,2);
		getLoadDistr(10, 3);
	}
	
	/** 
	 * Creates a possible load balancing between daemons 
	 * @return an array with the number of nodes per daemon 
	 */
	private static void getLoadDistr(int nbDaemons, int nbNodes) {
		int factor = nbNodes / nbDaemons;
		int result[] = new int[nbDaemons];
		
		System.out.println("Factor: " + factor);
		
		for (int i = 0; i < result.length; i++) {
			if (nbNodes > 0) {
				if (factor == 0) {
					result[i] = 1;
					nbNodes--;
				} else {
					result[i] = (i < nbDaemons - 1 ? factor : nbNodes);
					nbNodes -= factor;					
				}
			}
		}
		
		for (int k = 0; k < result.length; k ++){
			System.out.print(result[k] + " ");
		}
		System.out.println("");
	}
}
