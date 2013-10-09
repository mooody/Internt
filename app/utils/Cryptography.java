package utils;
import java.io.IOException;  
import java.security.InvalidKeyException;  
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;  
import javax.crypto.Cipher;  
import javax.crypto.IllegalBlockSizeException;  
import javax.crypto.SecretKey;  
import javax.crypto.SecretKeyFactory;  
import javax.crypto.spec.DESKeySpec;  
import java.security.MessageDigest;
import models.User;
import java.util.Date;
import sun.misc.BASE64Decoder;  
import sun.misc.BASE64Encoder;  
import java.math.BigInteger;
  
/** 
 * @author dharmvir.singh  
 * The class demonstrates the DES algorithm by using java 
 * crypto API 
 */  
public class Cryptography {  
 private static final String CRYPTOGRAPHY_ALGO_DES = "DES";  
 private static Cipher cipher = null;  
 private static DESKeySpec keySpec = null;  
 private static SecretKeyFactory keyFactory = null;  
   
	public static String encrypt(String inputString, String commonKey)  throws InvalidKeyException, IllegalBlockSizeException,  BadPaddingException 
	{
		String encryptedValue = null;  
		SecretKey key = getSecretKey(commonKey);  
		cipher.init(Cipher.ENCRYPT_MODE, key);  
		
		byte[] inputBytes = inputString.getBytes();  
		byte[] outputBytes = cipher.doFinal(inputBytes);  
		
		encryptedValue = new BASE64Encoder().encode(outputBytes);  
		
		return encryptedValue;  
	}  

	public static String decrypt(String encryptedString, String commonKey) throws InvalidKeyException, IllegalBlockSizeException,  BadPaddingException, IOException 
	{  
		String decryptedValue = "";  
		// When Base64Encoded strings are passed in URLs, '+' character gets converted to space and so we need to reconvert the space to '+' and since encoded string cannot have space in it so we are completely safe.  
		encryptedString = encryptedString.replace(' ', '+');  
		SecretKey key = getSecretKey(commonKey);  
		cipher.init(Cipher.DECRYPT_MODE, key);  
		byte[] recoveredBytes = cipher.doFinal(new BASE64Decoder().decodeBuffer(encryptedString));  
		decryptedValue = new String(recoveredBytes);  
		return decryptedValue;  
	}  
	private static SecretKey getSecretKey(String secretPassword) 
	{  
		SecretKey key = null;  
		try 
		{  
			cipher = Cipher.getInstance(CRYPTOGRAPHY_ALGO_DES);  
			keySpec = new DESKeySpec(secretPassword.getBytes("UTF8"));  
			keyFactory = SecretKeyFactory.getInstance(CRYPTOGRAPHY_ALGO_DES);  
			key = keyFactory.generateSecret(keySpec);  
		} 
		catch (Exception e) 
		{  
			e.printStackTrace();  
			System.out.println("Error in generating the secret Key");  
		}  
		return key;  
	}  
	
	public static String getPasswordToken() throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		return getUniqueToken();
	}
	/**
	* Genererar en unik nyckel baserad p� tiden samt ett slumpm�ssigt tal
	*
	*/
	public static String getUniqueToken() throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		int rand = new java.util.Random().nextInt();
		String token = (new Date()).toString()+new Integer(rand).toString();
		byte[] bytesOfMessage = token.getBytes("UTF-8");

		MessageDigest md = MessageDigest.getInstance("MD5");
		BigInteger hash = new BigInteger(1, md.digest(bytesOfMessage));
		String hashword = hash.toString(16);

		return hashword;
	}
}  