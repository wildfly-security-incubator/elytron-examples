package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.OneTimePassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.OneTimePasswordAlgorithmSpec;
import org.wildfly.security.password.spec.OneTimePasswordSpec;

public class OTPTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(OneTimePassword.ALGORITHM_OTP_SHA_512, ELYTRON_PROVIDER);

        String seed = "ke1234";
        int sequenceNumber = 500;

        OneTimePasswordAlgorithmSpec oneTimeAlgorithmSpec = new OneTimePasswordAlgorithmSpec(OneTimePassword.ALGORITHM_OTP_SHA_512, seed, sequenceNumber);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(TEST_PASSWORD.toCharArray(), oneTimeAlgorithmSpec);

        OneTimePassword original = (OneTimePassword) passwordFactory.generatePassword(encryptableSpec);

        byte[] hash = original.getHash();

        OneTimePasswordSpec oneTimeSpec = new OneTimePasswordSpec(hash, seed, sequenceNumber);

        OneTimePassword restored = (OneTimePassword) passwordFactory.generatePassword(oneTimeSpec);
    }

}
