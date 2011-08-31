package ch.epfl.lsr.adhoc.routing.ncode;

//import Packet;
//import Vector;
/**
 * Class NCdatagram
 * 
 */

import java.util.*;

public class NCdatagram {
	protected ExtendedGaloisField GF;
	public int dataLength;
	protected int Index_;
	public Hashtable<String,Coef_Elt> coefs_list;
	public byte[] Buf;
	private boolean decoded_;

	// Methods
	// Constructors
	public NCdatagram(byte[] buf, int length, int Index) {
		coefs_list = new Hashtable<String,Coef_Elt>();
		dataLength=Math.min(length,NCGlobals.MaxBufLength);
		if (dataLength>0) {
			Buf=new byte[dataLength];
			System.arraycopy(buf,0,Buf,0,dataLength);		
		} 
		Index_=Index;
	}

	public NCdatagram(int Index) {
		coefs_list = new Hashtable<String,Coef_Elt>();
		dataLength=0;
		Buf=new byte[NCGlobals.MaxBufLength-1];
		Index_=Index;
		GF=NCGlobals.GF;
	}


	public int getIndex(  ) {
		return Index_;
	}

	public void setIndex( int value  ) {
		Index_ = value;
	}



	public Object clone() {		
		Coef_Elt coef,coefp;

		NCdatagram g = new NCdatagram(Buf,dataLength, Index_);

		Enumeration<Coef_Elt> ec = coefs_list.elements();
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

		Enumeration<Coef_Elt> ec = coefs_list.elements();
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
		long nodeID;

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

		Enumeration<Coef_Elt> ec = coefs_list.elements();
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
			nodeID=coef.nodeID_;
			data[index++] = (byte)'l';
			data[index++] = (byte)(nodeID >> 56);
			data[index++] = (byte)(nodeID >> 48);
			data[index++] = (byte)(nodeID >> 40);
			data[index++] = (byte)(nodeID >> 32);			
			data[index++] = (byte)(nodeID >> 24);
			data[index++] = (byte)(nodeID >> 16);
			data[index++] = (byte)(nodeID >>  8);
			data[index++] = (byte)nodeID;
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
		int index = 0;
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
			if(data[index++] != 'l') {
				throw new RuntimeException("No long found");
			}
			coef_elt.nodeID_= ((data[index++] & 0xFFL) << 56) +  
			((data[index++] & 0xFFL) << 48)+
			((data[index++] & 0xFFL) << 40)+
			((data[index++] & 0xFFL) << 32)+
			((data[index++] & 0xFFL) << 24)+
			((data[index++] & 0xFFL) << 16)+
			((data[index++] & 0xFFL) << 8)+
			((data[index++] & 0xFFL));
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
	}


	public int validate() {
		Coef_Elt coef;
		int sum=0,I=0;

		if(coefs_list.size()>0) {
			Enumeration<Coef_Elt> ec = coefs_list.elements();
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
					e.printStackTrace();
				}
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
				e.printStackTrace();
			}
		}
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

		int i;
		Coef_Elt coeftp;

		// handling the coefs_list
		Enumeration<Coef_Elt> ec = coefs_list.elements();
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

		int i;

		Coef_Elt coef, coefp;


		synchronized(g) {
			Enumeration<Coef_Elt> egp=g.coefs_list.elements();
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
		int i;

		Coef_Elt coef, coefp;

		synchronized(g) {
			Enumeration<Coef_Elt> egp=g.coefs_list.elements();
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

