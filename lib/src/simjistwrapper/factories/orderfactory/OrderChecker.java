package simjistwrapper.factories.orderfactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import simjistwrapper.utils.simstruct.SimParamList;

public class OrderChecker
{
	//private Document document;
	private ArrayList nodeModelList;
	
	public OrderChecker(Document document)
	{
		//this.document = document;
		this.checkRedundancy(document);
		this.createNodeModelList(document);
	}
	
	/*
	 * this method checks that one node model is ordered during one unique
	 * order.
	 */ 
	// TODO rise an error or do nothing -> the addition will be done auto.
	private void checkRedundancy(Document document)
	{
		ArrayList modelRqd = new ArrayList();
		Element element = document.getDocumentElement();
		NodeList orders = element.getElementsByTagName(SimParamList.Order);
		for(int i = 0; i < orders.getLength(); i++)
		{
			Node currentOrder = orders.item(i);
			NodeList leaves = currentOrder.getChildNodes();
			for (int j = 0; j < leaves.getLength(); j++)
			{
				Node currentLeaf = leaves.item(j);
				if (currentLeaf.getNodeName().equals(SimParamList.orderedmodel))
				{
					Integer thisInt = new Integer(currentLeaf.getFirstChild()
							.getNodeValue());
					if(modelRqd.contains(thisInt))
						System.err
								.println("REDUNDANCY DETECTED !\nSimulation continues");
					else
						modelRqd.add(thisInt);
				}
				
			}
		}
	}
	
	private void createNodeModelList(Document document)
	{
		Element element = document.getDocumentElement();
		NodeList nodeModels = element
				.getElementsByTagName(SimParamList.NodeModel);
		nodeModelList = new ArrayList(nodeModels.getLength());
		for (int i = 0; i < nodeModels.getLength(); i++)
			nodeModelList.add(new Integer(((Element) nodeModels.item(i))
					.getAttribute("id")));
	}
	
	public boolean check(int modelNumber)
	{
		boolean answer = true;
		if (!nodeModelList.contains(new Integer(modelNumber)))
		{
			System.err
					.println("ERROR DETECTED.. YOU'RE ASKING FOR AN UNKNOWN MODEL : "
							+ modelNumber);
			System.err
					.println("SIMULATION CONTINUES, BUT THIS ORDER WON'T BE TAKEN INTO ACCOUNT");
			answer = false;
		}
		return answer;
	}
}
