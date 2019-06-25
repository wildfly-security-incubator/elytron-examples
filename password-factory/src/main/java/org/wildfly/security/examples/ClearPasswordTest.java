package org.wildfly.security.examples;

import java.security.Provider;

import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;

public class ClearPasswordTest {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();
    static final String TEST_PASSWORD = "test_password";

    public static void main(String[] args) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ClearPassword.ALGORITHM_CLEAR, ELYTRON_PROVIDER);

        ClearPasswordSpec passwordSpec = new ClearPasswordSpec(TEST_PASSWORD.toCharArray());
        Password password = passwordFactory.generatePassword(passwordSpec);

        System.out.println(String.format("Password Verified '%b'", passwordFactory.verify(password, TEST_PASSWORD.toCharArray())));
    }

}
