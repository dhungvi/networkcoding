package ch.epfl.lsr.adhoc.simulator.testing;
/*
 * Created on Jun 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.util.*;
/**
 * @author Boris
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestDate {

	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		System.out.println(s);
		Date d = new Date(s);
		double k = (double)s / 1000.0;
		
		System.out.println(Math.round(k)*1000);
		System.out.println(((long)Math.ceil((double)s / 1000.0))*1000);
	}
}
