/*
 * Created on Oct 31, 2005
 *
 */
package simjistwrapper.utils.simstruct;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.tools.ParserXml;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Config;
import org.w3c.dom.Node;

public class SubConfig extends Config //implements ConfigInterface
{
    private JSNode jsNode;
    
    public SubConfig(String francFileName)
    {
        super(francFileName);
    }
    
    public AsynchronousLayer createDataLinkLayer()
    {
        // TODO put the two lines underneath in one line (all in the constructor ?)
        // AsynchronousLayer datalink = new AsyncBroadcast("datalink layer");
        //((AsyncBroadcast)(datalink)).setConfig(this);
    	if(jsNode == null)
    		throw new RuntimeException("jsNode is null (in SubConfig)");
      JSNode.NestedAsyncBroadcast dataLinkLayer = jsNode.getDatalinkLayer();
      Node node = franc.getElementsByTagName(ParserXml.DATA_LINK_LAYER).item(0);
      Parameters params = getParameters(node);
      dataLinkLayer.setParameters(params);
      return dataLinkLayer;
    }
    
    protected void loadConfig() {
        super.loadConfig();
    }

	protected FrancRuntime createRuntime() {
        return 	super.createRuntime();
	}

	public void setJsNode(JSNode jsNode)
	{
		System.out.println("--> call to SubConfig.setJsNode()");
		if(jsNode == null)
			throw new RuntimeException("jsNode is null (in SubConfig.set)");
		this.jsNode = jsNode;
	}
}
