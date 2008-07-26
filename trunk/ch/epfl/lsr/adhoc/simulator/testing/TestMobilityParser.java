/*
 * Created on Dec 14, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

import ch.epfl.lsr.adhoc.simulator.mobility.IParsedMobility;
import ch.epfl.lsr.adhoc.simulator.util.ns2.MobilityParser;

/**
 * @author aurelien
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestMobilityParser {

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        PrintWriter out =
            new PrintWriter(new BufferedWriter(new FileWriter(args[1])));
        IParsedMobility[] nodes = new MobilityParser(in).parse();
        Enumeration enumeration;
        double[] rc;
        for (int i = 0; i < nodes.length; i++) {
            out.println(
                "Node " + i + "---------------------------------------\n");
            enumeration = nodes[i].enumeration();
            while (enumeration.hasMoreElements()) {
                rc = (double[])enumeration.nextElement();
                out.println(
                    "$ns_ at "
                        + rc[0]
                        + " \"$node_("
                        + i
                        + ") setdest "
                        + rc[1]
                        + " "
                        + rc[2]
                        + " "
                        + rc[3]
                        + "\"\n");
            }
        }
        in.close();
        out.close();
    }
}
