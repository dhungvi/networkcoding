/*
 * Created on Dec 18, 2003
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

import ch.epfl.lsr.adhoc.simulator.events.NewLocationEvent;
import ch.epfl.lsr.adhoc.simulator.mobility.IMobilityPattern;
import ch.epfl.lsr.adhoc.simulator.mobility.IParsedMobility;
import ch.epfl.lsr.adhoc.simulator.mobility.MobilityPattern;
import ch.epfl.lsr.adhoc.simulator.mobility.MobilityPattern_Factory;
import ch.epfl.lsr.adhoc.simulator.util.ns2.MobilityParser;

/**
 * @author aurelien
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestMobility {

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        PrintWriter out =
            new PrintWriter(new BufferedWriter(new FileWriter(args[1])));
        PrintWriter debug =
            new PrintWriter(
                new BufferedWriter(new FileWriter(args[1] + "_debug.csv")));
        IParsedMobility[] nodes = (new MobilityParser(in).parse());
        IMobilityPattern current;
        NewLocationEvent pos;
        in.close();
        in = null;
        for (int i = 0; i < nodes.length; i++) {
            out.println("\n\nNode " + i);
            out.println("-------------------");
            current =
                MobilityPattern_Factory.getMobilityPattern(nodes[i], 1000);
            ((MobilityPattern)current).printSegment(out);
            for (int j = 0; j < 1000; j++) {
                pos = current.getPositionAt(j * 1000);
                out.println(j + ",   " + pos.getX() + ",   " + pos.getY());
                debug.println(pos.getX() + " " + pos.getY());
            }
        }
        debug.flush();
        out.flush();
        debug.close();
        out.close();
        out = null;
    }
}
