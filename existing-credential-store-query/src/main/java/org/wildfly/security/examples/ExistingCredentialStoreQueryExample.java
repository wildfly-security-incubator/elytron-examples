/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.examples;

import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import org.wildfly.security.auth.server.IdentityCredentials;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStore.CredentialSourceProtectionParameter;
import org.wildfly.security.credential.store.CredentialStore.ProtectionParameter;
import org.wildfly.security.credential.store.WildFlyElytronCredentialStoreProvider;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.ClearPassword;

/**
 * Example demonstrating how a credential store created by WildFly CLI can be queried.
 */
public class ExistingCredentialStoreQueryExample {

    private static final Provider CREDENTIAL_STORE_PROVIDER = new WildFlyElytronCredentialStoreProvider();
    private static final Provider PASSWORD_PROVIDER = new WildFlyElytronPasswordProvider();

    static {
        Security.addProvider(PASSWORD_PROVIDER);
    }

    public static void main(String[] args) throws Exception {

        /*
         * Create a ProtectionParameter for access to the store.
         */
        Password storePassword = ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, "StorePassword".toCharArray());
        ProtectionParameter protectionParameter = new CredentialSourceProtectionParameter(IdentityCredentials.NONE.withCredential(new PasswordCredential(storePassword)));
        // Get an instance of the CredentialStore
        CredentialStore credentialStore = CredentialStore.getInstance("KeyStoreCredentialStore", CREDENTIAL_STORE_PROVIDER);

        // Initialise the CredentialStore
        Map<String, String> configuration = new HashMap<>();
        configuration.put("location", "/PATH/TO/credential-store.cs");
        configuration.put("modifiable", "true");
        configuration.put("keyStoreType", "JCEKS");
        credentialStore.initialize(configuration, protectionParameter);

        System.out.println("************************************");
        System.out.println("Current Aliases in credential store: ");
        for (String alias : credentialStore.getAliases()) {
            System.out.print(" - ");
            System.out.println(alias);
        }
        System.out.println("************************************");

        String queriedAlias = "my-secret-db-password";
        final PasswordCredential credential = credentialStore.retrieve(queriedAlias,PasswordCredential.class);
        final Password password = credential.castAndApply(PasswordCredential.class, ClearPassword.ALGORITHM_CLEAR, PasswordCredential::getPassword);
        final ClearPassword clearPassword = password.castAs(ClearPassword.class, ClearPassword.ALGORITHM_CLEAR);
        System.out.println("Your secret key for alias " + queriedAlias + " is: " + new String(clearPassword.getPassword()));
    }

}
