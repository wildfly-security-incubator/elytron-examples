package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.DigestPassword;
import org.wildfly.security.password.spec.DigestPasswordAlgorithmSpec;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;

public class RawDigestTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_USERNAME = "test_username";
    static final String TEST_REALM = "Test Realm";
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(DigestPassword.ALGORITHM_DIGEST_MD5, ELYTRON_PROVIDER);

        DigestPasswordAlgorithmSpec digestAlgorithmSpec = new DigestPasswordAlgorithmSpec(TEST_USERNAME, TEST_REALM);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(TEST_PASSWORD.toCharArray(), digestAlgorithmSpec);

        DigestPassword original = (DigestPassword) passwordFactory.generatePassword(encryptableSpec);

        byte[] digest = original.getDigest();

        DigestPassword rawPassword = DigestPassword.createRaw(DigestPassword.ALGORITHM_DIGEST_MD5, TEST_USERNAME, TEST_REALM, digest);

        DigestPassword restored = (DigestPassword) passwordFactory.translate(rawPassword);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
