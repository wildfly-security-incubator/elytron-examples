package org.wildfly.security.examples;

import java.security.Provider;
import java.security.SecureRandom;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.MaskedPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.MaskedPasswordAlgorithmSpec;
import org.wildfly.security.password.spec.MaskedPasswordSpec;

public class MaskedTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();

    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(MaskedPassword.ALGORITHM_MASKED_MD5_DES, ELYTRON_PROVIDER);

        char[] key = "my_secret_key".toCharArray();

        byte[] salt = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        int iterationCount = 100;

        MaskedPasswordAlgorithmSpec maskedAlgorithmSpec = new MaskedPasswordAlgorithmSpec(key, iterationCount, salt);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(TEST_PASSWORD.toCharArray(), maskedAlgorithmSpec);

        MaskedPassword original = (MaskedPassword) passwordFactory.generatePassword(encryptableSpec);

        byte[] masked = original.getMaskedPasswordBytes();

        MaskedPasswordSpec maskedPasswordSpec = new MaskedPasswordSpec(key, iterationCount, salt, masked);

        MaskedPassword restored = (MaskedPassword) passwordFactory.generatePassword(maskedPasswordSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
