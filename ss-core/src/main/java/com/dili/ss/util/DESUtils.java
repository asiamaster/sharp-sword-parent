package com.dili.ss.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DES对称算法组件
 *
 * @author asiamaster
 */
public class DESUtils {
    //DES密钥算法
    public static final String KEY_ALGORITHM = "DES";

    /**
     * DES加密
     * @param srcStr
     * @param charset
     * @param sKey 8的倍数
     * @return
     */
    public static String encrypt(String srcStr, Charset charset, String sKey) {
        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        byte[] src = srcStr.getBytes(charset);
        byte[] buf = encrypt(src, sKey);
        return parseByte2HexStr(buf);
    }

    /**
     * DES解密
     *
     * @param hexStr
     * @param charset
     * @param sKey 8的倍数
     * @return
     * @throws Exception
     */
    public static String decrypt(String hexStr, Charset charset, String sKey) throws Exception {
        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        byte[] src = parseHexStr2Byte(hexStr);
        byte[] buf = decrypt(src, sKey);
        return new String(buf, charset);
    }

    /**
     * DES加密
     * @param data byte[]
     * @param password String 8的倍数
     * @return byte[]
     */
    private static byte[] encrypt(byte[] data, String password) {
        try{
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
//创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            SecretKey securekey = keyFactory.generateSecret(desKey);
//Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
//用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
//现在，获取数据并加密
//正式执行加密操作
            return cipher.doFinal(data);
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DES解密
     * @param data byte[]
     * @param password String 8的倍数
     * @return byte[]
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, String password) throws Exception {
// DES算法要求有一个可信任的随机数源
        SecureRandom random = new SecureRandom();
// 创建一个DESKeySpec对象
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
// 创建一个密匙工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
// 将DESKeySpec对象转换成SecretKey对象
        SecretKey securekey = keyFactory.generateSecret(desKey);
// Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
// 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
// 真正开始解密操作
        return cipher.doFinal(data);
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    private final static String charset = "UTF-8";
    private final static String algorithm = "DES";
    /**
     * 解密
     *
     * @param encryptStr 密文
     * @param key        密钥
     * @return 明文
     */
    public static String decrypt(String encryptStr, String key) {
        String strDecrypt = null;
        try {
            strDecrypt = new String(decryptByte(Base64.getDecoder().decode(encryptStr), key), charset);
        } catch (Exception e) {
            throw new RuntimeException("decrypt exception", e);
        }
        return strDecrypt;
    }

    /**
     * 字节解密
     *
     * @param encryptByte 密文
     * @param key         密钥
     * @return 明文
     */
    public static byte[] decryptByte(byte[] encryptByte, String key) {
        Cipher cipher;
        byte[] decryptByte = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
            decryptByte = cipher.doFinal(encryptByte);
        } catch (Exception e) {
            throw new RuntimeException("decryptByte exception", e);
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
        try {
            DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
            return keyFactory.generateSecret(desKeySpec);
        } catch (Exception e) {
            throw new RuntimeException("generateKey exception", e);
        }
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
            throw new RuntimeException("字节加密异常",e);
        } finally {
            cipher = null;
        }
        return byteFina;
    }


    /**
     * 加密
     * @param srcStr	明文
     * @param key		密钥
     * @return			密文
     */
    public static String encrypt(String srcStr, String key) {
        String strEncrypt = null;
        try {
            strEncrypt = Base64.getEncoder().encodeToString(encryptByte(srcStr.getBytes(charset), key));
        } catch (Exception e) {
            throw new RuntimeException("加密异常", e);
        }
        return strEncrypt;
    }
}
