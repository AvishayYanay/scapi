#include "stdafx.h"
#include "RabinPermutation.h"
#include "rabin.h"
#include "cryptlib.h"
#include "Utils.h"
#include "osrng.h"
#include <iostream>

using namespace std;
using namespace CryptoPP;

/*
 * function initRabinAll    : This function initialize the Rabin object with public key and private key
 * param tpPtr				: The pointer to the trapdoor permutation object 
 * param mod    			: modolus (n) 
 * param r					: quadratic residue mod prime 1
 * param s					: quadratic residue mod prime 2
 * param p					: prime 1
 * param q					: prime 2
 * param u					: inverse of p mod (q)
 * return jlong				: pointer to the native object
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_initRabinAll
  (JNIEnv *env, jobject, jbyteArray mod, jbyteArray r, jbyteArray s, jbyteArray p , jbyteArray q, jbyteArray u) {
	  Utils utils;
	  Integer modN, m_r, m_s, m_p, m_q, m_u;

	  /* get the Integers values for the Rabin permutation */
	  modN=utils.jbyteArrayToCryptoPPInteger(env, mod);
	  m_r=utils.jbyteArrayToCryptoPPInteger(env, r);
	  m_s=utils.jbyteArrayToCryptoPPInteger(env, s);
	  m_p=utils.jbyteArrayToCryptoPPInteger(env, p);
	  m_q=utils.jbyteArrayToCryptoPPInteger(env, q);
	  m_u=utils.jbyteArrayToCryptoPPInteger(env, u);

	  //create pointer to InvertibleRabinFunction object
	  TrapdoorFunction *tpPtr = new InvertibleRabinFunction;

	  //initialize the Rabin object with the parameters
	  ((InvertibleRabinFunction *) tpPtr) -> Initialize(modN, m_r, m_s, m_p, m_q, m_u);

	  return (jlong) tpPtr; // return the pointer

}

/*
 * function initRabinNRS    : This function initialize the Rabin object with public key 
 * param tpPtr				: The pointer to the trapdoor permutation object 
 * param mod    			: modolus (n) 
 * param r					: quadratic residue mod prime 1
 * param s					: quadratic residue mod prime 2
 * return jlong				: pointer to the native object
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_initRabinNRS
  (JNIEnv *env, jobject, jbyteArray n, jbyteArray r, jbyteArray s) {
	  Utils utils;
	  Integer m_n, m_r, m_s;

	   /* get the Integers values for the Rabin permutation */
	  m_n=utils.jbyteArrayToCryptoPPInteger(env, n);
	  m_r=utils.jbyteArrayToCryptoPPInteger(env, r);
	  m_s=utils.jbyteArrayToCryptoPPInteger(env, s);
	 
	  //create pointer to RabinFunction object
	  TrapdoorFunction* tpPtr =  new RabinFunction;  //assign RSAFunction to the pointer

	  //initialize the Rabin object with the parameters
	  ((RabinFunction *) tpPtr) -> Initialize(m_n, m_r, m_s);

	  return (jlong) tpPtr; // return the pointer
}

/*
 * function initRabinKeySize    : This function initialize the Rabin object with random values
 * param tpPtr					: The pointer to the trapdoor permutation object 
 * param numBits				: Number of bits
  * return jlong				: pointer to the native object
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_initRabinKeySize
  (JNIEnv * env, jobject, jint numBits) {
	  //Random Number Generator
	  AutoSeededRandomPool rng;
	  
	  //create pointer to InvertibleRabinFunction object
	  TrapdoorFunction *tpPtr = new InvertibleRabinFunction;

	  //initialize the trapdoor object with the random values
	  ((InvertibleRabinFunction *) tpPtr) -> Initialize(rng, numBits);

	  return (jlong) tpPtr; // return the pointer
}

/*
 * function loadRabinName : This function return the name of the Rabin trapdoor permutation
 * param ptr	          : The pointer to the Rabin object 
 * return			      : The name of the trapdoor permutation.
 */
