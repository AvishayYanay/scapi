/**
 * Suppose we wish to wrap a higher-level cryptographic element that uses a primitive interface in its inner implementation. 
 * For example, HMAC of BC holds a Digest interface but requires a specific digest to invoke its main functions. 
 * However, the users of our tool are not familiar with the classes of BC. Yet if we wish to wrap this class, 
 * we will need to instantiate a concrete Digest (specified by the user) to the wrapped BcHMAC. 
 * The aim of the BCFactory is to translate from an algorithm name string to an external library instance. 
 * For example, in BcHMAC the user passes a hash name that eventually will be translated to a digest. 
 * If the user passed the "SHA1" string, the translation tool generates the related SHA1 digest.
 */
package edu.biu.scapi.tools.Factories;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.StreamCipher;


/** 
 * @author LabTest
 */
public final class BCFactory {
	private FactoriesUtility factoriesUtility;
	private static BCFactory instance = new BCFactory();//singleton

	
	/**
	 * Private constructor since this class is of the singleton pattern. 
     * It creates an instance of FactoriesUtility and passes a predefined file name to the constructor
     * of FactoriesUtility.
	 * 
	 */
	private BCFactory() {

		//create an instance of FactoriesUtility with the predefined file name.  
		factoriesUtility = new FactoriesUtility(null, "BC.properties");
		
	}
	
	/** 
	 * Returns the equivalent BC block cipher according to the specified name.
	 * @param name the name of the pseudo random permutation equivalent to the BC block cipher
	 * @return BC <code>BlockCipher<code> object
	 */
	public BlockCipher getBlockCipher(String name) {
		
		return (BlockCipher) factoriesUtility.getObject("BC", name);
	}

	/** 
	 * Returns the equivalent BC asymmetric block cipher according to the specified name.
	 * @param name the name of the trapdoor permutation equivalent to the BC asymmetric block cipher
	 * @return BC <code>AsymmetricBlockCipher<code> object
	 */
	public AsymmetricBlockCipher getAsymetricBlockCipher(String name) {
		
		return (AsymmetricBlockCipher) factoriesUtility.getObject("BC", name);
	}

	/** 
	 * Returns the equivalent BC digest according to the specified name.
	 * @param name the name of the collision resistant hash equivalent to the BC digest cipher
	 * @return BC <code>Digest<code> object
	 */
	public Digest getDigest(String name) {
		
		return (Digest) factoriesUtility.getObject("BC", name);
	}

	/** 
	 * Returns the equivalent BC stream cipher according to the specified name. 
	 * @param name the name of the pseudo random generator equivalent to the BC stream cipher
	 * @return BC <code>StreamCipher<code> object
	 */
	public StreamCipher getStreamCipher(String name) {
		
		return (StreamCipher) factoriesUtility.getObject("BC", name);
	}

	/** 
	 * @return the singleton instance
	 */
	public static BCFactory getInstance() {
		
		return instance;
		
	}
}