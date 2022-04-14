package org.wildfly.examples;

import java.security.Provider;
import java.util.Collections;

public class MyCredentialStoreProvider extends Provider {

    public MyCredentialStoreProvider() {
        super("MyCredentialStoreProvider", "1.0", "My Credential Store Provider");

        putService(new Service(this, "CredentialStore",
            MyCredentialStore.class.getSimpleName(), MyCredentialStore.class.getName(),
            Collections.emptyList(), Collections.emptyMap()));
    }
}
