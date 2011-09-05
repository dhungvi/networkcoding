package ch.epfl.lsr.adhoc.routing.ncode.fast;


public class GField {
  // GF initialization
  int Lfield;	    
  int GF_Size; 

  //  public static int COLBIT[];
  public int BIT[];
  public int ExptoFE[];
  public int FEtoExp[];

  public GField(int Lfield){
	this.Lfield=Lfield;
	int TableLength = 1<<Lfield;
	GF_Size = TableLength-1;
//    COLBIT = new int[Lfield+1];
    BIT = new int[Lfield+1];
    ExptoFE = new int[TableLength+Lfield];
    FEtoExp = new int[TableLength];

    initField(BIT, ExptoFE, FEtoExp, Lfield);
  }

  /**
   * Retrieve the Field Size.
   **/
  public int getLfield() 
  {
    return Lfield;
  }

  /**
   * Retrieve the Field Size.
   **/
  public int getGF_SIze() 
  {
    return GF_Size;
  }

  /**
   * Retreve the BIT table for the input Lfield. */
  public int[] getBIT()
  {
    return BIT;
  }

  /**
   * Retreve the ExptoFE table for the input Lfield.
   **/
  public int[] getExptoFE()
  {
    return ExptoFE;
  }

  /**
   * Retrieve the FEtoExp table for the input Lfield.
   **/
  public int[] getFEtoExp() 
  {
    return FEtoExp;
  }

  
  /**
   * initField initializes the finite Field Element to Exponent (FEtoExp)
   *   table and the Exponent to finite Field Element (ExptoFE) table.
   *
   * Recall SMultField = TableLength - 1 is the number of 
   *   elements in the multiplicative group of the field. 
   * 
   * @param BIT == The BIT array is used to mask out single bits in 
   *               equations: bit 
   * @param ExptoFE == ExptoFE is the table that goes from the exponent to the 
   *                   finite field element. 
   * @param FEtoExp == FEtoExp is the table that goes from the finite field 
   *                   element to the exponent.
   * @param Lfield == Lfield is the log of the length of the field.. */
  public static void initField(int[] BIT, int[] ExptoFE, 
			       int[] FEtoExp, int Lfield)
  {
    /** 
     * Recall SMultField = TableLength - 1 is the number of 
     *   elements in the multiplicative group of the field. */ 
    int SMultField = (1 << Lfield) - 1;

    /**
     * CARRYMASK is used to see when there is a carry in the polynomial
     * and when it should be XOR'd with POLYMASK. */
    int CARRYMASK;
    
    /**
     * POLYMASK is the irreducible polynomial. */
    int POLYMASK[] = {0x0, 0x3, 0x7, 0xB, 0x13, 0x25, 0x43, 0x83, 
		      0x11D, 0x211, 0x409, 0x805, 0x1053, 0x201B, 
		      0x402B, 0x8003, 0x1100B} ;
    int i ;
    
    BIT[0] = 0x1 ;
    
    for(i=1; i < Lfield ; i++)  BIT[i] = BIT[i-1] << 1 ;
    CARRYMASK = BIT[Lfield-1] << 1 ;
    ExptoFE[0] = 0x1 ;
    
    
    for(i = 1; i < SMultField + Lfield - 1; i++)
    {
      ExptoFE[i] = ExptoFE[i-1] << 1;
      if((ExptoFE[i] & CARRYMASK) > 0) 
    	  ExptoFE[i] ^= POLYMASK[Lfield] ;
    }
      
    FEtoExp[0] = -1 ;
    for(i=0; i < SMultField ; i++)	
      FEtoExp[ExptoFE[i]] = i;
  }
  
  public int sum(int x,int y) {
		return x^y;  
  }

  public int minus(int x,int y) {
		return x^y;  
  }

  public int product(int x,int y) {
	  if ((x==0) || (y==0)) {
		  return 0;
	  }
	return ExptoFE[(FEtoExp[x]+FEtoExp[y])%GF_Size];  
  }

  public int divide(int x,int y) {
	  if (x ==0) {
		  return 0;
	  }
	  if (y==0) {
		  System.out.println("DIVISION ERROR!");
		  return 0;
	  }
	  int divExp=FEtoExp[x]-FEtoExp[y];
	  if (divExp<0) {
		  divExp+=GF_Size;
	  }
	  return ExptoFE[divExp];  
  }
  
}

