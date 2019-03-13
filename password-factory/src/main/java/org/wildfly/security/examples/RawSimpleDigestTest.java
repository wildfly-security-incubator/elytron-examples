package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.SimpleDigestPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;

public class RawSimpleDigestTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(SimpleDigestPassword.ALGORITHM_SIMPLE_DIGEST_SHA_512, ELYTRON_PROVIDER);

        ClearPasswordSpec clearSpec = new ClearPasswordSpec(TEST_PASSWORD.toCharArray());
        SimpleDigestPassword original = (SimpleDigestPassword) passwordFactory.generatePassword(clearSpec);

        byte[] digest = original.getDigest();

        SimpleDigestPassword rawPassword = SimpleDigestPassword.createRaw(SimpleDigestPassword.ALGORITHM_SIMPLE_DIGEST_SHA_512, digest);

        SimpleDigestPassword restored = (SimpleDigestPassword) passwordFactory.translate(rawPassword);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(restored, TEST_PASSWORD.toCharArray())));
    }

}
