/*
 * $Revision: 1.27 $
 * 
 * $Date: 2004/08/19 16:39:46 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 * 
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology All Rights
 * Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.util.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import ch.epfl.lsr.adhoc.simulator.config.DaemonsConfig;
import ch.epfl.lsr.adhoc.simulator.config.LayerConfig;
import ch.epfl.lsr.adhoc.simulator.config.NetworkConfig;
import ch.epfl.lsr.adhoc.simulator.config.NodeConfig;
import ch.epfl.lsr.adhoc.simulator.config.SimulationConfig;
import ch.epfl.lsr.adhoc.simulator.mobility.IParsedMobility;
import ch.epfl.lsr.adhoc.simulator.mobility.MobilityPattern_Factory;
import ch.epfl.lsr.adhoc.simulator.mobility.TimeSchedulePattern;
import ch.epfl.lsr.adhoc.simulator.modules.ContextKey;
import ch.epfl.lsr.adhoc.simulator.modules.SimulatorContext;
import ch.epfl.lsr.adhoc.simulator.util.ns2.MobilityParser;
import ch.epfl.lsr.adhoc.tools.ParserXml;

/**
 * The class configures the simulation process using an XML File as input
 * 
 * @version $Revision: 1.27 $ $Date: 2004/08/19 16:39:46 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimConfigurator {
	public static final String codeRevision =
		"$Revision: 1.27 $ $Date: 2004/08/19 16:39:46 $ Author: Boris Danev and Aurelien Frossard";

	/** Configures the simulator from an XML file */
	public static void configure(String p_fileName, SimulatorContext p_context)
		throws ParserException {
		new SimConfigurator(p_context).doConfigure(p_fileName);
	}

	/** Reference to the simulator context */
	private SimulatorContext m_context;

	/** Default constructor */
	private SimConfigurator(SimulatorContext p_context) {
		m_context = p_context;
	}

	/** Does the configuration using a particular parser implementation */
	private void doConfigure(String p_fileName) throws ParserException {
		ParseVisitor jdom;
		try {
			jdom = new JDOMParser(new File(p_fileName));
			jdom.parseSimulationConfig();
			jdom.parseSimulationDaemons();
			jdom.parseSimulationLayer();
			jdom.parseSimulationNetwork();
			jdom.outputXMLFile();
		} catch (JDOMException jdome) {
			throw ParserException.parseError(jdome);
		} catch (IOException ioe) {
			throw ParserException.parseError(ioe);
		}
	}

	/** An implementation parser should implement this interface */
	interface ParseVisitor {
		public void parseSimulationConfig() throws ParserException;
		public void parseSimulationDaemons() throws ParserException;
		public void parseSimulationLayer();
		public void parseSimulationNetwork() throws ParserException;
		public void outputXMLFile() throws ParserException;
	}

	/** JDOM implementation of the XML parser */
	class JDOMParser implements ParseVisitor {
		private SAXBuilder m_builder;
		private Document m_document;

		public JDOMParser(File p_xmlFile) throws JDOMException, IOException {
			m_builder =
				new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
			m_builder.setFeature(
				"http://apache.org/xml/features/validation/schema",
				true);
			m_builder.setValidation(true);
			m_document = m_builder.build(p_xmlFile);
		}

		public void parseSimulationConfig() throws ParserException {
			Element root, server;
			SimulationConfig config;
			long ltemp;
			int itemp;

			root =
				m_document.getRootElement().getChild(
					SimXMLTags.SIMULATION_CONFIG_TAG);
			config = new SimulationConfig();

			try {
				server = root.getChild(SimXMLTags.SERVER_TAG);
				config.setServIP(server.getAttributeValue(SimXMLTags.IP_ATT));
				itemp =
					Integer.parseInt(
						server.getAttributeValue(SimXMLTags.PORT_ATT));
				config.setServPort(itemp);
				itemp =
					Integer.parseInt(
						server.getAttributeValue(SimXMLTags.TIMEOUT_ATT));
				config.setServTimeout(itemp);
				itemp =
					Integer.parseInt(
						server.getAttributeValue(SimXMLTags.QUEUE_ATT));
				config.setServQueue(itemp);
				ltemp = Long.parseLong(root.getChildText(SimXMLTags.CLOCK_TAG));
				config.setClockSpeed(ltemp);
				ltemp =
					Long.parseLong(root.getChildText(SimXMLTags.DURATION_TAG));
				config.setDuration(ltemp);
				config.setOutputDir(
					root.getChildText(SimXMLTags.OUTPUT_DIR_TAG));
			} catch (NumberFormatException nfe_e) {
				throw ParserException.parseError(NUMBERFORMAT_ERROR);
			}

			/* Freezes the this component */
			config.freeze();

			/* Adds the component in the context of the simulator */
			m_context.put(ContextKey.SIMULATION_KEY, config);
		}

		public void parseSimulationDaemons() throws ParserException {
			Element root;
			DaemonsConfig dc;
			String temp;

			root =
				m_document.getRootElement().getChild(
					SimXMLTags.DEAMONS_CONFIG_TAG);
			dc = new DaemonsConfig();
			for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
				Element e = (Element) i.next();
				//Test the number format of the port
				try {
					Integer.parseInt(e.getAttributeValue(SimXMLTags.PORT_ATT));
				} catch (NumberFormatException nfe_e) {
					throw ParserException.parseError(NUMBERFORMAT_ERROR);
				}
				dc.addDaemon(
					new String[] {
						e.getAttributeValue(SimXMLTags.IP_ATT),
						e.getAttributeValue(SimXMLTags.PORT_ATT)});
			}

			/* Freeze the component */
			dc.freeze();

			/* Adds the component in the context of the simulator */
			m_context.put(ContextKey.DEAMONS_KEY, dc);
		}

		public void parseSimulationLayer() {
			Element root;
			LayerConfig config;
			String temp;

			root =
				m_document.getRootElement().getChild(
					SimXMLTags.LAYER_CONFIG_TAG);
			config = new LayerConfig();
			config.setName(root.getChildText(SimXMLTags.NAME_TAG));
			config.setClass(root.getChildText(SimXMLTags.CLASS_TAG));

			/* Freeze the component */
			config.freeze();

			/* Adds the component in the context of the simulator */
			m_context.put(ContextKey.LAYER_KEY, config);
		}

		public void parseSimulationNetwork() throws ParserException {
			Element root, global, custom;
			NodeConfig[] nodeConf;
			NetworkConfig config = new NetworkConfig();
			MobilityParser parser;
			IParsedMobility[] patterns = new IParsedMobility[0];
			String file;
			double range;
			double quality;
			int timeUnit;
			int networkSize;
			int mobilityID;
			String mode;
			try {
				/* Get the root of the network component */
				root =
					m_document.getRootElement().getChild(
						SimXMLTags.NETWORK_TAG);

				/* Get the size of the network */
				networkSize =
					Integer.parseInt(
						root.getAttributeValue(SimXMLTags.SIZE_ATT));
				config.setNetworkSize(networkSize);

				/* Get the mode of simulation network */
				mode = root.getAttributeValue(SimXMLTags.MODE_ATT);
				config.setType(mode.equals(SimXMLTags.MODE_VALUE_ATT));

				/* Get global configuration */
				global = root.getChild(SimXMLTags.GLOBAL_CONFIG_TAG);
				config.setFrameworkInFile(
					global.getChildText(SimXMLTags.NODE_CONFIGIN_TAG));
				config.setFrameworkOutFile(
					global.getChildText(SimXMLTags.NODE_CONFIGOUT_TAG));
				config.setErrorLog(
					global.getChildText(SimXMLTags.NODE_ERRORLOG));
				config.setSimulationLog(
					global.getChildText(SimXMLTags.NODE_SIMLOG));

				range =
					Double.parseDouble(
						global.getChild(
							SimXMLTags
								.NODE_TRANS_DEFAULTS_TAG)
								.getAttributeValue(
							SimXMLTags.NODE_RANGE_ATT));
				config.setRange(range);
				quality =
					Double.parseDouble(
						global.getChild(
							SimXMLTags
								.NODE_TRANS_DEFAULTS_TAG)
								.getAttributeValue(
							SimXMLTags.NODE_QUALITY_ATT));
				config.setQuality(quality);

				/* Create the total number of simulation nodes */
				nodeConf = new NodeConfig[networkSize];

				timeUnit =
					Integer.parseInt(
						global.getChild(
							SimXMLTags.MOBILITYPATTERN_TAG).getAttributeValue(
							SimXMLTags.TIME_UNIT_ATT));

				/* Fill global configuration for each node */
				file = global.getChildText(SimXMLTags.MOBILITYPATTERN_TAG);
				try {
					parser = new MobilityParser(new FileReader(file));
					patterns = parser.parse();
					parser.close();
				} catch (FileNotFoundException e) {
					throw ParserException.parseError(e);
				} catch (IOException e) {
					throw ParserException.parseError(e);
				}

				//If the network size is bigger, it is an error
				if (networkSize > patterns.length)
					throw ParserException.parseError(
						new ArrayIndexOutOfBoundsException(SIZE_ERROR));

				for (int i = 0; i < networkSize; i++) {
					nodeConf[i] = new NodeConfig();
					nodeConf[i].setNodeID(i + 1);
					nodeConf[i].setConfigInFile(config.getFrameworkInFile());
					nodeConf[i].setConfigOutFile(
						config.getFrameworkOutFile() + Integer.toString(i + 1));
					nodeConf[i].setMobility(
						MobilityPattern_Factory.getMobilityPattern(
							patterns[i],
							timeUnit));
					nodeConf[i]
						.setSchedule(new TimeSchedulePattern(new long[] {
					}, new long[] { 0 }));
					nodeConf[i].setErrorLog(config.getErrorLog());
					nodeConf[i].setSimulationLog(config.getSimulationLog());
					nodeConf[i].setTransmissionRange(range);
					nodeConf[i].setTransmissionQuality(quality);
				}

				/*
				 * Fill custom configuration for each node if available and
				 * type = manual
				 */
				custom = root.getChild(SimXMLTags.CUSTOM_CONFIG_TAG);
				if ((custom != null) && (config.getType())) {
					List lNodes, off, on;
					Element node, schedule, iter;
					String temp;
					int id, scaleBy;
					long offs[], ons[];

					lNodes = custom.getChildren();
					for (Iterator i = lNodes.iterator(); i.hasNext();) {
						node = (Element) i.next();
						id =
							Integer.parseInt(
								node.getAttributeValue(SimXMLTags.ID_ATT))
								- 1;
						if (id < 0)
							throw ParserException.parseError(
								CUSTOM_CONFIG_ERROR);

						temp =
							node.getAttributeValue(SimXMLTags.NODE_RANGE_ATT);
						if (temp != null) {
							nodeConf[id].setTransmissionRange(
								Double.parseDouble(temp));
						}
						temp =
							node.getAttributeValue(SimXMLTags.NODE_QUALITY_ATT);
						if (temp != null) {
							nodeConf[id].setTransmissionQuality(
								Double.parseDouble(temp));
						}
						temp = node.getChildText(SimXMLTags.NODE_CONFIGIN_TAG);
						if (temp != null) {
							nodeConf[id].setConfigInFile(temp);
						}
						temp = node.getChildText(SimXMLTags.NODE_CONFIGOUT_TAG);
						if (temp != null) {
							nodeConf[id].setConfigOutFile(temp);
						}
						schedule = node.getChild(SimXMLTags.NODE_SCHEDULE_TAG);
						if (schedule != null) {
							scaleBy =
								Integer.parseInt(
									schedule.getAttributeValue(
										SimXMLTags.SCALEBY_ATT));
							off = schedule.getChildren(SimXMLTags.NODE_OFF_TAG);
							on = schedule.getChildren(SimXMLTags.NODE_ON_TAG);

							offs = new long[off.size()];
							int j = 0;
							for (Iterator k = off.iterator(); k.hasNext();) {
								offs[j] =
									scaleBy
										* Long.parseLong(
											(
												(Element) k
													.next())
													.getAttributeValue(
												SimXMLTags.AT_TIME_ATT));
							}
							ons = new long[on.size()];
							j = 0;
							for (Iterator k = on.iterator(); k.hasNext();) {
								ons[j] =
									scaleBy
										* Long.parseLong(
											(
												(Element) k
													.next())
													.getAttributeValue(
												SimXMLTags.AT_TIME_ATT));
							}
							nodeConf[id].setSchedule(
								new TimeSchedulePattern(offs, ons));
						}
						temp =
							node.getChildText(SimXMLTags.NODE_MOBILITYID_TAG);
						if (temp != null) {
							mobilityID =
								Integer.parseInt(
									node.getChildText(
										SimXMLTags.NODE_MOBILITYID_TAG));
							if (mobilityID < 0
								|| mobilityID >= patterns.length) {
								throw ParserException.parseError(
									MOBILITY_CONFIG_ERROR);
							}
							nodeConf[id].setMobility(
								MobilityPattern_Factory.getMobilityPattern(
									patterns[mobilityID],
									timeUnit));
						}
					}
				}

				/* Freeze all the components */
				for (int i = 0; i < networkSize; i++) {
					nodeConf[i].freeze();
				}

				/* Sets the simulation nodes configuration */
				config.setNodes(nodeConf);

				/* Freezes the this component */
				config.freeze();

				/* Adds the component in the context of the simulator */
				m_context.put(ContextKey.NETWORK_KEY, config);

			} catch (NumberFormatException nfe_e) {
				throw ParserException.parseError(NUMBERFORMAT_ERROR);
			}
		}

		public void outputXMLFile() throws ParserException {
			/* Get the additional parameters */
			Element dll = null;
			SimulationConfig sc;
			NetworkConfig nc;
			LayerConfig lc;
			int nodeLength;
			NodeConfig node;

			try {
				m_document.detachRootElement();
				sc =
					(SimulationConfig) m_context.get(ContextKey.SIMULATION_KEY);
				nc = (NetworkConfig) m_context.get(ContextKey.NETWORK_KEY);
				lc = (LayerConfig) m_context.get(ContextKey.LAYER_KEY);
				nodeLength = nc.getNodeCount();

				for (int i = 0; i < nodeLength; i++) {
					node = nc.getNodeConfig(i);
					m_document =
						m_builder.build(new File(node.getConfigInFile()));
					dll =
						m_document.getRootElement().getChild(
							ParserXml.LAYERS).getChild(
							SimXMLTags.DATA_LINK_TAG);

					dll.getChild(ParserXml.NAME).setText(lc.getName());
					dll.getChild(ParserXml.CLASS).setText(lc.getClassName());
					dll.addContent(
						new Element(ParserXml.PARAM)
							.setAttribute(
								new Attribute(
									ParserXml.NAME,
									SimXMLTags.SERVER_TAG))
							.setText(sc.getServIP()));
					dll.addContent(
						new Element(ParserXml.PARAM)
							.setAttribute(
								new Attribute(
									ParserXml.NAME,
									SimXMLTags.SERVER_PORT))
							.setText(Long.toString(sc.getServPort())));
					dll.addContent(
						new Element(ParserXml.PARAM)
							.setAttribute(
								new Attribute(
									ParserXml.NAME,
									SimXMLTags.NODE_ID))
							.setText(Integer.toString(node.getNodeID())));

					FileWriter fw =
						new FileWriter(new File(node.getConfigOutFile()));
					XMLOutputter outp = new XMLOutputter();
					outp.setTextTrim(true);
					outp.setIndent("  ");
					outp.setNewlines(true);
					outp.output(m_document, fw);

					/* Detach the document currently used */
					m_document.detachRootElement();
				}
			} catch (Exception e) {
				throw ParserException.parseError(e);
			}
		}
	}

	/**
	 * This error signals that the mobility pattern doesn't correspond to the
	 * size of the network
	 */
	private static final String SIZE_ERROR =
		"network size > number of mobility patterns";
	private static final String NETWORK_ERROR =
		"network size < number of nodes in <custom-config> ";
	private static final String NUMBERFORMAT_ERROR =
		"expected a number -> found a string";
	private static final String CUSTOM_CONFIG_ERROR =
		"Node ID could not be equal to 0. Node IDs should start from 1";
	private static final String MOBILITY_CONFIG_ERROR =
		"Mobility ID should be in a given range defined by the network size";
}