package ch.epfl.lsr.adhoc.routing.ncode.feedback;

import java.util.Enumeration;

import util.bloom.BloomFilter;
import ch.epfl.lsr.adhoc.routing.ncode.CoefEltPool;
import ch.epfl.lsr.adhoc.routing.ncode.Coef_Elt;
import ch.epfl.lsr.adhoc.routing.ncode.GaloisException;
import ch.epfl.lsr.adhoc.routing.ncode.NCdatagram;

public class NCFBdatagram extends NCdatagram{
	//public long gen_;
	public FbData fbData; 
	private long NodeId;   
	// Methods
	// Constructors
	public NCFBdatagram(byte[] buf, int length, int Index, FbData fbData) {
		super(buf,length, Index);
		this.fbData=fbData;
	}

	public NCFBdatagram() {
		super(0);
		this.NodeId=NCFBGlobals.nodeID_;
		fbData=new FbData(0, 0, 0, 0, null, null);
	}


	public NCFBdatagram(int Index, Integer numEq, Integer numVar, Integer numDecoded,Integer capacity, long nodeID, BloomFilter bfDecoded, BloomFilter bfVariable) {
		super(Index);
		this.NodeId=nodeID;
		fbData=new FbData(numEq, numVar, numDecoded,capacity, bfDecoded, bfVariable);
	}

	//	public NCdatagram clone() {
	public Object clone() {		
		Coef_Elt coef,coefp;

		NCFBdatagram g = new NCFBdatagram(Buf,dataLength, Index_, fbData);

		Enumeration<Coef_Elt> ec = coefs_list.elements();
		while(ec.hasMoreElements())
		{
			coef = (Coef_Elt) ec.nextElement();
			coefp =(Coef_Elt) coef.clone();
			g.coefs_list.put(coefp.key(),coefp);				
		}
		return g;
	}

	public boolean isNull() {
		if ((coefs_list.size()==0) && (fbData.numDecoded==0) && (fbData.numVar==0)) {
			return true;
		} else {
			return false;
		}	
	}




	public synchronized int serialize(byte[] data) {
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
		// Adding fbdata
		index=index+fbData.putFbData(data, index);              
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
		// get fbData
		if (fbData.bfdecoded!= null) {
			fbData.bfdecoded.clear();
		}
		if (fbData.bfvariable!= null) {
			fbData.bfvariable.clear();
		}
		index=index+fbData.getFbData(data, index, NodeId);;              
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

		return index;
	}

	public void setNodeID(long srcNode) {
		NodeId=srcNode;
	}
	
	public synchronized NCFBdatagram product(int coef) throws GaloisException {

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

//	----
	public synchronized NCFBdatagram sum(NCFBdatagram g,CoefEltPool cf) throws GaloisException {

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

}
