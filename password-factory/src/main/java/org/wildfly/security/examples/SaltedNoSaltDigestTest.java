package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.SaltedSimpleDigestPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.spec.SaltedHashPasswordSpec;

public class SaltedNoSaltDigestTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(SaltedSimpleDigestPassword.ALGORITHM_PASSWORD_SALT_DIGEST_SHA_512, ELYTRON_PROVIDER);

        ClearPasswordSpec clearSpec = new ClearPasswordSpec(TEST_PASSWORD.toCharArray());
        SaltedSimpleDigestPassword original = (SaltedSimpleDigestPassword) passwordFactory.generatePassword(clearSpec);

        byte[] salt = original.getSalt();
        byte[] digest = original.getDigest();

        SaltedHashPasswordSpec saltedHashSpec = new SaltedHashPasswordSpec(digest, salt);

        SaltedSimpleDigestPassword restored = (SaltedSimpleDigestPassword) passwordFactory.generatePassword(saltedHashSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
