package edu.biu.scapi.primitives.dlog.miracl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import org.bouncycastle.util.encoders.Hex;

import edu.biu.scapi.primitives.dlog.DlogECFp;
import edu.biu.scapi.primitives.dlog.ECElement;
import edu.biu.scapi.primitives.dlog.GroupElement;
import edu.biu.scapi.primitives.dlog.groupParams.ECFpGroupParams;
import edu.biu.scapi.securityLevel.DDH;

/**This class implements an Elliptic curve Dlog group over Zp utilizing Miracl's implementation.<p>
 * It uses JNI technology to call Miracl's native code.
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 */
public class MiraclDlogECFp extends MiraclAdapterDlogEC implements DlogECFp, DDH{

	private native void initFpCurve(long mip, byte[] p, byte[] a,byte[] b);
	private native long multiplyFpPoints(long mip, long p1, long p2);
	private native long simultaneousMultiplyFp(long mip, long[] points, byte[][] exponents);
	private native long exponentiateFpPoint(long mip, long p, byte[] exponent);
	private native long invertFpPoint(long mip, long p);
	private native boolean validateFpGenerator(long mip, long generator, byte[] x, byte[] y);
	private native boolean isFpMember(long mip, long point);
	private native long createInfinityFpPoint(long mip);
	
	/**
	 * Default constructor. Initializes this object with P-192 NIST curve.
	 */
	public MiraclDlogECFp() throws IOException{
		this("P-192");
	}
	
	public MiraclDlogECFp(String fileName, String curveName) throws IOException{
		super(fileName, curveName);
	}
	
	public MiraclDlogECFp(String curveName) throws IllegalArgumentException, IOException{
		
		
		Properties ecProperties;
	
		ecProperties = getProperties(PROPERTIES_FILES_PATH); //get properties object containing the curve data
	
		//checks that the curveName is in the file
		if(!ecProperties.containsKey(curveName)) { 
			throw new IllegalArgumentException("no such NIST elliptic curve"); 
		} 
		
		//check that the given curve is in the field that matches the group
		if (!curveName.startsWith("P-")){
			throw new IllegalArgumentException("curveName is not a curve over Fp field and doesn't match the DlogGroup type"); 
		} 
		doInit(ecProperties, curveName);  // set the data and initialize the curve
		
			
	}

	/**
	 * Extracts the parameters of the curve from the properties object and initialize the groupParams, 
	 * generator and the underlying curve
	 * @param ecProperties - properties object contains the curve file data
	 * @param curveName - the curve name as it called in the file
	 */
	protected void doInit(Properties ecProperties, String curveName){
		//get the nist parameters
		BigInteger p = new BigInteger(ecProperties.getProperty(curveName));
		BigInteger a = new BigInteger(ecProperties.getProperty(curveName+"a"));
		BigInteger b = new BigInteger(1,Hex.decode(ecProperties.getProperty(curveName+"b")));
		BigInteger x = new BigInteger(1,Hex.decode(ecProperties.getProperty(curveName+"x")));
		BigInteger y = new BigInteger(1,Hex.decode(ecProperties.getProperty(curveName+"y")));
		BigInteger q = new BigInteger(ecProperties.getProperty(curveName+"r"));
		
		//create the GroupParams
		groupParams = new ECFpGroupParams(q, x, y, p, a, b);
		
		//create the curve
		initFpCurve(getMip(), p.toByteArray(), a.mod(p).toByteArray(), b.toByteArray());
		
		//create the generator
		generator = new ECFpPointMiracl(x,y, this);	
	}
	
	/**
	 * @return the type of the group - ECFp
	 */
	public String getGroupType(){
		return "elliptic curve over Fp";
	}
	
