package edu.biu.scapi.primitives.trapdoorPermutation;

import java.math.BigInteger;
import java.util.Random;


/**
 * Concrete class of TPElement for RSA element. 
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public final class RSAElement implements TPElement{

	private BigInteger element; // the element value
	
	/**
	 * Constructor that chooses a random element according to the given modulus.
	 * @param modN the modulus
	 */
	public RSAElement(BigInteger modN) {
		/*
		 * samples a number between 1 to n-1
		 */
		
		Random generator = new Random();
		
		BigInteger randNumber = null;
		do {
			//samples a random BigInteger with modN.bitLength()+1 bits
			randNumber = new BigInteger(modN.bitLength()+1, generator);
			//drops the element if it bigger then mod(N)-2
		} while(randNumber.compareTo(modN.add(new BigInteger("-2")))>0);
		//gets a random BigInteger between 1 to modN-1
		randNumber = randNumber.add(new BigInteger("1"));
		
		//sets it to be the element
		element = randNumber;
	}
	
	/**
	 * Constructor that gets a modulus and a value. If the value is a valid RSA element according to the modulus, sets it to be the element.
	 * @param modN - the modulus
	 * @param x - the element value
	 * @throws IllegalArgumentException if the element is not legal according the modulus
	 */
	public RSAElement(BigInteger modN, BigInteger x) throws IllegalArgumentException{
		
		/*
		 * checks if the value is valid (between 1 to (mod n) - 1).
		 * if valid - sets it to be the element
		 * if not valid - throws exception 
		 */
		if(((x.compareTo(BigInteger.ZERO))>0) && (x.compareTo(modN)<0)) {
			element = x;
		} else {
			throw new IllegalArgumentException("element out of range");
		}
			
	}
	
	/**
	 * Returns the RSA element.
	 * @return the element
	 */
	public BigInteger getElement() {
		return element;
	}

	
}
