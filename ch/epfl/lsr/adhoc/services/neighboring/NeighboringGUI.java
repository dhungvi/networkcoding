package ch.epfl.lsr.adhoc.services.neighboring;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**
 * This class is a Graphic user interface which shows the neighbors.
 *
 *@see NeighborService
 */



class NeighboringGUI extends JFrame implements WindowListener,ItemListener,Runnable{
	
  private Panel pHaut;
  private BorderLayout lm2;	
  private GridLayout lm4;
  private JLabel lName,lID,lExp;
  private int [] listeIntervalles = {2,4,5,10};
  private int [] listeExp = {4,8,10,20};
  private int interv,exp;
  private Choice cExp,cInterv;
  private Panel pResultats ;
  private NeighborService nm;
  private Hello sender;
  private int maxVoisins = 10; 
  private JLabel [][] listeLabels = new JLabel [maxVoisins][3];;
  
  
  public NeighboringGUI (int exp, int interv,NeighborService nm,Hello sender, String name) {
  	
  	this.exp = exp;
  	this.interv = interv;
  	this.nm = nm;
  	this.sender = sender;
  	creerListes ();
    setSize(450,350);
    setLocation(520,381);
    setTitle ("Lists of neighbors - "+name);
    addWindowListener(this);
    BorderLayout lm1 = new BorderLayout();
    this.getContentPane().setLayout (lm1);
    
    lm2 = new BorderLayout ();
    pHaut = new Panel (lm2);
    getContentPane().add(pHaut,BorderLayout.CENTER);
    
    creerHaut ();
    creerCentre ();
    getContentPane().add (new JLabel ("    "),BorderLayout.NORTH);
    getContentPane().add (new JLabel ("    "),BorderLayout.SOUTH);
    getContentPane().add (new JLabel ("    "),BorderLayout.EAST);
    getContentPane().add (new JLabel ("    "),BorderLayout.WEST);
    FrancThread t = new FrancThread(this);
    this.setVisible (true);
    this.setResizable (false);
    t.setDaemon(true);
    t.start();
  }
  
  public void run() {
    while(true) {
    	try {FrancThread.sleep(1000);} catch(InterruptedException ie) {}
    	updateCentre ();
      
    }
  }
  
  private void creerHaut ()
  {
  	
  	
    Font flTitre = new Font ("Arial",Font.BOLD,14);
    GridLayout lm3 = new GridLayout (2,3,0,5);
    Panel pParametres = new Panel (lm3);
    
    JLabel lParametres = new JLabel ("Parameters",JLabel.LEFT);
    lParametres.setFont(flTitre);
	lParametres.setForeground (Color.black);
    pParametres.add (lParametres);
    JLabel lInterval = new JLabel ("Inverval",JLabel.LEFT);
    lInterval.setForeground (Color.black);
    pParametres.add (lInterval);
    cInterv = new Choice ();
    
    for (int i = 0; i<listeIntervalles.length;i++)
    {
    	cInterv.addItem (""+listeIntervalles [i]);
    }
    pParametres.add (cInterv);
    cInterv.addItemListener (this);
    cInterv.select (""+interv);
    
    JLabel lEntry = new JLabel ("Entry Exp",JLabel.LEFT);
    lEntry.setForeground (Color.black);
    pParametres.add (new JLabel ("",JLabel.LEFT));
    pParametres.add (lEntry);
    cExp = new Choice ();
    
    for (int i = 0; i<listeExp.length;i++)
    	cExp.addItem (""+listeExp [i]);
    
    cExp.addItemListener(this);
    cExp.select (""+exp);
    
    pParametres.add(cExp);
    
    pHaut.add (pParametres,BorderLayout.NORTH);
  }
  
  
  
