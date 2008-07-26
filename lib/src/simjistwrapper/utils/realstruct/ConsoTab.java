/*
 * Created on Jan 30, 2006
 *
 */
package simjistwrapper.utils.realstruct;

import java.util.*;

public class ConsoTab implements Comparator
{
    private TreeSet treeSet;
    
    public ConsoTab()
    {
        treeSet = new TreeSet(this);
    }
    
    public synchronized void put(TimeMsg timeMsg)
    {
        treeSet.add(timeMsg);
        notify();
    }

    public synchronized TimeMsg getFirst()
    {
        while(treeSet.isEmpty())
            try
            {
                wait();
            } catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        
        TimeMsg tmp = (TimeMsg) treeSet.first();
        treeSet.remove(tmp);
        
        long currentTime;
        while(tmp.getConsoTime() > (currentTime = System.currentTimeMillis()))
            try
            {
                wait(tmp.getConsoTime() - currentTime);
            } catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            
        return tmp;
    }

    public int compare(Object obj1, Object obj2)
    {
        int answer;
        if(((TimeMsg) obj1).getConsoTime() < ((TimeMsg) obj2).getConsoTime())
            answer = -1;
        else if(((TimeMsg) obj1).getConsoTime() > ((TimeMsg) obj2)
                .getConsoTime())
            answer = 1;
        else
            answer = 0;
        return answer;
    }
}
