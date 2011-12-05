package edu.biu.scapi.primitives.prf;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import edu.biu.scapi.exceptions.UnInitializedException;

/** 
 * This class implements some common functionality of PrpVaryingIOLength by having an instance of prfVaryingIOLength.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Meital Levy)
 */
public abstract class PrpFromPrfVarying implements PrpVaryingIOLength {
	
	protected PrfVaryingIOLength prfVaryingIOLength; // the underlying prf
	
	/**
	 * Initializes this PrpFromPrfVarying with secret key
	 * @param secretKey the secret key
	 */
	public void init(SecretKey secretKey) {
		//initializes the underlying prf with the given secret key
		prfVaryingIOLength.init(secretKey);
		
	}

	/**
	 * Initializes this PrpFromPrfVarying with the secret key and the auxiliary parameters
	 * @param secretKey the secret key
	 * @param params the auxiliary parameters
	 * @throws InvalidParameterSpecException 
	 */
	
	public void init(SecretKey secretKey, AlgorithmParameterSpec params) throws InvalidParameterSpecException {
		//initializes the underlying prf with the given secret key and params
		prfVaryingIOLength.init(secretKey, params);	
	}
	
	public boolean isInitialized(){
		// call the underlying prf isInitialized function and return the result
		return prfVaryingIOLength.isInitialized();
	}
	
	public AlgorithmParameterSpec getParams() throws UnInitializedException {
		// return the params of the underlying prf
		return prfVaryingIOLength.getParams();
	}

	public SecretKey getSecretKey() throws UnInitializedException {
		// return the secret key of the underlying prf
		return prfVaryingIOLength.getSecretKey();
	}

	/** 
	 * This function is suitable for block ciphers where the input/output length is known in advance.
	 * In this case, both input and output variables are varying so this function should not be called. Throws an exception.
	 * 
	 * @throws IllegalBlockSizeException 
	 * @throws UnInitializedException 
	 */

	public void computeBlock(byte[] inBytes, int inOff, byte[] outBytes,
			int outOff) throws IllegalBlockSizeException, UnInitializedException {
		if(!isInitialized()){
			throw new UnInitializedException();
		}
		throw new IllegalBlockSizeException("to use this prp, call the computeBlock function which specify the block size length");
		
	}

	/** 
	 * Computes the function using the secret key. <p>
	 * 
	 * This function is provided in the interface especially for the sub-family PrfVaryingIOLength, which may have variable input and output length.
	 * Since this is a prp, both input and output variables should be equal and there is no need to send them both, so this function should not be called. 
	 * If the user still wants to use this function, the specified arguments <code>inLen<code> and <code>outLen<code> should be the same, otherwise, throws an exception.
	 * 
	 * @param inBytes input bytes to compute
	 * @param inOff input offset in the inBytes array
	 * @param inLen the length of the input array
	 * @param outBytes output bytes. The resulted bytes of compute
	 * @param outOff output offset in the outBytes array to put the result from
	 * @param outLen the length of the output array
	 * @throws IllegalBlockSizeException 
	 * @throws UnInitializedException 
	 */
	public void computeBlock(byte[] inBytes, int inOff, int inLen,
			byte[] outBytes, int outOff, int outLen)
			throws IllegalBlockSizeException, UnInitializedException {
		if(!isInitialized()){
			throw new UnInitializedException();
		}
		// checks that the offsets and lengths are correct 
		if ((inOff > inBytes.length) || (inOff+inLen > inBytes.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given input buffer");
		}
		if ((outOff > outBytes.length) || (outOff+outLen > outBytes.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given output buffer");
		}
		
		//if input and output lengths are equal, call the computeBlock which takes just one length argument
		if (inLen == outLen){
			computeBlock(inBytes, inOff, inLen, outBytes, outOff);
		}
		else throw new IllegalBlockSizeException("input and output lengths should be equal");
		
	}

	/**
	 * This function is suitable for block ciphers where the input/output length is known in advance.
	 * In this case, both input and output variables are varying so this function should not be called. Throws an exception.
	 * 
	 * @throws IllegalBlockSizeException 
	 * @throws UnInitializedException 
	 */
	public void invertBlock(byte[] inBytes, int inOff, byte[] outBytes,
			int outOff) throws IllegalBlockSizeException, UnInitializedException {
		if(!isInitialized()){
			throw new UnInitializedException();
		}
		throw new IllegalBlockSizeException("to use this prp, call the invertBlock function which specify the block size length");
		
	}
	
	
}