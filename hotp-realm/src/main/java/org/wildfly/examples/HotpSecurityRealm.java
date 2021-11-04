/*
 * Copyright 2021 Red Hat, Inc.
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

import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;

import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * A simple security realm implementation which supports identities
 * which can verify evidence using OATH HOTP.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HotpSecurityRealm implements SecurityRealm {

    private volatile boolean initialised = false;

    public void initialize(final Map<String, String> configuration) {
        // TODO Load identities from file which must be specified in the configuration.

        initialised = true;
    }

    /**
     * Write the full collection if identities back to file.
     *
     * To be called after each evidence verification to output the set of identities and updated counters.
     */
    void save() {

    }

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        if (!initialised) {
            throw new RealmUnavailableException("This security realm has not been initialised.")l
        }

        return RealmIdentity.NON_EXISTENT;
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType, String algorithmName,
            AlgorithmParameterSpec parameterSpec) throws RealmUnavailableException {
        // This simple realm implementation only supports evidence verification.
        return SupportLevel.UNSUPPORTED;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName)
            throws RealmUnavailableException {

        if (PasswordGuessEvidence.class.isAssignableFrom(evidenceType)) {
            return SupportLevel.SUPPORTED;
        }

        return SupportLevel.UNSUPPORTED;
    }

    class HotpRealmIdentity implements RealmIdentity {

        private final Principal principal;

        HotpRealmIdentity(final Principal principal) {
            this.principal = principal;
        }

        @Override
        public Principal getRealmIdentityPrincipal() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType, String algorithmName,
                AlgorithmParameterSpec parameterSpec) throws RealmUnavailableException {
            return HotpSecurityRealm.this.getCredentialAcquireSupport(credentialType, algorithmName, parameterSpec);
        }

        @Override
        public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName)
                throws RealmUnavailableException {
            return HotpSecurityRealm.this.getEvidenceVerifySupport(evidenceType, algorithmName);
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType) throws RealmUnavailableException {
            // This realm does not support returning a credential so return null.
            return null;
        }

        @Override
        public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            return true;
        }

    }

}
