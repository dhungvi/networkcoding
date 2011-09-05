package ch.epfl.lsr.adhoc.routing.ncode.fast;

/**
 * Class NCdatagram
 * 
 */

import java.lang.*;
import java.net.*;
import java.util.*;

import ch.epfl.lsr.adhoc.routing.ncode.CoefEltPool;
import ch.epfl.lsr.adhoc.routing.ncode.Coef_Elt;
import ch.epfl.lsr.adhoc.routing.ncode.fast.NCGlobals;

public class NCFastdatagram
{
  	// Fields
  	// 

 	private GField GF;
  	public int dataLength;
  	private int Index_;
  	public Hashtable<String,Coef_Elt> coefs_list;
	public int[] Buf;
	private int NInt;
	private int NSegs;
	
	public NCFastdatagram(int[] buf, int length, int Index) {
		GF=NCGlobals.GF;
  		coefs_list = new Hashtable<String,Coef_Elt>();
		dataLength=Math.min(length,NCGlobals.MaxBufLength);
		NSegs=(int)Math.ceil(dataLength/(4.0*GF.getLfield()));
		NInt=NSegs*GF.getLfield();

		if (dataLength>0) {
			Buf=new int[NInt];
		    System.arraycopy(buf,0,Buf,0,NInt);		
		} 
		Index_=Index;

	}

	public NCFastdatagram(int Index) {
		coefs_list = new Hashtable<String,Coef_Elt>();
		dataLength=0;
		Buf=new int[NCGlobals.MaxBufLength-1];
		Index_=Index;
	}

	
  	public NCFastdatagram(int Index,GField GF) {
		//coefs_list = new Vector();
		coefs_list = new Hashtable<String,Coef_Elt>();
		NSegs=0;
		NInt=0;
		dataLength=0;
		Buf=new int[NCGlobals.MaxBufLength-1];
		Index_=Index;
		this.GF=GF;
  	}

  	public int getIndex(  ) {
		return Index_;
	}

  	public void setIndex( int value  ) {
		Index_ = value;
	}

  	public Object clone() {		
		Coef_Elt coef,coefp;

		NCFastdatagram g = new NCFastdatagram(Buf,dataLength, Index_);
		
		Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coef = (Coef_Elt) ec.nextElement();
			coefp =(Coef_Elt) coef.clone();
			g.coefs_list.put(coefp.key(),coefp);				
	    } 		
		return g;
	}
  	
  	public boolean isNull() {
  		if (coefs_list.size()==0) {
  			return true;
  		} else {
  			return false;
  		}	
  	}
  	
  	public void setBuf(int[] buf,int length){
  		dataLength=length;
		NSegs=(int)Math.ceil(dataLength/(4.0*GF.getLfield()));  		
		NInt=NSegs*GF.getLfield();
		System.arraycopy(buf,0,Buf,0,NInt);		
  	}

  	public int getBuf(int[] buf){
	    System.arraycopy(Buf,0,buf,0,NInt);
  		return NInt;
  	}
  	
  	public void setLength(int length) {
  		dataLength=length;
		NSegs=(int)Math.ceil(dataLength/(4.0*GF.getLfield()));
		NInt=NSegs*GF.getLfield();
  	}

  	public int getLength() {
  		return dataLength;
  	}

  	public int getNInt() {
  		return NInt;
  	}

  	public int getNSegs() {
  		return NSegs;
  	}  	
  	
  	public void free(CoefEltPool cfpool){
  	  	
  		dataLength=0;
  		NSegs=0;
  		NInt=0;
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
	  	data[index++] = (byte)'i';
  	  	data[index++] = (byte)(i >> 24);
  	  	data[index++] = (byte)(i >> 16);
  	  	data[index++] = (byte)(i >>  8);
  	  	data[index++] = (byte)i;
  		for (int j=0; j < NInt;j++) {
  			i=Buf[j];
  			data[index++]= (byte)(i >> 24);
  			data[index++] = (byte)(i >> 16);
  			data[index++] = (byte)(i >>  8);
  			data[index++] = (byte)i;
  		}
  	  	return index;
  	}
  	
	public synchronized  int unserialize(byte[] data, int len, CoefEltPool cfpool) {
		byte coef;
  		Coef_Elt coef_elt;
		int index = 0;
  		try{
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
	  	  	dataLength=i;
	  	  	
	  	  	int normPayloadSize=len-index;
	  	  	if ((normPayloadSize%(4*GF.getLfield()) !=0) || (normPayloadSize < dataLength)) {
	  	  		throw new RuntimeException("normPayloadSize problem");	  	  		
	  	  	}

	  	  	NSegs=(int)Math.ceil(dataLength/(4.0*GF.getLfield()));
			NInt=NSegs*GF.getLfield();
	  	  	for (int j=0;j < NInt;j++) {
	  	  		Buf[j] = ((data[index++] & 0xFF) << 24) +
	  	  		((data[index++] & 0xFF) << 16) +
	  	  		((data[index++] & 0xFF) <<  8) +
	  	  		(data[index++] & 0xFF);
	  	  	}
//	  	  	toIntBuf(data,index,normPayloadSize);
	  		return index;	  		
  		} catch(RuntimeException e) {
  			// Exception in decoding the packet
  			// Should not happen but seems to happen because of fragmentation !!!
  			return 0;
  		}
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
	
	public synchronized NCFastdatagram product(int coef) {
		int k,l;
		Coef_Elt coeftp;
		int[] Bufp=new int[Buf.length];
		// handling the coefs_list
  	  	Enumeration ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coeftp=(Coef_Elt) ec.nextElement();
			coeftp.coef_ = GF.product(coeftp.coef_ , coef);
		}
				
		int Lfield=GF.getLfield();
		int ExpFE=GF.FEtoExp[coef];
		// handling the data in the Buffer
		for(int row_eqn=0; row_eqn < Lfield; row_eqn++) {
		  k = row_eqn * NSegs;
		  for(int col_eqn=0; col_eqn < Lfield; col_eqn++){
		    l = col_eqn * NSegs;  
		    if ((GF.ExptoFE[ExpFE+row_eqn] & GF.BIT[col_eqn]) > 0) {
		       for (int ind_seg=0; ind_seg < NSegs; ind_seg++) {
		    	  Bufp[k+ind_seg] ^= Buf[l+ind_seg];
		       }
		    }
		  }
		}
		System.arraycopy(Bufp,0, Buf, 0, NInt);
		return this;
	}

