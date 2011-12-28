package edu.biu.scapi.primitives.dlog.bc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Level;

import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECFieldElement.F2m;
import org.bouncycastle.math.ec.ECPoint;

import edu.biu.scapi.exceptions.UnInitializedException;
import edu.biu.scapi.generals.Logging;
import edu.biu.scapi.primitives.dlog.groupParams.ECF2mGroupParams;
import edu.biu.scapi.primitives.dlog.groupParams.ECF2mKoblitz;
import edu.biu.scapi.primitives.dlog.groupParams.ECF2mPentanomialBasis;
import edu.biu.scapi.primitives.dlog.groupParams.ECF2mTrinomialBasis;
import edu.biu.scapi.primitives.dlog.groupParams.ECGroupParams;
import edu.biu.scapi.primitives.dlog.groupParams.GroupParams;

/**
 * This class is an adapter for ECPoint.F2m of BC
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class ECF2mPointBc extends ECPointBc{
	
	/**
	 * Constructor that accepts x,y values of a point. 
	 * if the values are valid - set the point.
	 * @param x
	 * @param y
	 * @param curve - DlogGroup
	 * @throws UnInitializedException 
	 */
	public ECF2mPointBc(BigInteger x, BigInteger y, BcDlogECF2m curve) throws UnInitializedException{
		
		//checks validity
		if (!checkValidity(x, y,(ECF2mGroupParams) curve.getGroupParams()))
			throw new IllegalArgumentException("x, y values are not a point on this curve");
		/*
		 * create the point with the given parameters.
		 */
		point = ((BcAdapterDlogEC)curve).createPoint(x, y);	
	}
	
	/**
	 *  Constructor that gets DlogGroup and chooses a random point in the group
	 * @param curve
	 * @throws UnInitializedException 
	 */
	public ECF2mPointBc(BcDlogECF2m curve) throws UnInitializedException{
		
		try {
			ECF2mGroupParams desc = (ECF2mGroupParams)curve.getGroupParams();
			
			/*
			 * choosing random point on the curve
			 */
			int m = desc.getM(); //get the field size
			/* get curve parameters */
			int[] k = new int[3];
			
			if (desc instanceof ECF2mKoblitz)
				getBasis(((ECF2mKoblitz) desc).getCurve(), k);
			else
				getBasis(desc, k);
			
			SecureRandom random = new SecureRandom();
			BigInteger x = null;
			
			/*find a point in the group */
			for(int i=0; i<2*m; i++){
				x = new BigInteger(m, random); //get a random element
				
				/*calculate y value corresponding to x value */
				ECFieldElement.F2m xElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], x);
				ECFieldElement.F2m aElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], desc.getA());
				ECFieldElement.F2m bElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], desc.getB());
				//compute x^3
				ECFieldElement.F2m x3 = (F2m) xElement.square().multiply(xElement);
				//compute ax^2
				ECFieldElement.F2m ax2 = (F2m) aElement.multiply(xElement.square());
				//compute f(x) = x^3+ax^2+b
				ECFieldElement.F2m fx = (F2m) x3.add(ax2).add(bElement);
				//compute 4(x^3+ax^2+b)
				ECFieldElement.F2m fx4 = (F2m) fx.multiply(new ECFieldElement.F2m(m, k[0], k[1], k[2], new BigInteger("4")));
				//compute x^2-4f(x)
				ECFieldElement.F2m delta = (F2m) xElement.square().add(fx4.negate());
				ECFieldElement.F2m yVal = null;
				ECFieldElement.F2m two = new ECFieldElement.F2m(m, k[0], k[1], k[2], new BigInteger("2"));
				
				//if the delta is 0 - there is 1 solution to the equation
				if (delta.toBigInteger().compareTo(BigInteger.ZERO) == 0){  
					//compute y value = -x/2
					yVal = (F2m) xElement.negate().divide(two);
				} 
				//if the delta is greater than 0 - there are 2 solutions to the equation and we choose one of them to be the y value
				if (delta.toBigInteger().compareTo(BigInteger.ZERO) > 0){  
					Boolean coin = random.nextBoolean();
					if (coin==true){
						//compute y value = (-x+sqrt(f(x)))/2
						yVal = (F2m) xElement.negate().add(fx4.sqrt()).divide(two);
					} else yVal = (F2m) xElement.negate().add(fx4.sqrt().negate()).divide(two);
				} 
				
				if (yVal!=null){ // if there is a square root, create a point
					BigInteger y = yVal.toBigInteger();
					//create the point
					point = ((BcAdapterDlogEC)curve).createPoint(x, y);
					i=2*m; //stop the loop
				}
			}
			//if the algorithm failed, write it to the log
			if (point == null)
				Logging.getLogger().log(Level.WARNING, "couldn't find a random element");
		}catch (RuntimeException e){
			if (e.getMessage().equals("Not implemented")){
				throw new RuntimeException("Create random ECF2mPointBC will be available as soon as BC implements the sqrt function in ECFieldElement.F2m");
			}
		}
	}

	/**
	 * Constructor that accepts x value of a point, calculates its corresponding y value and create a point with these values. 
	 * @param x the x coordinate of the point
	 * @param curve - elliptic curve dlog group over F2m
	 * @throws UnInitializedException if the given curve is not initialized
	 */
	ECF2mPointBc(BigInteger x, BcDlogECF2m curve) throws UnInitializedException{
		try{
			ECF2mGroupParams params = (ECF2mGroupParams)curve.getGroupParams();
			
			int m = params.getM(); //get the field size
			/* get curve parameters */
			int[] k = new int[3];
			
			if (params instanceof ECF2mKoblitz)
				getBasis(((ECF2mKoblitz) params).getCurve(), k);
			else
				getBasis(params, k);
			
			SecureRandom random = new SecureRandom();
			/*calculates y value corresponding to x value */
			ECFieldElement.F2m xElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], x);
			ECFieldElement.F2m aElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], params.getA());
			ECFieldElement.F2m bElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], params.getB());
			//computes x^3
			ECFieldElement.F2m x3 = (F2m) xElement.square().multiply(xElement);
			//computes ax^2
			ECFieldElement.F2m ax2 = (F2m) aElement.multiply(xElement.square());
			//computes f(x) = x^3+ax^2+b
			ECFieldElement.F2m fx = (F2m) x3.add(ax2).add(bElement);
			//computes 4(x^3+ax^2+b)
			ECFieldElement.F2m fx4 = (F2m) fx.multiply(new ECFieldElement.F2m(m, k[0], k[1], k[2], new BigInteger("4")));
			//computes x^2-4f(x)
			ECFieldElement.F2m delta = (F2m) xElement.square().add(fx4.negate());
			ECFieldElement.F2m yVal = null;
			ECFieldElement.F2m two = new ECFieldElement.F2m(m, k[0], k[1], k[2], new BigInteger("2"));
			
			//if the delta is 0 - there is 1 solution to the equation
			if (delta.toBigInteger().compareTo(BigInteger.ZERO) == 0){  
				//compute y value = -x/2
				yVal = (F2m) xElement.negate().divide(two);
			} 
			//if the delta is greater than 0 - there are 2 solutions to the equation and we choose one of them to be the y value
			if (delta.toBigInteger().compareTo(BigInteger.ZERO) > 0){  
				Boolean coin = random.nextBoolean();
				if (coin==true){
					//compute y value = (-x+sqrt(f(x)))/2
					yVal = (F2m) xElement.negate().add(fx4.sqrt()).divide(two);
				} else yVal = (F2m) xElement.negate().add(fx4.sqrt().negate()).divide(two);
			} 
			
			if (yVal!=null){ // if there is a square root, create a point
				BigInteger y = yVal.toBigInteger();
				//create the point
				point = ((BcAdapterDlogEC)curve).createPoint(x, y);
			} else {
				throw new IllegalArgumentException("the given x has no corresponding y in the current curve");
			}
		}catch (RuntimeException e){
			if (e.getMessage().equals("Not implemented")){
				throw new RuntimeException("Create an ECF2mPointBC element will be available as soon as BC implements the sqrt function in ECFieldElement.F2m");
			}
		}
	}
	
	/*
	 * Constructor that gets an element and sets it.
	 * Only our inner functions use this constructor to set an element. 
	 * The ECPoint is a result of our DlogGroup functions, such as multiply.
	 * @param point
	 */
	ECF2mPointBc(ECPoint point){
		this.point = point;
	}
	
	/*
	 * Checks if the x,y values constitute a valid point in the given DlogGroup.
	 */
	boolean checkValidity(BigInteger x, BigInteger y, ECGroupParams params) {
		//the GroupParams that matches this class is ECF2mGroupParams
		if (params instanceof ECF2mGroupParams){
			int m = ((ECF2mGroupParams) params).getM(); //get the field size
			/* get curve parameters */
			int[] k = new int[3];
			
			if (params instanceof ECF2mKoblitz){
				getBasis(((ECF2mKoblitz) params).getCurve(), k);
			}else
				getBasis(params, k);
			
			/* construct ECFieldElements from a,b,x,y */
			ECFieldElement.F2m xElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], x);
			ECFieldElement.F2m yElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], y);
			ECFieldElement.F2m aElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], params.getA());
			ECFieldElement.F2m bElement = new ECFieldElement.F2m(m, k[0], k[1], k[2], params.getB());
			
			/*
			 * Calculates the curve equation with the given x,y.
			 */
			//compute x^3
			
			ECFieldElement.F2m x_2 = (F2m) xElement.square();
			ECFieldElement.F2m x_3 = (F2m) x_2.multiply(xElement);
			//compute ax^2
			ECFieldElement.F2m ax_2 = (F2m) aElement.multiply(x_2);
			//compute x^3+ax^2+b
			ECFieldElement.F2m addition = (F2m) x_3.add(ax_2);
			ECFieldElement.F2m result = (F2m) addition.add(bElement);
			
			//compute xy
			ECFieldElement.F2m xy = (F2m) yElement.multiply(xElement);
			//compute y^2+xy
			ECFieldElement.F2m y_2 = (F2m) yElement.square();
			ECFieldElement.F2m result2 = (F2m) y_2.add(xy);
			
			//if the the equation is solved - the point is in the elliptic curve and return true
			if (result2.equals(result))
				return true;
			else return false;
			//if the GroupParams is not ECF2mGroupParams throw exception
		} else throw new IllegalArgumentException("groupParams doesn't match the GroupElement");
		
	}
	
	private void getBasis(GroupParams params, int[] k){
		
		if (params instanceof ECF2mTrinomialBasis){
			k[0] = ((ECF2mTrinomialBasis)params).getK1();
		}
		if (params instanceof ECF2mPentanomialBasis){
			k[0] = ((ECF2mPentanomialBasis)params).getK1();
			k[1] = ((ECF2mPentanomialBasis)params).getK2();
			k[2] = ((ECF2mPentanomialBasis)params).getK3();
		}
	}
	
	public boolean equals(Object elementToCompare){
		if (!(elementToCompare instanceof ECF2mPointBc)){
			throw new IllegalArgumentException("element type doesn't match the group type");
		}
		ECF2mPointBc element = (ECF2mPointBc) elementToCompare;
		if ((element.getX().compareTo(getX()) ==0) && (element.getY().compareTo(getY()) == 0)){
			return true;
		}
		return false;
	}
}