  private void creerCentre ()
  {
  	Font flTitre = new Font ("Arial",Font.BOLD,14);
  	Font fresults = new Font ("Arial",Font.BOLD,12);
  	lm4 = new GridLayout (maxVoisins+2,3,0,0);
  	pResultats = new Panel (lm4);
  	lName = new JLabel ("Node Name",JLabel.LEFT);
  	lName.setFont(flTitre);
	lName.setForeground (Color.black);
  	lID = new JLabel ("Node ID",JLabel.LEFT);
  	lID.setFont(flTitre);
	lID.setForeground (Color.black);
  	lExp = new JLabel ("Time to live",JLabel.LEFT);
  	lExp.setFont(flTitre);
	lExp.setForeground (Color.black);
	pHaut.add (pResultats,BorderLayout.CENTER);
	
	pResultats.add (new JLabel (""));
  	pResultats.add (new JLabel (""));
  	pResultats.add (new JLabel (""));
	
  	pResultats.add (lName);
  	pResultats.add (lID);
  	pResultats.add (lExp);
  	for (int i = 0;i<maxVoisins;i++)
  	{
  		for (int j =0;j<3;j++)
  		{
  			JLabel labProv = new JLabel ("",JLabel.LEFT);
  			labProv.setForeground (Color.black);
  			listeLabels [i][j] = labProv;
  			pResultats.add (listeLabels [i][j]);
  		}
  	}
  }
  
  private void updateCentre ()
  {
  	String [] []listeVoisins = (nm.getTable()).getTableString();
  	
  	
  	for (int i=0;i<listeVoisins.length;i++)
  	{
  		for (int j = 0; j<listeVoisins[i].length;j++)
  		{
  			listeLabels [i][j].setText (listeVoisins [i][j]);
  		}
  	}
  	for (int k=listeVoisins.length;k<maxVoisins;k++)
  	{
  		listeLabels [k][0].setText ("");
  		listeLabels [k][1].setText ("");
  		listeLabels [k][2].setText ("");
  	}
  		
  	
  }
  
  
  /** 
  * This method is used to create the lists of options
  */
  private void creerListes ()
  {
  	boolean isInside = false;
  	boolean place = false;
  	for (int i = 0;i<listeIntervalles.length;i++)
  	{
  		if (listeIntervalles [i] == interv)
  			isInside = true;
  	}
  	if (!isInside)
  	{
  		int []listeProv = listeIntervalles;
  		listeIntervalles = new int [listeProv.length+1];
  		int indice = 0; 
  		for (int i = 0;i<listeProv.length;i++)
  		{
  			if (listeProv [i] < interv || place)
  			{
  				listeIntervalles [indice] = listeProv [i];
  			}
  			else
  			{
  				listeIntervalles [indice] = interv;
  				indice++;
  				listeIntervalles [indice] = listeProv [i];
  				place = true;
  			}
  			indice++;
  		}
  		if (!place)
  			listeIntervalles [listeIntervalles.length-1] = interv;	
  	}
  	place = false;
  	isInside = false;
  	for (int i = 0;i<listeExp.length;i++)
  	{
  		if (listeExp [i] == exp)
  			isInside = true;
  	}
  	if (!isInside)
  	{
  		int []listeProv = listeExp;
  		listeExp = new int [listeProv.length+1];
  		int indice = 0; 
  		for (int i = 0;i<listeProv.length;i++)
  		{
  			if (listeProv [i] < exp || place)
  				listeExp [indice] = listeProv [i];
  			else
  			{
  				listeExp [indice] = exp;
  				indice++;
  				listeExp [indice] = listeProv [i];
  				place = true;
  			}
  			indice++;
  		}	
  		if (!place)
  			listeExp [listeExp.length-1] = exp;	
  	}
  			 
  	
  }
  	
  
  public void windowClosed(WindowEvent we) {}
  /**
   * This method is called when the user tries to close the window (via
   * system menu). It just terminates the application.
   */
  public void windowClosing(WindowEvent we) {
    System.exit(0);
  }
  
  public void windowActivated(WindowEvent we) {
  }
  /** We need to implement this method for the WindowListener interface, but it's not used */
  public void windowDeactivated(WindowEvent we) {}
  /** We need to implement this method for the WindowListener interface, but it's not used */
  public void windowDeiconified(WindowEvent we) {}
  /** We need to implement this method for the WindowListener interface, but it's not used */
  public void windowIconified(WindowEvent we) {}
  /** We need to implement this method for the WindowListener interface, but it's not used */
  public void windowOpened(WindowEvent we) {}
  
  
  
  public void itemStateChanged (ItemEvent IE){
  	if (IE.getSource() == cExp)
  	{
  		int valeur = (new Integer (cExp.getSelectedItem())).intValue();
  		nm.setEntryExp ((new Integer (cExp.getSelectedItem())).intValue());
  	}
  	else 
  	{
  		if (IE.getSource() == cInterv)
  		{
	  		sender.setInterval ((new Integer (cInterv.getSelectedItem())).intValue());
 		} 	
  	}
  }
}
