package edu.biu.scapi.primitives.trapdoorPermutation.cryptopp;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import edu.biu.scapi.exceptions.UnInitializedException;
import edu.biu.scapi.primitives.trapdoorPermutation.RabinKeyGenParameterSpec;
import edu.biu.scapi.primitives.trapdoorPermutation.RabinPermutation;
import edu.biu.scapi.primitives.trapdoorPermutation.RabinPrivateKey;
import edu.biu.scapi.primitives.trapdoorPermutation.RabinPublicKey;
import edu.biu.scapi.primitives.trapdoorPermutation.ScRabinPublicKey;
import edu.biu.scapi.primitives.trapdoorPermutation.TPElValidity;
import edu.biu.scapi.primitives.trapdoorPermutation.TPElement;
import edu.biu.scapi.primitives.trapdoorPermutation.TrapdoorPermutationAbs;

/** 
 * Concrete class of trapdoor permutation for Rabin.
 * This class wraps the crypto++ implementation of Rabin permutation.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 */
public final class CryptoPpRabinPermutation extends TrapdoorPermutationAbs implements RabinPermutation {
	
	private long tpPtr; //pointer to the Rabin native object 
	
	// native functions. These functions are implemented in the CryptoPPJavaInterface dll using the JNI.
	
	//initializes Rabin permutation with public and private keys
	private native long initRabinPublicPrivate(byte[] mod, byte[] r, byte[] s, byte[] p, byte[] q, byte[] u);
	//initializes Rabin permutation with public key
	private native long initRabinPublic(byte[] mod, byte[] r, byte[] s);
	////initializes Rabin permutation randomly
	private native long initRabinRandomly(int numBits);
	
	//returns the algorithm name - rabin
	private native String loadRabinName(long ptr);
	//returns the modulus
	private native byte[] getRabinModulus(long ptr);
	//returns the QuadraticResidueModPrime1 (r)
	private native byte[]getQuadraticResidueModPrime1(long ptr);
	//returns the QuadraticResidueModPrime1 (s)
	private native byte[]getQuadraticResidueModPrime2(long ptr);
	//checks if the given element value is valid for this Rabin permutation
	private native boolean checkRabinValidity(long value, long tpPtr);
	
	//computes Rabin permutation
	private native long computeRabin(long tpr, long x);
	//inverts Rabin permutation
	private native long invertRabin(long ptr, long y);

	//deletes the native object
	private native void deleteRabin(long ptr);
	
	

	/** 
	 * No such implementation for Rabin permutation. throws UnsupportedOperationException.
	 * This Rabin implementation can be initialized by two ways:
	 * 1. keys
	 * 2. algorithmParameterSpec
	 * any combination of these ways is not a legal initialization.
	 * @throws UnsupportedOperationException 
	 */
	public void init(PublicKey publicKey, PrivateKey privateKey,
			AlgorithmParameterSpec params) throws UnsupportedOperationException {
		/*initialization of Rabin can be done by two ways:
		 * 1. keys
		 * 2. algorithmParameterSpec
		 * any combination of these ways is not a legal initialization.
		 */
		throw new UnsupportedOperationException("no such Rabin initialization");

	}
	
	/** 
	 * Initializes this Rabin permutation with public and private keys
	 * @param publicKey - public key
	 * @param privateKey - private key
	 * @throws InvalidKeyException if the given keys are invalid for Rabin permutation
	 */
	public void init(PublicKey publicKey, PrivateKey privateKey) throws InvalidKeyException {
			
		if (!(publicKey instanceof RabinPublicKey) || !(privateKey instanceof RabinPrivateKey)) {
			throw new InvalidKeyException("Key type doesn't match the trapdoor permutation type");
		}
		
		RabinPublicKey pub = (RabinPublicKey)publicKey;
		RabinPrivateKey priv = (RabinPrivateKey)privateKey;
		
		//gets all the parameters
		BigInteger r, s, p, q, u;
		modN = pub.getModulus();
		r = pub.getQuadraticResidueModPrime1();
		s = pub.getQuadraticResidueModPrime2();
		p = priv.getPrime1();
		q = priv.getPrime2();
		u = priv.getInversePModQ();
		
	
		//init the rabin native object
		tpPtr = initRabinPublicPrivate(modN.toByteArray(), r.toByteArray(), s.toByteArray(), 
					 p.toByteArray(), q.toByteArray(), u.toByteArray());
		
		//calls the parent init that sets the keys
		super.init(publicKey, privateKey);
			
	}

	/** 
	 * Initializes this Rabin permutation with public key.
	 * After this initialization, this object can do compute but not invert.
	 * This initialization is for user that wants to encrypt a message using the public key but deosn't want to decrypt a message.
	 * @param publicKey - public key
	 * @throws InvalidKeyException if the given key is invalid for this Rabin permutation
	 */
	public void init(PublicKey publicKey) throws InvalidKeyException {
			
		if (!(publicKey instanceof RabinPublicKey)) {
			throw new InvalidKeyException("Key type doesn't match the trapdoor permutation type");
		}
		
		RabinPublicKey pub = (RabinPublicKey)publicKey;
		//get the n, r, s parameters
		BigInteger r,s;
		modN = pub.getModulus();
		r = pub.getQuadraticResidueModPrime1();
		s = pub.getQuadraticResidueModPrime2();
		
		//init the rabin native object
		tpPtr = initRabinPublic(modN.toByteArray(), r.toByteArray(), s.toByteArray());
		
		//calls the parent init that sets the key
		super.init(publicKey);
		
	}
	
