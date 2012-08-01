package edu.biu.scapi.midLayer.asymmetricCrypto.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAKeyGenParameterSpec;

import edu.biu.scapi.midLayer.plaintext.ByteArrayPlaintext;
import edu.biu.scapi.midLayer.plaintext.Plaintext;

/**
 * Abstract class of RSA OAEP encryption scheme. This class has some common functionality of the encryption scheme, such as key generation.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public abstract class RSAOaepAbs implements RSAOaepEnc {

	protected SecureRandom random;		//Source of randomness.
	protected boolean isKeySet;
	protected RSAPublicKey publicKey;
	
	@Override
	public boolean isKeySet() {
		return isKeySet;
	}
	
	/**
	 * Returns the PublicKey of this RSA encryption scheme.
	 * This function should not be use to check if the key has been set. 
	 * To check if the key has been set use isKeySet function.
	 * @return the RSAPublicKey
	 * @throws IllegalStateException if no public key was set.
	 */
	public PublicKey getPublicKey(){
		if (!isKeySet()){
			throw new IllegalStateException("no PublicKey was set");
		}
		
		return publicKey;
	}

	/**
	 * @return the name of this Asymmetric encryption - "RSA/OAEP".
	 */
	@Override
	public String getAlgorithmName() {
		return "RSA/OAEP";
	}

	/**
	 * RSA OAEP has a limit of the byte array length to generate a plaintext from.
	 * @return true. 
	 */
	public boolean hasMaxByteArrayLengthForPlaintext(){
		return true;
	}
	
	/**
	 * Generates a Plaintext suitable to ElGamal encryption scheme from the given message.
	 * @param msg byte array to convert to a Plaintext object.
	 * @throws IllegalArgumentException if the given message's length is greater than the maximum. 
	 */
	public Plaintext generatePlaintext(byte[] text){
		if (text.length > getMaxLengthOfByteArrayForPlaintext()){
			throw new IllegalArgumentException("the given text is too big for plaintext");
		}
		
		return new ByteArrayPlaintext(text);
	}
	
	/**
	 * Generates a byte array from the given plaintext. 
	 * This function should be used when the user does not know the specific type of the Asymmetric encryption he has, 
	 * and therefore he is working on byte array.
	 * @param plaintext to generates byte array from. MUST be an instance of ByteArrayPlaintext.
	 * @return the byte array generated from the given plaintext.
	 * @throws IllegalArgumentException if the given plaintext is not an instance of ByteArrayPlaintext.
	 */
	public byte[] generateBytesFromPlaintext(Plaintext plaintext){
		if (!(plaintext instanceof ByteArrayPlaintext)){
			throw new IllegalArgumentException("plaintext should be an instance of ByteArrayPlaintext");
		}
		
		return ((ByteArrayPlaintext) plaintext).getText();
	}

	/**
	 * This function is not supported. 
	 */
	@Override
	public KeyPair generateKey() {
		throw new UnsupportedOperationException("To generate RSA keys call generateKey with RSAKeyGenParameterSpec");
	}

	
	/**
	 * Generate an RSA key pair using the given parameters.
	 * @param keyParams RSAKeyGenParameterSpec
	 * @return KeyPair contains keys for this RSAOaep object
	 * @throws InvalidParameterSpecException if keyParams is not instance of RSAKeyGenParameterSpec
	 */
	public KeyPair generateKey(AlgorithmParameterSpec keyParams) throws InvalidParameterSpecException {
		//If keyParams is not the expected, throw exception.
		if (!(keyParams instanceof RSAKeyGenParameterSpec)){
			throw new InvalidParameterSpecException("keyParams should be instance of RSAKeyGenParameterSpec");
		}
		
		try {
			//Generates keys using the KeyPairGenerator
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(keyParams, random);
			return generator.generateKeyPair(); 
		} catch(InvalidAlgorithmParameterException e){
			//Shouldn't occur since the parameterSpec is valid for RSA
		} catch (NoSuchAlgorithmException e) {
			//Shouldn't occur since RSA is a valid algorithm
			e.printStackTrace();
		}
		return null;
	}

}
