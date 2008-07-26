/*
 * Created on Oct 24, 2005
 *
 */
package simjistwrapper.utils.simstruct;

import java.util.ArrayList;

import simjistwrapper.utils.realstruct.*;


public class OrderList
{
    private ArrayList orders;
    
    public OrderList()
    {
        orders = new ArrayList();
    }

    public void add(Order order)
    {
        orders.add(order);
    }
    
    public Order get(int i)
    {
        return (Order)(orders.get(i));
    }
    
    public int size()
    {
        return orders.size();
    }
}
