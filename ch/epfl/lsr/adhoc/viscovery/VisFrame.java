package ch.epfl.lsr.adhoc.viscovery;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import com.touchgraph.graphlayout.*;
import java.util.Iterator;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/** 
 * This class is used to display the network topology of a MANET graphically.
 * The viscovery layer of the MANET Framework is needed in order to gather the
 * necessary information via a TokenMessage
 *
 * @see TokenMessage
 * @see Viscovery
 * @author Stefan Thurnherr
 * @version 1.0
 */
 
class VisFrame extends JFrame implements WindowListener, ActionListener {
		/** JTabbedPane used to display either the current graph or the log window */
		private JTabbedPane jt = null;
		/** Textbox showing what kind of messages arrive/leave the viscovery layer */
		private JEditorPane taMsgMonitor = null;
		/* DropDownList to choose the delay between receipt and Send of the token */
		private JComboBox jcbDelay = null;
		/* DropDownList to choose the algo for a newly created token */
		JComboBox jcbAlgo = null;
		/* Button to create a new Token */
		private JButton btnCreate = null;
		/* Button to clear the log window */
		private JButton btnClear = null;
		/* Checkbox to enable holding the token upon its next visit on this node */
		private JCheckBox cbxHold = null;
		/* Button to release a hold token again into the net */
		private JButton btnRelease = null;
		/* Button to destroy a token that is currently being hold by this node */
		private JButton btnDestroy = null;
		/* Reference to the viscovery layer which created this object */
		private Viscovery visco = null;
		/* Reference to the unique node ID of this node */
		private long nodeID = -1;
		/* Touchgraph object used to graphically represent the discovered network topology */
		private TGPanel tgPanel = null;
		/** Number of digits that we want to be represented in a node's label, starting from
		 * the end of a node's ID; set this to a value between 0 (no label) and 13 (full label)
		 */
		private int labelLength = 4;
		/** Statemachine variable to organise the actionPerformed of the input possibilities */
		private int guiState = 0;
		

