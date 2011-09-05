package ch.epfl.lsr.adhoc.routing.ncode.feedback;

import java.io.*;
import java.util.*;

import util.bloom.BloomFilter;

import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

public class FbData {
	byte numEq, numVar;
	int numDecoded;
	BloomFilter bfdecoded, bfvariable;
	long time;
	byte capacity;
	long nodeID;
	boolean  active;

	public FbData(Integer numEq, Integer numVar, Integer numDecoded, Integer capacity, BloomFilter bfDecoded, BloomFilter bfVariable) {
		this.numEq=numEq.byteValue();
		this.numVar=numVar.byteValue();
		this.numDecoded=numDecoded.byteValue();
		this.capacity=capacity.byteValue();
		this.bfdecoded=bfDecoded;
		this.bfvariable=bfVariable;
	}
	
	public void setData(Integer numEq, Integer numVar, Integer numDecoded, Integer capacity, BloomFilter bfDecoded, BloomFilter bfVariable) { 
		this.numEq=numEq.byteValue();
		this.numVar=numVar.byteValue();
		this.numDecoded=numDecoded.byteValue();
		this.capacity=capacity.byteValue();
		this.bfdecoded=bfDecoded;
		this.bfvariable=bfVariable;		
	}

	public int putFbData( byte[] buf, int offset){
		//Adding all data in the Feedback		
		int oldoffset=offset;
		buf[offset++]=(byte)'b';
		buf[offset++]=numEq;
		buf[offset++]=(byte)'b';
		buf[offset++]=numVar;
		buf[offset++]=(byte)'b';
		buf[offset++]=capacity;
		int i=numDecoded;
		buf[offset++] = (byte)'i';
		buf[offset++] = (byte)(i >> 24);
		buf[offset++] = (byte)(i >> 16);
		buf[offset++] = (byte)(i >>  8);
		buf[offset++] = (byte)i;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dataBF = new DataOutputStream(out);
		try {
			bfdecoded.write(dataBF);
			bfvariable.write(dataBF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.arraycopy(out.toByteArray(), 0, buf, offset, out.size());
		offset=offset+out.size();
		return offset-oldoffset;
	}

	public int getFbData(byte buf[],int offset, Long nodeID) {
		active=true;
		int oldoffset=offset;
		this.nodeID=nodeID;
		//extracting all from the Feedback
		time=System.currentTimeMillis(); 
		try {
		if (buf[offset++]!='b') {
			throw new SendMessageFailedException("At FbData 'b'");
		}
		numEq=buf[offset++];
		if (buf[offset++]!='b') {
			throw new SendMessageFailedException("At FbData 'b'");
		}
		numVar=buf[offset++];
		if (buf[offset++]!='b') {
			throw new SendMessageFailedException("At FbData 'b'");
		}
		capacity=buf[offset++];
		if (buf[offset++]!='i') {
			throw new SendMessageFailedException("At FbData 'b'");
		}
		numDecoded = ((buf[offset++] & 0xFF) << 24) +
		((buf[offset++] & 0xFF) << 16) +
		((buf[offset++] & 0xFF) <<  8) +
		(buf[offset++] & 0xFF);
		
		ByteArrayInputStream in = new ByteArrayInputStream(buf, offset, buf.length);
		DataInputStream dataBF = new DataInputStream(in);
		if (bfdecoded==null){
			bfdecoded= new BloomFilter();
		}
		bfdecoded.clear();
		offset=offset+bfdecoded.read(dataBF);
		if (bfvariable==null){
			bfvariable= new BloomFilter();
		}
		bfvariable.clear();
		offset=offset+bfvariable.read(dataBF);
		} catch (SendMessageFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return offset-oldoffset;
	} 

	static int bits2Ints(BitSet bs, byte[] buf, int offset) {
		byte[] temp = new byte[bs.size() / 8];

		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < 8; j++)
				if (bs.get(i * 8 + j))
					buf[i+offset] |= 1 << j;
		offset=offset+temp.length;
		return offset;
	}

	public static BitSet byte2BitSet (byte[] buf, int offset, int bitLen)
	{
		BitSet bmap = new BitSet (bitLen);
		for (int i=0; i<bitLen; i++) 
			if (((buf[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
				bmap.set(i+1);
		return bmap;
	}
	
	public FbData clone() {
		FbData clone=new FbData(0, 0, 0,0, null, null);
		clone.numEq=numEq;
		clone.numVar=numVar;
		clone.numDecoded=numDecoded;
		clone.nodeID=nodeID;
		clone.capacity=capacity;
		if (bfvariable==null)
			clone.bfvariable=null;
		else
			clone.bfvariable= (BloomFilter)bfvariable.clone();
		if (bfdecoded==null)
			clone.bfdecoded=null;
		else
			clone.bfdecoded=(BloomFilter) bfdecoded.clone();
		clone.time=time;
		return clone;
	}
	
	public void disactivate() {
		active=false;
	}
}
