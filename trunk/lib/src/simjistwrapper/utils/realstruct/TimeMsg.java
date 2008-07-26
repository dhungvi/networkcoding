/*
 * Created on Jan 30, 2006
 *
 */
package simjistwrapper.utils.realstruct;

public class TimeMsg
{
    private long consoTime;
    private byte[] byteMsg;
    
    public TimeMsg(long consoTime, byte[] byteMsg)
    {
        this.consoTime = consoTime;
        this.byteMsg = byteMsg;
    }
    
    public byte[] getMsg()
    {
        return byteMsg;
    }
    
    public long getConsoTime()
    {
        return consoTime;
    }
}
