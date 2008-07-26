/*
 * Created on Oct 17, 2005
 *
 */
package simjistwrapper.utils.realstruct;

public class Order
{
    private int modelNbre;
    private int qtity;
    
    public Order(int modelNbre, int qtity)
    {
        this.modelNbre = modelNbre;
        this.qtity = qtity;
    }

    /**
     * @return Returns the modelNbre.
     */
    public int getModelNbre()
    {
        return modelNbre;
    }

    /**
     * @return Returns the qtity.
     */
    public int getQtity()
    {
        return qtity;
    }
    
    public void showAll()
    {
        System.out.println("model " + modelNbre + " ordered " + qtity + " times");
    }
    
}
