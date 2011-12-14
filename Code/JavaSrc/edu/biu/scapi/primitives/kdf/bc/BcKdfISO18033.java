package edu.biu.scapi.primitives.kdf.bc;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.generators.BaseKDFBytesGenerator;
import org.bouncycastle.crypto.generators.KDF1BytesGenerator;
import org.bouncycastle.crypto.params.ISO18033KDFParameters;
import org.bouncycastle.crypto.params.KDFParameters;

import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.UnInitializedException;
import edu.biu.scapi.primitives.hash.CryptographicHash;
import edu.biu.scapi.primitives.kdf.KeyDerivationFunction;
import edu.biu.scapi.tools.Factories.BCFactory;

/**
 * concrete class of KDF for ISO18033. This class wraps the implementation of bouncy castle.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Meital Levy)
 *
 */
public class BcKdfISO18033 implements KeyDerivationFunction {

	BaseKDFBytesGenerator bcKdfGenerator; // the adaptee kdf of BC
	
	/**
	 * creates the related bc kdf. Retrieve the related digest out of the given hash name.
	 * @throws FactoriesException in case of error while creating the object
	 */
	public BcKdfISO18033(String hash) throws FactoriesException {
		
		//creates a digest through the factory and passes it to the KDF
		bcKdfGenerator = new KDF1BytesGenerator(BCFactory.getInstance().getDigest(hash));
		
	}
	
	/**
	 * creates the related bc kdf, with the given hash
	 * @param hash - the underlying collision resistant hash
	 * @throws FactoriesException in case of error while creating the object
	 */
	public BcKdfISO18033(CryptographicHash hash) throws FactoriesException {
		//first checks that the hmac is initialized.
		if(hash.isInitialized()){
			//creates a digest of the given hash type through the factory and passes it to the KDF
			bcKdfGenerator = new KDF1BytesGenerator(BCFactory.getInstance().getDigest(hash.getAlgorithmName()));
		}
		else{//the user must pass an initialized object, otherwise throws an exception
			throw new IllegalStateException("argumrnt hmac must be initialized");
		}
	}
	
	/**
	 * BcKdfISO18033 is initialized by the constructor. The underlying type is a digest that doesn't need a key.
	 * Therefore this function souldn't be called. Throws exception.
	 */
	public void init(SecretKey secretKey) {
		throw new UnsupportedOperationException("BcKdfISO18033 doesn't need initialization");	
	}

	/**
	 * BcKdfISO18033 is initialized by the constructor. The underlying type is a digest that doesn't need a key.
	 * Therefore this function souldn't be called. Throws exception.
	 */
	public void init(SecretKey secretKey, AlgorithmParameterSpec params) {
		throw new UnsupportedOperationException("BcKdfISO18033 doesn't need initialization");	
	}

	/**
	 * BcKdfISO18033 is initialized by the constructor. Therefore, always return true.
	 */
	public boolean isInitialized() {
		return true;
	}
	
	public SecretKey generateKey(SecretKey seedForGeneration, int len) throws UnInitializedException {
		//calls the generateKey with iv=null
		return generateKey(seedForGeneration, len, null);
	}

	public SecretKey generateKey(SecretKey seedForGeneration, int outLen, byte[] iv) throws UnInitializedException {
		if(!isInitialized()){
			throw new UnInitializedException();
		}
		byte[] generatedKey = new byte[outLen];//generated key bytes
		
		//generates the related derivation parameter for bc with the seed and iv
		bcKdfGenerator.init(generateParameters(seedForGeneration.getEncoded(), iv));
		
		//generates the actual key bytes
		bcKdfGenerator.generateBytes(generatedKey, 0, outLen);
		
		//converts to secret key
		return new SecretKeySpec(generatedKey, "KDF");
	}


	public void generateKey(byte[] seedForGeneration, int inOff, int inLen, byte[] outKey,
			 int outOff,int outLen) throws UnInitializedException {
		//calls the generateKey with iv=null
		generateKey(seedForGeneration, inOff, inLen, outKey, outOff, outLen, null);
		
	}
	
	public void generateKey(byte[] seedForGeneration, int inOff, int inLen, byte[] outKey,
			 int outOff,int outLen, byte[] iv) throws UnInitializedException {
		if(!isInitialized()){
			throw new UnInitializedException();
		}
		//checks that the offset and length are correct
		if ((inOff > seedForGeneration.length) || (inOff+inLen > seedForGeneration.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given input buffer");
		}
		if ((outOff > outKey.length) || (outOff+outLen > outKey.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given output buffer");
		}
		
		//generates the related derivation parameter for bc with the seed and iv
		bcKdfGenerator.init(generateParameters(seedForGeneration,iv));
		
		//generates the actual key bytes and puts it in the output array
		bcKdfGenerator.generateBytes(outKey, outOff, outLen);
		
	}
	
	/**
	 * Generates the bc related parameters of type DerivationParameters
	 * @param shared the input key 
	 * @param iv
	 */
	private DerivationParameters generateParameters(byte[] shared, byte[] iv){
		
		if(iv==null){//iv is not provided
			
			return new ISO18033KDFParameters(shared);
		}
		else{ //iv is provided. Passes to the KDFParameters
			return new KDFParameters(shared, iv);
		}
		
	}


	
}