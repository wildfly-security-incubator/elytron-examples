package org.wildfly.security.examples;

import java.security.Provider;
import java.security.SecureRandom;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.ScramDigestPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedHashPasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;

public class ScramTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();

    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ScramDigestPassword.ALGORITHM_SCRAM_SHA_512, ELYTRON_PROVIDER);

        byte[] salt = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(2000, salt);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(TEST_PASSWORD.toCharArray(), iteratedAlgorithmSpec);

        ScramDigestPassword original = (ScramDigestPassword) passwordFactory.generatePassword(encryptableSpec);

        byte[] digest = original.getDigest();

        IteratedSaltedHashPasswordSpec scramPasswordSpec = new IteratedSaltedHashPasswordSpec(digest, salt, 2000);

        ScramDigestPassword restored = (ScramDigestPassword) passwordFactory.generatePassword(scramPasswordSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
