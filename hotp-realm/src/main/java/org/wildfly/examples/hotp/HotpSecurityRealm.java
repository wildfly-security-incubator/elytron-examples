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

package org.wildfly.examples.hotp;

import static org.wildfly.examples.hotp.OneTimePasswordAlgorithm.generateOTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.MapAttributes;
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

    public static String PATH = HotpSecurityRealm.class.getName() + ".path";

    private volatile boolean initialised = false;
    private volatile File identityFile;
    private volatile RealmData realmData;

    public void initialize(final Map<String, String> configuration) {
        if (!configuration.containsKey(PATH)) {
            throw new IllegalStateException("No path file has been specified.");
        }

        File file = new File(configuration.get(PATH));
        if (!file.exists()) {
            throw new IllegalStateException(String.format("File \"%s\" does not exist.", file.getAbsolutePath()));
        }
        identityFile = file;

        Jsonb jsonb = JsonbBuilder.create();
        try (FileInputStream fis = new FileInputStream(file)) {
            realmData = jsonb.fromJson(fis, RealmData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load identities.", e);
        }

        initialised = true;
    }

    /**
     * Write the full collection if identities back to file.
     *
     * To be called after each evidence verification to output the set of identities and updated counters.
     */
    synchronized void save() throws IOException {
        JsonbConfig config = new JsonbConfig().withFormatting(true);

        Jsonb jsonb = JsonbBuilder.create(config);

        try (FileOutputStream fos = new FileOutputStream(identityFile)) {
            jsonb.toJson(realmData, fos);
        }
    }

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        if (!initialised) {
            throw new RealmUnavailableException("This security realm has not been initialised.");
        }

        String name = principal.getName();
        for (RawHotpIdentity identity : realmData.getIdentities()) {
            if (name.equals(identity.getName())) {
                return new HotpRealmIdentity(principal, identity);
            }
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
        private final RawHotpIdentity rawIdentity;

        HotpRealmIdentity(final Principal principal, final RawHotpIdentity rawIdentity) {
            this.principal = principal;
            this.rawIdentity = rawIdentity;
        }

        @Override
        public Principal getRealmIdentityPrincipal() {
            return principal;
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
            if (evidence instanceof PasswordGuessEvidence) {
                PasswordGuessEvidence pge = (PasswordGuessEvidence) evidence;
                String guess = new String(pge.getGuess());

                synchronized(rawIdentity) {
                    long thisCount = rawIdentity.getLastHotpCount() + 1;
                    try {
                        String nextOTP = generateOTP(rawIdentity.getRawHotpSecret(), thisCount, 6, false, -1);
                        if (guess.equals(nextOTP)) {
                            rawIdentity.setLastHotpCount(thisCount);
                            save();

                            return true;
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
                        throw new RealmUnavailableException("Realm unable to verify OTPs", e);
                    }
                }
            }

            return false;
        }

        @Override
        public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
            Map<String, Collection<String>> attributeMap = Collections.singletonMap("groups", rawIdentity.getGroups());
            Attributes attributes = new MapAttributes(attributeMap);

            return AuthorizationIdentity.basicIdentity(attributes);
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            return true;
        }

    }

}