	/**
	 * Calculate the inverse of the given GroupElement
	 * @param groupElement to inverse
	 * @return the inverse element of the given GroupElement
	 * @throws IllegalArgumentException
	 */
	public GroupElement getInverse(GroupElement groupElement) throws IllegalArgumentException{
		
		//if the GroupElement doesn't match the DlogGroup, throw exception
		if (!(groupElement instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
		}
		
		//the inverse of infinity point is infinity
		if (((ECFpPointMiracl)groupElement).isInfinity()){
			return groupElement;
		}
		
		long point = ((ECFpPointMiracl)groupElement).getPoint();
		//call to native inverse function
		long result = invertFpPoint(mip, point);
		//build a ECFpPointMiracl element from the result value
		return new ECFpPointMiracl(result, mip);	
		
	}
	
	/**
	 * Multiply two GroupElements
	 * @param groupElement1
	 * @param groupElement2
	 * @return the multiplication result
	 * @throws IllegalArgumentException
	 */
	public GroupElement multiplyGroupElements(GroupElement groupElement1, 
						GroupElement groupElement2) throws IllegalArgumentException{
		
		//if the GroupElements don't match the DlogGroup, throw exception
		if (!(groupElement1 instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
		}
		if (!(groupElement2 instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
		}
		
		//if one of the points is the infinity point, the second one is the multiplication result
		if (((ECFpPointMiracl)groupElement1).isInfinity()){
			return groupElement2;
		}
		if (((ECFpPointMiracl)groupElement2).isInfinity()){
			return groupElement1;
		}
		
		long point1 = ((ECFpPointMiracl)groupElement1).getPoint();
		long point2 = ((ECFpPointMiracl)groupElement2).getPoint();
		
		//call to native multiply function
		long result = multiplyFpPoints(mip, point1, point2);
		//build a ECFpPointMiracl element from the result value
		return new ECFpPointMiracl(result, mip);
		
	}
	
	/**
	 * Calculate the exponentiate of the given GroupElement
	 * @param exponent
	 * @param base 
	 * @return the result of the exponentiation
	 * @throws IllegalArgumentException
	 */
	public GroupElement exponentiate(GroupElement base, BigInteger exponent) 
									 throws IllegalArgumentException{
		
		//if the GroupElements don't match the DlogGroup, throw exception
		if (!(base instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
		}
		
		//infinity remains the same after any exponentiate
		if (((ECFpPointMiracl) base).isInfinity()){
			return base;
		}
		
		long point = ((ECFpPointMiracl)base).getPoint();
		//call to native exponentiate function
		long result = exponentiateFpPoint(mip, point, exponent.toByteArray());
		//build a ECFpPointMiracl element from the result value
		return new ECFpPointMiracl(result, mip);
		
	}
	
	/**
	 * Computes the product of several exponentiations with distinct bases 
	 * and distinct exponents. 
	 * Instead of computing each part separately, an optimization is used to 
	 * compute it simultaneously. 
	 * @param groupElements
	 * @param exponentiations
	 * @return the exponentiation result
	 */
	@Override
	public GroupElement simultaneousMultipleExponentiations(GroupElement[] groupElements, 
			BigInteger[] exponentiations) {
		
		int len = groupElements.length;
		
		//Our test results show that for elliptic curve over Fp and n<25 the naive algorithm gives the best performances
		if (len < 25){
				return computeNaive(groupElements, exponentiations);
		} 				
		
		long[] nativePoints = new long[len];
		byte[][] exponents = new byte[len][];
		for(int i=0; i<len; i++){
			//if the GroupElements don't match the DlogGroup, throw exception
			if (!(groupElements[i] instanceof ECFpPointMiracl)){
				throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
			}
			nativePoints[i] = ((ECFpPointMiracl)groupElements[i]).getPoint();
			exponents[i] = exponentiations[i].toByteArray();
		}
		
		//call to native exponentiate function
		long result = simultaneousMultiplyFp(mip, nativePoints, exponents);
		//build a ECF2mPointMiracl element from the result value
		return new ECFpPointMiracl(result, mip);
	}
	
	/**
	 * Create a random member of that Dlog group
	 * @return the random element 
	 */
	public GroupElement getRandomElement(){
		
		return new ECFpPointMiracl(this);
	}
	
	/**
	 * Create a point in the Fp field with the given parameters
	 * @return the created point
	 */
	public ECElement getElement(BigInteger x, BigInteger y) {
		
		return new ECFpPointMiracl(x, y, this);
	}
	
	/**
	 * Check if the given element is member of that Dlog group
	 * @param element - 
	 * @return true if the given element is member of that group. false, otherwise.
	 * @throws IllegalArgumentException
	 */
	public boolean isMember(GroupElement element){
		
		boolean member = false;
		//checks that the element is the correct object
		if(!(element instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("groupElement doesn't match the DlogGroup");
		}
		
		//infinity point is a valid member
		if (((ECFpPointMiracl) element).isInfinity()){
			return true;
		}
		
		//call for a native function that checks if the element is a point in this curve
		member = isFpMember(mip, ((ECFpPointMiracl) element).getPoint());	
		
		return member;
	}
	
	public ECElement getInfinity(){
		long infinity = createInfinityFpPoint(mip);
		return new ECFpPointMiracl(infinity, mip);
	}
	
	//upload MIRACL library
	static {
        System.loadLibrary("MiraclJavaInterface");
	}

	/**
	 * Converts a byte array to a ECFpPointMiracl.
	 * @param binaryString the byte array to convert
	 * @return the created group Element
	 */
	public GroupElement convertByteArrayToGroupElement(byte[] binaryString){
		if (binaryString.length >= ((ECFpGroupParams) groupParams).getP().bitLength()){
			throw new IllegalArgumentException("String is too long. It has to be of length less than log p");
		}
		BigInteger  x = new BigInteger(binaryString);
		GroupElement point = null;
		try {
			point = new ECFpPointMiracl(x, this);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The given string is not a valid point to this curve");
		} 
		return point;
	}
	
	/**
	 * Convert a ECFpPointMiracl to a byte array.
	 * @param groupElement the element to convert
	 * @return the created byte array
	 */
	public byte[] convertGroupElementToByteArray(GroupElement groupElement){
		if (!(groupElement instanceof ECFpPointMiracl)){
			throw new IllegalArgumentException("element type doesn't match the group type");
		}
		return ((ECElement) groupElement).getX().toByteArray();
	}
	
	
	//upload MIRACL library
	static {
        System.loadLibrary("MiraclJavaInterface");
	}
}
