package ch.epfl.lsr.adhoc.routing.ncode;

//import Packet;
//import Vector;
/**
 * Class NCdatagram
 * 
 */

import java.lang.*;
import java.net.*;
import java.util.*;

public class NCdatagram {
  	// Fields
  	// 

  	//public long gen_;
	private GaloisField base;
	private ExtendedGaloisField GF;
  	public int dataLength;
  	private int Index_;
//  	public Vector coefs_list;
  	public Hashtable coefs_list;
	public byte[] Buf;
	private boolean decoded_;
	
  	// Methods
  	// Constructors
  	public NCdatagram(byte[] buf, int length, int Index) {
//		coefs_list = new Vector();
  		coefs_list = new Hashtable();
		dataLength=Math.min(length,Globals.MaxBufLength);
		if (dataLength>0) {
			Buf=new byte[dataLength];
		    System.arraycopy(buf,0,Buf,0,dataLength);		
		} 
		Index_=Index;
	}

  	public NCdatagram(int Index) {
		//coefs_list = new Vector();
		coefs_list = new Hashtable();
		dataLength=0;
		Buf=new byte[Globals.MaxBufLength-1];
		Index_=Index;
		GF=Globals.GF;
	}

  	
//  	private long getGen_ (  ) {
//    		return gen_;
//  	}
//  	private void setGen_ ( long value  ) {
//   		gen_ = value;
//  	}

  	public int getIndex(  ) {
		return Index_;
	}

