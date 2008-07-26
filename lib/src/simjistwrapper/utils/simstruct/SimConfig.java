package simjistwrapper.utils.simstruct;

import java.io.File;

import simjistwrapper.exceptions.*;
import simjistwrapper.factories.fieldfactory.*;
import simjistwrapper.factories.jsnodefactory.*;
import simjistwrapper.factories.orderfactory.*;
import simjistwrapper.utils.simstruct.OrderList;
import simjistwrapper.utils.simstruct.SimParamList;
import simjistwrapper.xml.*;
import org.w3c.dom.*;

// TODO check the implements underneath
public class SimConfig //implements ConfigInterface
{
	public SimConfig(String simFileName) throws ParsingException,
			SemanticException, SimRuntimeException
	{
		Document document = new ParserSimFile().parse(new File(simFileName));
		this.inits(document);

		Element element = document.getDocumentElement();
		NodeList orders = element.getElementsByTagName(SimParamList.Order);
		OrderList orderList = OrderFactory.createOrders(orders);
		JSNodeFactory.createNodes(FieldFactory.getField(), FieldFactory
				.getBounds(), orderList);
	}
	
	/*
	 * should I do smthg like: this method MUST be called ? (for developers...)
	 */
	private void inits(Document document) throws SemanticException
	{
		SimParamList.init();
		OrderFactory.init(document);
		JSNodeFactory.init(document);
		FieldFactory.init(document);
	}
}