JNIEXPORT jstring JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_loadRabinName
  (JNIEnv *env, jobject, jlong tpPtr) {
	  //get the Rabin algorithm name
	  string ls =((Rabin *) tpPtr) -> StaticAlgorithmName();

	  //return the name 
	  return env->NewStringUTF(ls.c_str());
}

/*
 * function getRabinModulus		: This function return the modulus of the current Rabin permutation
 * param tpPtr					: The pointer to the trapdoor permutation object 
 * return jbyteArray			: The modulus as byte array
 */
JNIEXPORT jbyteArray JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_getRabinModulus
  (JNIEnv *env, jobject, jlong tpPtr) {
	  Utils utils;
	  Integer mod;

	  //get ghe mod from the tp
	  mod = ((InvertibleRabinFunction *) tpPtr) -> GetModulus(); 

	  //convert the mod to jbyteArray and return it
	  return utils.CryptoPPIntegerTojbyteArray(env, mod);
}

/*
 * function checkRabinValidity	  : This function check if the element is valid for this Rabin permutation 
 *								    (if the number if between 1 to mod(N))
 * param tpPtr					  : The pointer to the trapdoor permutation object 
 * param value					  : The element to check
 * return boolean				  : True if valid, false if not
 */
JNIEXPORT jboolean JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_checkRabinValidity
  (JNIEnv *env, jobject, jlong pValue, jlong tpPtr) {
	  Utils utils;
	  Integer value, mod, p, q, square;

	  //get the Integer value of the element
	  value = *((Integer*) pValue);

	  //get mod(N), p, q
	  mod = ((RabinFunction *) tpPtr) -> GetModulus();
	  p = ((InvertibleRabinFunction *) tpPtr) -> GetPrime1();
	  q = ((InvertibleRabinFunction *) tpPtr) -> GetPrime2();

	  bool valid = false;

	  //check if the element is in the right range
	  if ((value < mod) && (value > 0)) {
		  //get a square root
		  square = utils.SquareRoot(value, mod, p, q);

		  //check if the return value of SquareRoot is indeed the square root of the element
		  if (((square*square).Modulo(mod)) == value)
			  valid = true;
	  }

	  return valid;
}

/*
 * function computeRabin	: This function compute the Rabin function on the accepted element
 * param tpPtr				: The pointer to the Rabin object 
 * param element			: The element for the computation
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_computeRabin
  (JNIEnv *env, jobject, jlong tpPtr, jlong element) {
	  
	  Utils utils;

	  //get the Integer value for the computation
	  Integer x = *(Integer*) element;

	  //operate the compute
	  Integer result = ((RabinFunction *) tpPtr)-> ApplyFunction(*(Integer*) element);

	  //return the result as jbyteArray
	  return (jlong) utils.getPointerToInteger(result);
}

/*
 * function invertRabin		: This function invert the Rabin permutation
 * param tpPtr				: The pointer to the Rabin object 
 * param element			: The element to invert
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpRabinPermutation_invertRabin
  (JNIEnv *env, jobject, jlong tpPtr, jlong element) {
	  // Random Number Generator
	  AutoSeededRandomPool rng;
	  Utils utils;

	  //get the Integer value to invert
	  Integer x = *(Integer*) element;

	  //operate the invert
	  Integer result = ((InvertibleRabinFunction *) tpPtr) -> CalculateInverse(rng, *(Integer*) element);

	  //return the result as jbyteArray
	  return (jlong) utils.getPointerToInteger(result);
}

/*
 * Delete the native object
 */
JNIEXPORT void JNICALL Java_edu_biu_scapi_primitives_trapdoorPermutation_cryptopp_CryptoPpTrapdoorPermutation_deleteRabin
	(JNIEnv *, jobject, jlong tpPtr) {
		delete((RabinFunction*) tpPtr);
}
