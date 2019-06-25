package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.ScramDigestPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedHashPasswordSpec;

public class ScramNoSaltTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();

    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ScramDigestPassword.ALGORITHM_SCRAM_SHA_512, ELYTRON_PROVIDER);

        ClearPasswordSpec clearSpec = new ClearPasswordSpec(TEST_PASSWORD.toCharArray());

        ScramDigestPassword original = (ScramDigestPassword) passwordFactory.generatePassword(clearSpec);

        byte[] salt = original.getSalt();
        byte[] digest = original.getDigest();
        int iterationCount = original.getIterationCount();

        IteratedSaltedHashPasswordSpec scramPasswordSpec = new IteratedSaltedHashPasswordSpec(digest, salt, iterationCount);

        ScramDigestPassword restored = (ScramDigestPassword) passwordFactory.generatePassword(scramPasswordSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
