package simjistwrapper.factories.orderfactory;

import java.util.ArrayList;

import simjistwrapper.utils.realstruct.*;
import simjistwrapper.utils.simstruct.*;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class OrderFactory
{
	private static OrderChecker orderChecker = null;
	
	public static void init(Document document)
	{
		orderChecker = new OrderChecker(document);
	}

	public static OrderList createOrders(NodeList orders)
	{
		OrderList orderList = new OrderList();
		for(int i = 0; i < orders.getLength(); i++)
		{
			Node currentOrder = orders.item(i);
			NodeList leaves = currentOrder.getChildNodes();
			int modelNumber = 0;
			int qtity = 0;
			for (int j = 0; j < leaves.getLength(); j++)
			{
				Node currentLeaf = leaves.item(j);
				if (currentLeaf.getNodeName().equals(SimParamList.orderedmodel))
					modelNumber = Integer.valueOf(
							currentLeaf.getFirstChild().getNodeValue())
							.intValue();
				else if (currentLeaf.getNodeName().equals(SimParamList.qtity))
					qtity = Integer.valueOf(
							currentLeaf.getFirstChild().getNodeValue())
							.intValue();
				else
					throw new RuntimeException(
							"in order, neither orderedmodel neither qtity");
			}
			
			if(orderChecker.check(modelNumber))
				orderList.add(new Order(modelNumber, qtity));
		}
		
		return orderList;
	}
}
