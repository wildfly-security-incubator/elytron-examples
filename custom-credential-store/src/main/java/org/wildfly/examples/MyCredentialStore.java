/*
 * Copyright 2022 Red Hat, Inc.
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

package org.wildfly.examples;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.credential.store.CredentialStoreSpi;
import org.wildfly.security.credential.store.UnsupportedCredentialTypeException;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;

public class MyCredentialStore extends CredentialStoreSpi {

    private PasswordFactory passwordFactory;

    public MyCredentialStore() {
        try {
            passwordFactory = PasswordFactory.getInstance(ClearPassword.ALGORITHM_CLEAR);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(Map<String, String> map,
                           CredentialStore.ProtectionParameter protectionParameter,
                           Provider[] providers) throws CredentialStoreException {

    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public void store(String s, Credential credential,
                      CredentialStore.ProtectionParameter protectionParameter)
        throws CredentialStoreException, UnsupportedCredentialTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C extends Credential> C retrieve(String credentialAlias, Class<C> credentialType, String credentialAlgo,
                                             AlgorithmParameterSpec algorithmParameterSpec,
                                             CredentialStore.ProtectionParameter protectionParameter)
        throws CredentialStoreException {
        byte[] credentialAliasBytes = credentialAlias.getBytes();

        final Password password;
        try {
            password =
                passwordFactory.generatePassword(new ClearPasswordSpec(new String(credentialAliasBytes,
                    Charset.forName("US-ASCII")).toCharArray()));
            final Credential credential = new PasswordCredential(password);
            return credentialType.cast(credential);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void remove(String s, Class<? extends Credential> aClass, String s1,
                       AlgorithmParameterSpec algorithmParameterSpec)
        throws CredentialStoreException {
        throw new UnsupportedOperationException();
    }
}