  	public void setIndex( int value  ) {
		Index_ = value;
	}



//	public NCdatagram clone() {
  	public Object clone() {		
		int i;
		Coef_Elt coef,coefp;

		NCdatagram g = new NCdatagram(Buf,dataLength, Index_);
		
		Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coef = (Coef_Elt) ec.nextElement();
			coefp =(Coef_Elt) coef.clone();
			g.coefs_list.put(coefp.key(),coefp);				
	    } 		
//	    System.arraycopy(Buf,0,g.Buf,0,dataLength);		
		return g;
	}
  	
  	public boolean isNull() {
  		if (coefs_list.size()==0) {
  			return true;
  		} else {
  			return false;
  		}	
  	}
  	
  	public void setBuf(byte[] buf,int length){
  		dataLength=length;
	    System.arraycopy(buf,0,Buf,0,dataLength);		
  	}

  	public int getBuf(byte[] buf){
	    System.arraycopy(Buf,0,buf,0,dataLength);
  		return dataLength;
  	}
  	
  	public void setLength(int length) {
  		dataLength=length;
  	}

  	public int getLength() {
  		return dataLength;
  	}
  	
  	public void setDecoded(){
  		decoded_=true;
  	}
  	
  	public void resetDecoded(){
  		decoded_=false;
  	}
  	
  	public boolean isDecoded(){
  		return decoded_;
  	}
  	
  	public void free(CoefEltPool cfpool){
  	  	
  		dataLength=0;
  	  	Index_=0;
  	  	freeCoefs_list(cfpool);
  		for (int i=0; i<Buf.length;i++) {
  			Buf[i]=0;
  		}  		
  	}

  	public void freeCoefs_list(CoefEltPool cfpool){
  	  	Coef_Elt coef;

  	  	Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coef = (Coef_Elt) ec.nextElement();
  	  		coef.free();
  	  		cfpool.freeCoef(coef);
	    } 		
  	  	coefs_list.clear();
  	}
  	
  	
  	
  	public synchronized int serialize(byte[] data){
  	  	int index =0;
  	  	Coef_Elt coef;
  	  	byte[] add=new byte[4];
  	  	
  	  	// Adding Index
  	  	int i=Index_;
  	  	data[index++] = (byte)'i';
  	  	data[index++] = (byte)(i >> 24);
  	  	data[index++] = (byte)(i >> 16);
  	  	data[index++] = (byte)(i >>  8);
  	  	data[index++] = (byte)i;
  	  	//Adding number of coefs
  	  	byte b=int2byte(coefs_list.size());
  		data[index++] = (byte)'b';
  		data[index++] = b;
  		int s=b;

  	  	Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
  			// Adding coefs
			coef = (Coef_Elt) ec.nextElement();			  		  		
  			b=int2byte(coef.getCoef());
  	  		data[index++] = (byte)'b';
  	  		data[index++] = b;
  	  	  	i=coef.index_;
  	  	  	data[index++] = (byte)'i';
  	  	  	data[index++] = (byte)(i >> 24);
  	  	  	data[index++] = (byte)(i >> 16);
  	  	  	data[index++] = (byte)(i >>  8);
  	  	  	data[index++] = (byte)i;
  	  	  	i=coef.length;
  	  	  	data[index++] = (byte)'i';
  	  	  	data[index++] = (byte)(i >> 24);
  	  	  	data[index++] = (byte)(i >> 16);
  	  	  	data[index++] = (byte)(i >>  8);
  	  	  	data[index++] = (byte)i;
 
  	  	  	data[index++] = (byte)'B';
  	  	  	int length=4;
  	  	  	i=length;
  	  	  	data[index++] = (byte)'i';
  	  	  	data[index++] = (byte)(i >> 24);
  	  	  	data[index++] = (byte)(i >> 16);
  	  	  	data[index++] = (byte)(i >>  8);
  	  	  	data[index++] = (byte)i;
  	  	  	add=coef.saddr.getAddress();
  	  	  	System.arraycopy(add,0,data,index,length);
  	  	  	index = index + length;
  		}
  		data[index++] = (byte)'B';
  		i=dataLength;
  		int length=dataLength;
	  	data[index++] = (byte)'i';
  	  	data[index++] = (byte)(i >> 24);
  	  	data[index++] = (byte)(i >> 16);
  	  	data[index++] = (byte)(i >>  8);
  	  	data[index++] = (byte)i;
  	    System.arraycopy(Buf,0,data,index,dataLength);
  	    index = index + length;
 	    return index;
  	}
  	
  	public synchronized  int unserialize(byte[] data, CoefEltPool cfpool) {
  		byte coef;
  		Coef_Elt coef_elt;
  		byte[] add=new byte[4];  
  		int index = 0;
//  		try{
	  		freeCoefs_list(cfpool);
	  		// get Index_
	  		if(data[index] != 'i') {
	  			throw new RuntimeException("No integer found");
	  		}
	  		index++;
	  		int i = ((data[index++] & 0xFF) << 24) +
	  				((data[index++] & 0xFF) << 16) +
	  				((data[index++] & 0xFF) <<  8) +
	  				(data[index++] & 0xFF);
	  		Index_=i;
	  		// get number of coefs
	  		if(data[index] != 'b') {
	  			throw new RuntimeException("No byte found");
	  		}
	  		index++;
	  		byte b = data[index++];
	  		int s=b ;
	  		for (int j=0; j < s; j++) {
	  	  		// get coef
	  			if(data[index] != 'b') {
	  				throw new RuntimeException("No byte found");
	  			}
	  			index++;
	  			b = data[index++];
	  			coef=b;
	  			coef_elt=cfpool.getCoefElt(coef,0);
	  	  		// get Pkt_ID.index_ 
	  	  		if(data[index] != 'i') {
	  	  			throw new RuntimeException("No integer found");
	  	  		}
	  	  		index++;
	  	  		i = ((data[index++] & 0xFF) << 24) +
	  	  				((data[index++] & 0xFF) << 16) +
	  	  				((data[index++] & 0xFF) <<  8) +
	  	  				(data[index++] & 0xFF);
	  			coef_elt.index_=i;
	  			// get coef.length
	  	  		if(data[index] != 'i') {
	  	  			throw new RuntimeException("No integer found");
	  	  		}
	  	  		index++;
	  	  		i = ((data[index++] & 0xFF) << 24) +
	  	  				((data[index++] & 0xFF) << 16) +
	  	  				((data[index++] & 0xFF) <<  8) +
	  	  				(data[index++] & 0xFF);
	  	  		coef_elt.length=i;
	  	  		// get Pkt_ID.saddr
	  	  		if(data[index] != 'B') {
	  	  			throw new RuntimeException("No Byte array found");
	  	  		}
	  	  		index++;
	  	  		if(data[index] != 'i') {
	  	  			throw new RuntimeException("No integer found");
	  	  		}
	  	  		index++;
	  	  		i = ((data[index++] & 0xFF) << 24) +
	  	  				((data[index++] & 0xFF) << 16) +
	  	  				((data[index++] & 0xFF) <<  8) +
	  	  				(data[index++] & 0xFF);
	  	  		int len= i;  	  		
	  	  		System.arraycopy(data, index, add, 0, len);
	  	  		index += len;
	  	  		try {
	  	  			coef_elt.saddr=InetAddress.getByAddress(add);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				coefs_list.put(coef_elt.key(),coef_elt);
	  		}
	  		// get NCdatagram payload
	  		if(data[index] != 'B') {
	  			throw new RuntimeException("No Byte array found at "+index);
	  	  	}
	  		index++;
	  		if(data[index] != 'i') {
	  			throw new RuntimeException("No integer found");
	  		}
	  		index++;
	  		i = ((data[index++] & 0xFF) << 24) +
	  			((data[index++] & 0xFF) << 16) +
	  			((data[index++] & 0xFF) <<  8) +
	  			(data[index++] & 0xFF);
	  	  	int len= i;  	  		
	  	  	System.arraycopy(data, index, Buf, 0, len);
	  	  	index += len;
	  	  	//now set datalength
	  	  	dataLength=len;
//	  	  	validate();
	  	  	return index;
//  		} catch(RuntimeException e) {
  			// Exception in decoding the packet
  			// Should not happen but seems to happen because of fragmentation !!!
//  			return 0;
//  		}
  	}
  	  		
  	
  	public int validate() {
  		Coef_Elt coef;
		int sum=0,I=0;
		
		if(coefs_list.size()>0) {
	  	  	Enumeration ec = coefs_list.elements();
			while(ec.hasMoreElements())
			{
				coef=(Coef_Elt) ec.nextElement();				
				sum=GF.sum(sum, coef.coef_); 
			}
			sum=GF.product(sum,32);
			if (sum!=0) {
				try {
					if (GF.divide(Buf[1],sum)!=1 && (sum !=0)) {
						System.out.println("Encoding Error !!");
						I=-1;
					}
				} catch (GaloisException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (dataLength !=74 ) {
//				System.out.println("DATALENGTH ! :"+dataLength);
			}
		}
		return I;		
	}

  	public int line_validate(int[] B, int L ) {
		int sum=0,I=0;
		
		for(int i=0; i < L; i++) {
			sum=GF.sum(sum, B[i]); 
		}
		sum=GF.product(sum,32);
		if (sum!=0) {
			try {
				if (GF.divide(Buf[1],sum)!=1 && (sum !=0)) {
					System.out.println("Encoding Error !!");
					I=-1;
				}
			} catch (GaloisException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		if (dataLength !=74 ) {
//			System.out.println("DATALENGTH ! :"+dataLength);
//		}
		return I;		
	}

  	public byte int2byte(int val) {
		return (byte) val;
	}

	public int byte2int(byte val){
		int i=val;
		if (i <0 ) {
			i=i+256;
			return i;
		} else {
			return i;
		}
	}
	
	public synchronized NCdatagram product(int coef) throws GaloisException {

		int i,j,k;
		Coef_Elt coeftp;
		
		// handling the coefs_list
  	  	Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coeftp=(Coef_Elt) ec.nextElement();
			coeftp.coef_ = GF.product(coeftp.coef_ , coef);
		}
		

		// handling the data in the StringBuffer
		for(i = 0; i < dataLength ; i++) {			
			Buf[i]=int2byte(GF.product(byte2int(Buf[i]), coef)); 				
		}				
		return this;
	}

//	--------------------------------------------------------------------	
	public synchronized NCdatagram sum(NCdatagram g,CoefEltPool cf) throws GaloisException {

		int i,j;

		Coef_Elt coef, coefp;
		boolean found;
		
		
		synchronized(g) {
			Enumeration egp=g.coefs_list.elements();
			while (egp.hasMoreElements()) {
				coefp=(Coef_Elt) egp.nextElement();
				coef= (Coef_Elt) coefs_list.get(coefp.key());
				if (coef != null) {
					coef.coef_=GF.sum(coef.getCoef(),coefp.getCoef());
					if (coef.coef_==0) {
						coefs_list.remove(coef.key());
					} 
				}else {
					coef=cf.coefclone(coefp);
					coefs_list.put(coef.key(),coef);	
				}
			}
			if (dataLength>=g.dataLength){
				for(i = 0; i < g.dataLength ; i++) {			
					Buf[i]=int2byte(GF.sum(Buf[i], g.Buf[i]));				
				}	
			} else {
				for(i = 0; i < dataLength ; i++) {			
					Buf[i]=int2byte(GF.sum(Buf[i], g.Buf[i]));				
				}
				System.arraycopy(g.Buf,dataLength,Buf,dataLength,g.dataLength-dataLength);
			}
			dataLength=Math.max(dataLength,g.dataLength);
		}
		return this;
	}

//--------------------------------------------------------------------	

	public NCdatagram minus(NCdatagram g, CoefEltPool cf) throws Exception {
		int i,j;

		Coef_Elt coef, coefp;
		
		synchronized(g) {
			Enumeration egp=g.coefs_list.elements();
			while (egp.hasMoreElements()) {
				coefp=(Coef_Elt) egp.nextElement();
				coef= (Coef_Elt) coefs_list.get(coefp.key());
				if (coef != null) {
					coef.coef_=GF.minus(coef.getCoef(),coefp.getCoef());
					if (coef.coef_==0) {
						coefs_list.remove(coef.key());
					}
				} else {
					coef=cf.coefclone(coefp);
					coefs_list.put(coef.key(),coef);	
				}
			}
		}
		
		if (dataLength>=g.dataLength){
			for(i = 0; i < g.dataLength ; i++) {			
				Buf[i]=int2byte(GF.minus(Buf[i], g.Buf[i]));				
			}	
		} else {
			for(i = 0; i < dataLength ; i++) {			
				Buf[i]=int2byte(GF.minus(Buf[i], g.Buf[i]));				
			}
			for(i = dataLength; i < g.dataLength ; i++) {
				Buf[i]=int2byte(GF.minus(0, g.Buf[i]));
			}
		}		
		return this;
	}
}

