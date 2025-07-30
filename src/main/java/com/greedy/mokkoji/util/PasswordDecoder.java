package com.greedy.mokkoji.util;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordDecoder {

    @Value("${aes.secret-key}")
    private String secretKey;

    @Value("${aes.IV}")
    private String initVector;

    public String decode(String rawPassword) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(rawPassword));
            return new String(original);
        } catch (Exception e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_PASSWORD);
        }
    }
}
