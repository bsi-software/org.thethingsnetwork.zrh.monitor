package org.thethingsnetwork.zrh.monitor.model;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// initial source: https://gist.github.com/bricef/2436364
public class Crypto {

	// https://github.com/TheThingsNetwork/croft/blob/master/server.go
	public static final byte [] SEMTECH_DEFAULT_KEY = new byte [] {
			0x2B, 0x7E, 0x15, 0x16, 0x28, (byte) 0xAE, (byte) 0xD2, (byte) 0xA6, 
			(byte) 0xAB, (byte) 0xF7, 0x15, (byte) 0x88, 0x09, (byte) 0xCF, 0x4F, 0x3C
	};

	public static final byte [] IV = new byte [] {
			0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
			0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
			0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
			0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0
	};

	static String IV2            = "AAAAAAAAAAAAAAAA";
	static String plaintext     = "test text 123\0\0\0"; /*Note null padding*/
	static String encryptionKey = "0123456789abcdef";

	public static void main(String [] args) {
		try {

			System.out.println("==Java==");
			System.out.println("plain:   " + plaintext);

			byte [] cipher = encrypt(plaintext, encryptionKey.getBytes("UTF-8"));

			System.out.print("cipher:  ");
			for (int i=0; i<cipher.length; i++)
				System.out.print(new Integer(cipher[i])+" ");
			
			System.out.println("");

			String decrypted = decrypt(cipher, encryptionKey.getBytes("UTF-8"));

			System.out.println("decrypt: " + decrypted);
			System.out.println("-------------------");
			String rawData = "QAIZAwIAAT0B7nvPRwgLIw==";
			String data = "fVn+";
			System.out.println("ttn rawData: " + rawData);
			System.out.println("ttn data: " + data);
			
			cipher = decodeFromBase64(rawData);
			decrypted = decrypt(cipher, SEMTECH_DEFAULT_KEY);
			
			System.out.println("ttn decrypted("+rawData+")=" + decrypted);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static byte[]  encrypt(String plainText, byte [] key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "SunJCE");
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(IV));
		return cipher.doFinal(plainText.getBytes("UTF-8"));
	}

	public static String decrypt(byte [] cipherBytes, byte [] key) throws Exception{
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "SunJCE");
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec,new IvParameterSpec(IV));
		return new String(cipher.doFinal(cipherBytes),"UTF-8");
	}

	public static String encodeToBase64(byte [] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte [] decodeFromBase64(String data) {
		return Base64.getDecoder().decode(data);
	}

	public static byte [] getNetworkKey(String gatewayEui, long deviceAddress) {
		return SEMTECH_DEFAULT_KEY;
	}

	public static byte [] getApplicationKey(String gatewayEui, long deviceAddress) {
		return SEMTECH_DEFAULT_KEY;
	}
}

