/*
 * Created on Oct 24, 2005
 *
 */
package simjistwrapper.utils.simstruct;

import java.util.ArrayList;

import simjistwrapper.factories.jsnodefactory.JSNodeModel;


public class ModelList
{
    private ArrayList list;

    public ModelList()
    {
        list = new ArrayList();
    }
    
    public void add(JSNodeModel model)
    {
        list.add(model);
    }
    
    public JSNodeModel get(int i)
    {
        return (JSNodeModel)(list.get(i));
    }
    
    public int size()
    {
        return list.size();
    }
}
