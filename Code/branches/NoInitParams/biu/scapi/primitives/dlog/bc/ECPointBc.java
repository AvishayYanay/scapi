package edu.biu.scapi.primitives.dlog.bc;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

import edu.biu.scapi.primitives.dlog.ECElement;
import edu.biu.scapi.primitives.dlog.groupParams.ECGroupParams;

/**
 * This class is an adapter for BC point.
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public abstract class ECPointBc implements ECElement{

	protected ECPoint point = null;
	
	
	ECPoint getPoint(){
		return point;
	}

	public BigInteger getX(){
		//in case of infinity, there is no coordinates and returns null
		if (isInfinity()){
			return null;
		}
		
		return point.getX().toBigInteger();
	}
	
	public BigInteger getY(){
		//in case of infinity, there is no coordinates and returns null
		if (isInfinity()){
			return null;
		}
		
		return point.getY().toBigInteger();
	}
	
	abstract boolean checkValidity(BigInteger x, BigInteger y,
			ECGroupParams groupParams);
	
	public boolean isInfinity(){
		return point.isInfinity();
	}
}
