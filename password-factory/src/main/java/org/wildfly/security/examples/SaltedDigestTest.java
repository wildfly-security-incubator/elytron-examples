package org.wildfly.security.examples;

import java.security.Provider;
import java.security.SecureRandom;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.SaltedSimpleDigestPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.SaltedHashPasswordSpec;
import org.wildfly.security.password.spec.SaltedPasswordAlgorithmSpec;

public class SaltedDigestTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(SaltedSimpleDigestPassword.ALGORITHM_PASSWORD_SALT_DIGEST_SHA_512, ELYTRON_PROVIDER);

        byte[] salt = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        SaltedPasswordAlgorithmSpec saltedSpec = new SaltedPasswordAlgorithmSpec(salt);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(TEST_PASSWORD.toCharArray(), saltedSpec);

        SaltedSimpleDigestPassword original = (SaltedSimpleDigestPassword) passwordFactory.generatePassword(encryptableSpec);

        byte[] digest = original.getDigest();

        SaltedHashPasswordSpec saltedHashSpec = new SaltedHashPasswordSpec(digest, salt);

        SaltedSimpleDigestPassword restored = (SaltedSimpleDigestPassword) passwordFactory.generatePassword(saltedHashSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
