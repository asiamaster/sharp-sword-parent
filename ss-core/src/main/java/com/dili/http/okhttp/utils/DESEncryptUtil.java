package com.dili.http.okhttp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

/**
 * DES对称加密算法
 *
 *  加密:encrypt(String srcStr, String key)
 *  解密:descrypt(String encryptStr,String key )
 *  加密解密过程中采用的key是一样的
 *
 */
public class DESEncryptUtil {
	protected static final Logger log = LoggerFactory.getLogger(DESEncryptUtil.class);
	private final static String charset = "UTF-8";
	private final static String algorithm = "DES";

	/**
	 * 加密
	 * @param srcStr	明文
	 * @param key		密钥
	 * @return			密文
	 */
	public static String encrypt(String srcStr, String key) {
		String strEncrypt = null;
		BASE64Encoder base64en = new BASE64Encoder();
		try {
			strEncrypt = base64en.encode(encryptByte(srcStr.getBytes(charset), key));
		} catch (Exception e) {
			log.error("加密异常:", e);
			throw new RuntimeException("加密异常", e);
		}
		return strEncrypt;
	}

	/**
	 * 安静加密
	 * @param srcStr	明文
	 * @param key		密钥
	 * @return			密文
	 */
	public static String encryptQuietly(String srcStr, String key) {
		String strEncrypt = null;
		BASE64Encoder base64en = new BASE64Encoder();
		try {
			strEncrypt = base64en.encode(encryptByteQuietly(srcStr.getBytes(charset), key));
		} catch (Exception e) {
			return null;
		}
		return strEncrypt;
	}

	/**
	 * 解密
	 * @param encryptStr	密文
	 * @param key			密钥
	 * @return				明文
	 */
	public static String decrypt(String encryptStr, String key) {
		BASE64Decoder base64De = new BASE64Decoder();
		String strDecrypt = null;
		try {
			strDecrypt = new String(decryptByte(base64De.decodeBuffer(encryptStr), key), charset);
		} catch (Exception e) {
			log.error("解密异常:", e);
			throw new RuntimeException("解密异常",e);
		} finally {
			base64De = null;
		}
		return strDecrypt;
	}

	/**
	 * 安静解密
	 * @param encryptStr	密文
	 * @param key			密钥
	 * @return				明文
	 */
	public static String decryptQuietly(String encryptStr, String key) {
		BASE64Decoder base64De = new BASE64Decoder();
		String strDecrypt = null;
		try {
			strDecrypt = new String(decryptByteQuietly(base64De.decodeBuffer(encryptStr), key), charset);
		} catch (Exception e) {
			return null;
		} finally {
			base64De = null;
		}
		return strDecrypt;
	}

	/**
	 * 字节加密
	 * @param srcByte	明文
	 * @param key		密钥
	 * @return			密文
	 */
	public static byte[] encryptByte(byte[] srcByte, String key) {
		byte[] byteFina = null;
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
			byteFina = cipher.doFinal(srcByte);
		} catch (Exception e) {
			log.error("字节加密异常:", e);
			throw new RuntimeException("字节加密异常",e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * 安静字节加密
	 * @param srcByte	明文
	 * @param key		密钥
	 * @return			密文
	 */
	public static byte[] encryptByteQuietly(byte[] srcByte, String key) {
		byte[] byteFina = null;
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
			byteFina = cipher.doFinal(srcByte);
		} catch (Exception e) {
			throw new RuntimeException("字节加密异常",e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * 字节解密
	 * @param encryptByte	密文
	 * @param key			密钥
	 * @return				明文
	 */
	public static byte[] decryptByte(byte[] encryptByte, String key) {
		Cipher cipher;
		byte[] decryptByte = null;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
			decryptByte = cipher.doFinal(encryptByte);
		} catch (Exception e) {
			log.error("字节解密异常:", e);
			throw new RuntimeException("字节解密异常",e);
		} finally {
			cipher = null;
		}
		return decryptByte;
	}

	/**
	 * 安静字节解密
	 * @param encryptByte	密文
	 * @param key			密钥
	 * @return				明文
	 */
	public static byte[] decryptByteQuietly(byte[] encryptByte, String key) {
		Cipher cipher;
		byte[] decryptByte = null;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
			decryptByte = cipher.doFinal(encryptByte);
		} catch (Exception e) {
			throw new RuntimeException("字节解密异常",e);
		} finally {
			cipher = null;
		}
		return decryptByte;
	}

	/**
	 * 根据传入的字符串的key生成key对象
	 *
	 * @param strKey
	 */
	public static Key generateKey(String strKey) {
		try{
			DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes(charset));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
			return keyFactory.generateSecret(desKeySpec);
		}catch(Exception e){
			throw new RuntimeException("生成密钥异常",e);
		}

	}
	/**
	 * 测试函数
	 * @param args
	 */
	public static void main1(String[] args) {

		String key = "i am key,let me encrypt you! 1234haha";//1co35SwcG+o=
		//String key1 = "i am key,let me encrypt you! 1234haha";
		String src = "t3ijqxtd";

		System.out.println("密钥:"+key);
		System.out.println("明文:"+src );

		String strEnc = DESEncryptUtil.encrypt(src, key);
		System.out.println("加密后,密文:"+strEnc);

		String strDes = DESEncryptUtil.decrypt(strEnc, key);
		System.out.println("解密后,明文:"+ strDes);

	}

}