//	--------------------------------------------------------------------	
	public synchronized NCFastdatagram sum(NCFastdatagram g,CoefEltPool cf) {

		int i;
		Coef_Elt coef, coefp;		
		
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
			if (NInt>=g.getNInt()){
				for(i = 0; i < g.getNInt() ; i++) {			
					Buf[i]=Buf[i]^g.Buf[i];				
				}	
			} else {
				for(i = 0; i < NInt ; i++) {			
					Buf[i]=Buf[i]^ g.Buf[i];				
				}
				System.arraycopy(g.Buf,NInt,Buf,NInt,g.getNInt()-NInt);
			}
			dataLength=Math.max(dataLength,g.dataLength);
			NInt=Math.max(NInt,g.getNInt());
			NSegs=Math.max(NSegs,g.getNSegs());
		}
		return this;
	}

//--------------------------------------------------------------------	

	public NCFastdatagram minus(NCFastdatagram g, CoefEltPool cf) throws Exception {
		int i;
		Coef_Elt coef, coefp;		
		
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
			if (NInt>=g.getNInt()){
				for(i = 0; i < g.getNInt() ; i++) {			
					Buf[i]=Buf[i]^g.Buf[i];				
				}	
			} else {
				for(i = 0; i < NInt ; i++) {			
					Buf[i]=Buf[i]^ g.Buf[i];				
				}
				System.arraycopy(g.Buf,NInt,Buf,NInt,g.getNInt()-NInt);
			}
			dataLength=Math.max(dataLength,g.dataLength);
			NInt=Math.max(NInt,g.getNInt());
			NSegs=Math.max(NSegs,g.getNSegs());
		}
		return this;
	}

	public int[] toIntBuf(byte[] buf,int pos,int length) {
		int i;
		int index= pos;
		
		dataLength=length;
  	  	NSegs=(int)Math.ceil(dataLength/(4.0*GF.getLfield()));
		NInt=NSegs*GF.getLfield();

  	  	int res=dataLength %4;	
  	  	int nint=(int)Math.floor(dataLength/4.0);
  	  	if (res==0) {
  	  		res=4;
  	  	}
 		for (int j=0; j < nint;j++) {
	  		i = ((buf[index++] & 0xFF) << 24) +
	  			((buf[index++] & 0xFF) << 16) +
	  			((buf[index++] & 0xFF) <<  8) +
	  			 (buf[index++] & 0xFF);
  			Buf[j]=i;
  		}
  		i=0;
  		for (int j=0; j <res; j++) {
  			i=i+((buf[index++] & 0xFF) << (24-8*j));
  		}
  		Buf[nint]=i; 		
  		res=dataLength %(4*GF.getLfield());
  		if (res!=0) {
  			res=GF.getLfield()-res;
  			for (int j=nint+1; j< NInt; j++) {
  				Buf[j]=0;
  			}
  		}
  		return this.Buf;
	}
	
	public int fromIntBuf(byte[] buf,int pos,int length) {

		int index=pos;
		int res=dataLength %4;
		int i;
		
  	  	if (res==0) {
  	  		res=4;
  	  	}
  		for (int j=0; j < NInt-1;j++) {
  			i=Buf[j];
  			buf[index++]= (byte)(i >> 24);
  			buf[index++] = (byte)(i >> 16);
  			buf[index++] = (byte)(i >>  8);
  			buf[index++] = (byte)i;
  		}
  		i=Buf[NInt];
  		for (int j=0; j < res; j++) {
  			buf[index++]= (byte)(i >> (24-8*j));
  		}
  		return dataLength;
	}

}