		VisFrame (Viscovery visco, long nodeID, String nodeName) {
				this.nodeID = nodeID;
				this.visco = visco;
				
				// Initialize Frame
				try {
						UIManager.getSystemLookAndFeelClassName();
						//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				}
				catch (Exception e) {
						//System.out.println("# failed while setting up L&F - applying std L&F");
						JFrame.setDefaultLookAndFeelDecorated(true);
				}
				this.setTitle("MANET Viscovery (" + Viscovery.version + ") - " + nodeID + " - " + nodeName);
				
				//GridBagLayout gbl = new GridBagLayout();
				//GridBagConstraints gbConstr = new GridBagConstraints();
				//this.getContentPane().setLayout(gbl);
				this.getContentPane().setLayout(new BorderLayout());

				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				this.setSize(600, 600);
				Dimension df = this.getSize();
				this.setLocation(((int)(d.width / 2)) - ((int)(df.width / 2)), ((int)(d.height / 2)) - ((int)(df.height / 2)));
				//this.setLocation(0,0);
				this.addWindowListener(this);
				this.guiState = 0; // init the stateMachine of the buttons etc. to state 0
				
				//Initialize the tabbed pane
				jt = new JTabbedPane(JTabbedPane.TOP);
				this.getContentPane().add(jt, BorderLayout.CENTER);

				//create a JGraph model to graphically represent the detected network topology
				tgPanel = new TGPanel();
				tgPanel.setBackground(Color.orange);
        TGLensSet tgls = new TGLensSet();
        tgls.addLens(tgPanel.getAdjustOriginLens());
        tgPanel.setLensSet(tgls);
        try {
						String s = "" + nodeID;
            tgPanel.addNode("" + nodeID, s.substring(s.length()-this.labelLength));  //Add a starting node.
        } catch ( TGException tge ) {
						//System.out.println("# TGException:");
            tge.printStackTrace();
        }
				tgPanel.setMaximumSize(new Dimension(300, 300));
        tgPanel.setVisible(true);
				Edge.setEdgeDefaultLength(10);
        //new GLEditUI(tgPanel).activate();
				jt.addTab("graph", null, tgPanel, "show discoverd network topology");

				//initialize a textfield to monitor the crossing messages
				taMsgMonitor = new JEditorPane("text/plain", "Welcome to the Viscovery application " + Viscovery.version + "\n");
				taMsgMonitor.setBackground(Color.white);
				//taMsgMonitor.setMinimumSize(new Dimension(200, 200));
				taMsgMonitor.setEditable(false);
				taMsgMonitor.setAutoscrolls(true);
				jt.addTab("log", null, new JScrollPane(taMsgMonitor), "show the log messages");
				jt.setSelectedIndex(1);

				//initialize the panel to display the buttons
				JPanel panButtons = new JPanel();
				GridBagLayout gridbag = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();
				panButtons.setLayout(gridbag);
				c.fill = GridBagConstraints.VERTICAL; //buttons should use all available vertical space
				c.insets = new Insets(4,4,4,4);
				this.getContentPane().add(panButtons, BorderLayout.SOUTH);

				//set up the buttons to enable interaction for the user
				JLabel labelDelay = new JLabel("set sending delay [s] to:");
				c.gridx = 0;
				c.gridy = 0;
				c.anchor = GridBagConstraints.EAST;
				gridbag.setConstraints(labelDelay, c);
				panButtons.add(labelDelay);

				String[] possibleDelays = {"" + visco.getDelay()/1000 + " (default)", "1", "3", "5", "10"};
				jcbDelay = new JComboBox(possibleDelays);
				jcbDelay.addActionListener(this);
				c.gridx = 1;
				c.gridy = 0;
				c.anchor = GridBagConstraints.WEST;
				gridbag.setConstraints(jcbDelay, c);
				panButtons.add(jcbDelay);

				String[] possibleAlgos = {"Choose algo", "LRV", "LFV"};
				jcbAlgo = new JComboBox(possibleAlgos);
				jcbAlgo.addActionListener(this);
				c.gridx = 0;
				c.gridy = 1;
				c.anchor = GridBagConstraints.EAST;
				gridbag.setConstraints(jcbAlgo, c);
				panButtons.add(jcbAlgo);

				btnCreate = new JButton("Create a new Token");
				btnCreate.addActionListener(this);
				btnCreate.setEnabled(false);
				c.gridx = 1;
				c.gridy = 1;
				c.anchor = GridBagConstraints.WEST;
				gridbag.setConstraints(btnCreate, c);
				panButtons.add(btnCreate);

				btnClear = new JButton("Clear log window");
				btnClear.addActionListener(this);
				c.gridx = 2;
				c.gridy = 1;
				gridbag.setConstraints(btnClear, c);
				panButtons.add(btnClear);

				cbxHold = new JCheckBox("Hold next visiting token", false);
				cbxHold.addActionListener(this);
				c.gridx = 0;
				c.gridy = 2;
				c.anchor = GridBagConstraints.SOUTH;
				gridbag.setConstraints(cbxHold, c);
				panButtons.add(cbxHold);

				btnRelease = new JButton("Release the token again");
				btnRelease.addActionListener(this);
				btnRelease.setEnabled(false);
				c.gridx = 1;
				c.gridy = 2;
				gridbag.setConstraints(btnRelease, c);
				panButtons.add(btnRelease);

				btnDestroy = new JButton("Destroy this token");
				btnDestroy.addActionListener(this);
				btnDestroy.setEnabled(false);
				c.gridx = 2;
				c.gridy = 2;
				gridbag.setConstraints(btnDestroy, c);
				panButtons.add(btnDestroy);
		}
		
		/**
		 * ActionListener that is used to send messages.
		 * <p>
		 * This method is called when the user clicks one of the offered button in the
		 * graphical interface
		 */
		public synchronized void actionPerformed(ActionEvent ae) {
				//writeLog("enter actionPerformed with guiState " + guiState);
				Object src = ae.getSource();
				if (src == btnClear) {
						taMsgMonitor.setText("Viscovery application " + Viscovery.version + " - log cleared\n");
				}
				else if (src == jcbDelay) {
						String delayString = (String)(jcbDelay.getSelectedItem());
						try {
								long delay = Long.parseLong(delayString.substring(0,1));
								visco.setDelay(delay);
								writeLog("delay changed to " + delay + " secs");
						}
						catch (NumberFormatException e) {
								writeLog("delay not understood and not changed");
						}
				}
				else {
						switch(guiState) {
						case 0:
								if(src == jcbAlgo) {
										if (jcbAlgo.getSelectedIndex() > 0) {
												btnCreate.setEnabled(true);
												guiState = 1;
												writeLog("algo changed to "+ (String)jcbAlgo.getSelectedItem());
												//writeLog("guiState changed to 1");
										}
								}
								else if (src == cbxHold) {
										if (cbxHold.isSelected()){
												visco.setHoldNextToken(true);
												btnRelease.setEnabled(true);
												btnDestroy.setEnabled(true);
												guiState = 2;
												writeLog("holding next token..");
												//writeLog("guiState changed to " + guiState);
										}
										else {
												writeLog("BAD case with guiState " + guiState);
										}
								}
								else {
										writeLog("BAD case with guiState " + guiState);
								}
								break;
						case 1:

								if (src == btnCreate) {
										//remember the selected algorithm
										String jcbValue = (String)(jcbAlgo.getSelectedItem());
										jcbAlgo.setSelectedIndex(0);
										btnCreate.setEnabled(false);
										//start a new tokenCreator-thread which creates a token
										new FrancThread(new TokenCreator(visco, jcbValue)).start();
										guiState = 0;
										//writeLog("creating new token..");
										//writeLog("guiState changed to " + guiState);
								}
								else if (src == jcbAlgo) {
										String jcbValue = (String)(jcbAlgo.getSelectedItem());
										if (!jcbValue.equals("LRV") && !jcbValue.equals("LFV")) {
												//user unselected all possible algos -> return to state 0
												guiState = 0;
												//writeLog("no valid algo selected!");
												//writeLog("guiState changed to " + guiState);
												btnCreate.setEnabled(false);
										}
								}
								else if(src == cbxHold) {

										if (cbxHold.isSelected()){
												visco.setHoldNextToken(true);
												btnRelease.setEnabled(true);
												btnDestroy.setEnabled(true);
												guiState = 3;
												writeLog("holding next token..");
												//writeLog("guiState changed to " + guiState);
										}
										else {
												writeLog("BAD case with guiState " + guiState);
										}
								}
								break;
						case 2:
								if(src == jcbAlgo) {
										if (jcbAlgo.getSelectedIndex() > 0) {
												btnCreate.setEnabled(true);
												guiState = 3;
												writeLog("algo changed to " + (String)jcbAlgo.getSelectedItem());
												//writeLog("guiState changed to " + guiState);
										}
								}
								else if (src == cbxHold) {
										if (!cbxHold.isSelected()){
												guiState = 0;
												writeLog("holding next token..");
												//writeLog("guiState changed to " + guiState);
												btnRelease.setEnabled(false);
												btnDestroy.setEnabled(false);
												visco.setHoldNextToken(false);
												visco.releaseToken(true);
										}
								}
								else if(src == btnRelease) {
										guiState = 0;
										writeLog("releasing token..");
										//writeLog("guiState changed to " + guiState);
										cbxHold.setSelected(false);
										btnRelease.setEnabled(false);
										btnDestroy.setEnabled(false);
										visco.setHoldNextToken(false);
										visco.releaseToken(true);
								}
								else if(src == btnDestroy) {
										guiState = 0;
										writeLog("releasing token..");
										//writeLog("guiState changed to " + guiState);
										cbxHold.setSelected(false);
										btnRelease.setEnabled(false);
										btnDestroy.setEnabled(false);
										visco.setHoldNextToken(false);
										visco.releaseToken(false);
								}
								else {
										writeLog("BAD case with guiState " + guiState);
								}
								break;
						case 3:
								if (src == btnCreate) {
										//remember the selected algorithm
										String jcbValue = (String)(jcbAlgo.getSelectedItem());
										jcbAlgo.setSelectedIndex(0);
										btnCreate.setEnabled(false);
										//start a new tokenCreator-thread which creates a token
										new FrancThread(new TokenCreator(visco, jcbValue)).start();
										guiState = 2;
										writeLog("creating new token..");
										//writeLog("guiState changed to " + guiState);
								}
								else if (src == jcbAlgo) {
										String jcbValue = (String)(jcbAlgo.getSelectedItem());
										if (!jcbValue.equals("LRV") && !jcbValue.equals("LFV")) {
												//user unselected all possible algos -> return to state 0
												btnCreate.setEnabled(false);
												guiState = 0;
												//writeLog("no valid algo selected!");
												//writeLog("guiState changed to " + guiState);
										}
								}
								else if (src == cbxHold) {
										if (!(cbxHold.isSelected())) {
												btnRelease.setEnabled(false);
												btnDestroy.setEnabled(false);
												cbxHold.setSelected(false);
												guiState = 1;
												writeLog("releasing token..");
												//writeLog("guiState changed to " + guiState);
												visco.setHoldNextToken(false);
												visco.releaseToken(true);
										}
								}
								else if(src == btnRelease) {
										guiState = 1;
										writeLog("releasing token..");
										//writeLog("guiState changed to " + guiState);
										cbxHold.setSelected(false);
										btnRelease.setEnabled(false);
										btnDestroy.setEnabled(false);
										visco.setHoldNextToken(false);
										visco.releaseToken(true);
								}
								else if(src == btnDestroy) {
										guiState = 1;
										writeLog("releasing token..");
										//writeLog("guiState changed to " + guiState);
										cbxHold.setSelected(false);
										btnRelease.setEnabled(false);
										btnDestroy.setEnabled(false);
										visco.setHoldNextToken(false);
										visco.releaseToken(false);
								}
								else {
										writeLog("BAD case with guiState " + guiState);
								}
								break;
						default:
								writeLog("case 'default' - BAD case! nothing happens..");
								break;
						}
						//writeLog("leaving switch with guiState " + guiState);
				}
				//System.out.println("actionPerformed: END reached");
		}

		/**
		 * Method to select the tab that is currently visible
		 *@param index The index of the tab that should be visible at
		 * a given moment
		 */
		protected void showTab(int index) {
				if (index < jt.getTabCount())
						jt.setSelectedIndex(index);
		}

		/** Method used to append some log messages into taMsgMonitor
		 *@param text The text that will be appended in the taMsgMonitor
		 */
	  protected void writeLog(String text) {
				synchronized(taMsgMonitor) {
						//System.out.println(":::" + text);
						try {
								String s = taMsgMonitor.getText();
								taMsgMonitor.setText(s + "> " + text + "\n");
								taMsgMonitor.setCaretPosition(s.length());
						}
						catch (IllegalArgumentException e) {
								//System.out.println("# unable to set caret; continuing..");
						}
						}
		}

		/** Method used to update the graphical representation of the current MANET topology
		 *@param newNeighbors The data containing the topology to visualize
		 */
		protected void updateGraph(TokenMessage msg) {
				tgPanel.clearAll();
				tgPanel.processGraphMove();
				tgPanel.repaintAfterMove();

				/**
				 * nodeID : unique node-id of the node hosting this application
				 * nodeId : temporary variable used in this function for iterators etc
				 */
				//System.out.println("updateGraph: starting with the following trace:\n******\n" + msg.toString() + "\n******\n");
				java.util.List tokenTrace = msg.getTrace();
				java.util.List traceRow;
				java.util.ArrayList connectedNodes = null;
				synchronized(tokenTrace) {
						Long nodeId = new Long(-1);
						Node thisNode = null;
						for (Iterator it = tokenTrace.iterator(); it.hasNext();){
								// go through every line/row of the tokenTrace and associate it with nodeId
								traceRow = (java.util.List)it.next();
								nodeId = (Long)traceRow.get(0);
								//writeLog("updateGraph: treating tracerow of this node (" + nodeId + ")");
								try {
										thisNode = tgPanel.findNode(nodeId.toString());
										if (thisNode == null) {//this node is not yet present in the graph
												//System.out.println(" nodeId of this tracerow is not yet present in graph -> add it");
												String s = nodeId.toString();
												thisNode = tgPanel.addNode(nodeId.toString(), s.substring(s.length() - this.labelLength));
										}
										Long aNodeId = null;
										Node aNode = null;
										/**
										 * get the old connections in the graph for thisNode (==nodeId)
										 */
										//java.util.ArrayList oldEdges = new java.util.ArrayList();
										connectedNodes = new java.util.ArrayList();
										if (thisNode.getEdges() != null) {										
												Edge e = null;
												for ( Iterator itNodeEdges = thisNode.getEdges(); itNodeEdges.hasNext(); e = null) {
														e = (Edge)itNodeEdges.next();												
														if (e != null) {
																connectedNodes.add(e.getOtherEndpt(thisNode));
																//oldEdges.add(e);
														}
												}
										}
										for (Iterator it2 = traceRow.listIterator(2); it2.hasNext();) {
												/**
												 * go through the neighbors stored in the row/line of node nodeId (=tracerow):
												 * Starting from column 2, go through every neighbor aNodeId of nodeId,
												 * assure that it is displayed in the graph, and connect it to nodeId
												 */
												aNodeId = ((Long)it2.next());
												aNode = tgPanel.findNode(aNodeId.toString());
												if (aNode == null) {
														//System.out.println(" node " + aNodeId.toString() + " not yet in graph..adding it and connecting it to " + thisNode.getID());
														String s = aNodeId.toString();
														aNode = tgPanel.addNode(aNodeId.toString(), s.substring(s.length() - this.labelLength));
												}
												else {
														//System.out.println(" node " + aNode.getID() + " already in graph..connecting to " + thisNode.getID());
												}
												/**
												 * now connect the neighbor to nodeId (== thisNode), but only if such a connection does not
												 * exist already
												 */
												if ( ! connectedNodes.contains(aNode)) {
														tgPanel.addEdge(thisNode, aNode, 15);
												}
										}
								}
								catch(TGException tge) {
										writeLog("GException occured:\n");
										tge.printStackTrace();
								}
								//System.out.println(" treatment of tracerow for node " + nodeId + " finished..going to next traceRow");
						}
						//System.out.println("No more tracerows..leaving updateGraph");
				}
				//this.repaint();
				tgPanel.processGraphMove();
				tgPanel.repaintAfterMove();
				//tgPanel.resetDamper();
				//System.out.println("updateGraph:processGraphMove() / repaint() done");
		}

		/** We need to implement this method for the WindowListener interface, but it's not used */
		public void windowClosed(WindowEvent we) {}
		/**
		 * This method is called when the user tries to close the window (via
		 * system menu). It just terminates the application.
		 */
		public void windowClosing(WindowEvent we) {
				System.exit(0);
		}
		/** We need to implement this method for the WindowListener interface, but it's not used */	
		public void windowActivated(WindowEvent we) {}
		/** We need to implement this method for the WindowListener interface, but it's not used */
		public void windowDeactivated(WindowEvent we) {}
		/** We need to implement this method for the WindowListener interface, but it's not used */
		public void windowDeiconified(WindowEvent we) {}
		/** We need to implement this method for the WindowListener interface, but it's not used */
		public void windowIconified(WindowEvent we) {}
		/** We need to implement this method for the WindowListener interface, but it's not used */
		public void windowOpened(WindowEvent we) {}
}
