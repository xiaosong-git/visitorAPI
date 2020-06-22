package com.goldccm.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class HashHmac {

    public static String hash_hmac(String value,String key){
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Base64.encodeBase64String(sha256_HMAC.doFinal(value.getBytes()));
        }
        catch (Exception e){
           e.printStackTrace();
        }

        return null;
    }
}
