package org.wildfly.examples;

import java.security.Provider;

public class MyCredentialStoreProvider extends Provider {

    public MyCredentialStoreProvider() {
        super("MyCredentialStoreProvider", "1.0", "My Credential Store Provider");
        put("my-credential-store", MyCredentialStoreSpi.class.getName());
    }
}