	/** 
	 * Initializes this Rabin permutation with random parameters.
	 * @param params auxiliary parameters
	 * @throws InvalidParameterSpecException if params are invalid parameter spec
	 */
	public void init(AlgorithmParameterSpec params) throws InvalidParameterSpecException {

		if (!(params instanceof RabinKeyGenParameterSpec)) {
			throw new InvalidParameterSpecException("AlgorithmParameterSpec type doesn't match the trapdoor permutation type");
		}
		
		//gets the modulus bits size
		int numBits = ((RabinKeyGenParameterSpec) params).getKeySize();

		//init the Rabin native object
		tpPtr = initRabinRandomly(numBits);
		
		//sets the modN
		modN = new BigInteger(getRabinModulus(tpPtr));
		BigInteger r = new BigInteger(getQuadraticResidueModPrime1(tpPtr));
		BigInteger s = new BigInteger(getQuadraticResidueModPrime2(tpPtr));
		
		pubKey = new ScRabinPublicKey(modN, r, s);
		
		//calls the parent init
		super.init(params);

	}
	
	/** 
	 * @return the algorithm name - Rabin
	 */
	public String getAlgorithmName() {
		
		return loadRabinName(tpPtr);
	}
	
	/** 
	 * Computes the Rabin permutation on the given TPElement 
	 * @param tpEl - the input for the computation
	 * @return - the result element
	 * @throws UnInitializedException if this object is not initialized 
	 * @throws - IllegalArgumentException if the given element is not CryptoPpRabinElement
	 */
	public TPElement compute(TPElement tpEl) throws IllegalArgumentException, UnInitializedException{
		
		if (!IsInitialized()){
			throw new UnInitializedException();
		}
		if (!(tpEl instanceof CryptoPpRabinElement)){
			throw new IllegalArgumentException("trapdoor element type doesn't match the trapdoor permutation type");
		}
		
		// gets the pointer for the native object
		long elementP = ((CryptoPpRabinElement)tpEl).getPointerToElement(); 
		
		//calls the native function
		long result = computeRabin(tpPtr, elementP); 
		
		//creates and initializes RabinElement with the result
		CryptoPpRabinElement returnEl = new CryptoPpRabinElement(result);
		
		return returnEl; // returns the result TPElement
	}
	
	/** 
	 * Inverts the Rabin permutation on the given element 
	 * @param tpEl - the input to invert
	 * @return - the result element
	 * @throws UnInitializedException if this object is not initialized 
	 * @throws - IllegalArgumentException if the given element is not CryptoPpRabinElement
	 */
	public TPElement invert(TPElement tpEl) throws IllegalArgumentException, UnInitializedException{
		if (!IsInitialized()){
			throw new UnInitializedException();
		}
		//in case that the initialization was with public key and no private key - can't do the invert and returns null
		if (privKey == null && pubKey!=null)
			return null;
		
		if (!(tpEl instanceof CryptoPpRabinElement)){
			throw new IllegalArgumentException("trapdoor element type doesn't match the trapdoor permutation type");
		}
		
		// get the pointer for the native object
		long elementP = ((CryptoPpRabinElement)tpEl).getPointerToElement(); 
		
		//calls the native function
		long result = invertRabin(tpPtr, elementP); 
		
		//creates and initializes RabinElement with the result
		CryptoPpRabinElement returnEl = new CryptoPpRabinElement(result);
		
		return returnEl; // returns the result TPElement
	}

	
	/** 
	 * Checks if the given element is valid to this Rabin permutation
	 * @param tpEl - the element to check
	 * @return TPElValidity - enum number that indicate the validation of the element 
	 * There are three possible validity values: 
	 * VALID (it is an element)
	 * NOT_VALID (it is not an element)
	 * DON�T_KNOW (there is not enough information to check if it is an element or not)  
	 * @throws UnInitializedException if this object is not initialized
	 * @throws - IllegalArgumentException if the given element is not CryptoPpRabinElement
	 */
	public TPElValidity isElement(TPElement tpEl) throws IllegalArgumentException, UnInitializedException{
		
		if (!IsInitialized()){
			throw new UnInitializedException();
		}
		if (!(tpEl instanceof CryptoPpRabinElement)){
			throw new IllegalArgumentException("trapdoor element type doesn't match the trapdoor permutation type");
		}
			
		long value = ((CryptoPpRabinElement)tpEl).getPointerToElement();
		TPElValidity validity = null;
		
		//if the trapdoor permutation or p,q are unknown - returns DONT_KNOW 
		if ((modN == null) || ((privKey == null) && (pubKey != null))) {
			validity = TPElValidity.DONT_KNOW;
			
		//if the value is valid (between 1 to (mod n) - 1 and has a square root mod (N)) - returns VALID 
		} else if(checkRabinValidity(value, tpPtr)) {
			
			validity = TPElValidity.VALID;
		//if the value is invalid returns NOT_VALID 
		} else {
			validity = TPElValidity.NOT_VALID;
		}		
		
		//returns the correct TPElValidity
		return validity;
	}

	/** 
	 * creates a random CryptoPpRabinElement.
	 * @return TPElement - the created element
	 * @throws UnInitializedException if this object is not initialized
	 */
	public TPElement getRandomTPElement() throws UnInitializedException {
		if (!IsInitialized()){
			throw new UnInitializedException();
		}
		return new CryptoPpRabinElement(modN);
	}

	/**
	 * deletes the native Rabin object
	 */
	protected void finalize() throws Throwable {
		
		//deletes from the dll the dynamic allocation of the Rabin permutation.
		deleteRabin(tpPtr);
		
		super.finalize();
	}
	
	//loads the dll
	 static {
	        System.loadLibrary("CryptoPPJavaInterface");
	 }
	
}