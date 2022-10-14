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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * Simple test case to test the HOTP security realm implementation.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HotpSecurityRealmTest {

    private static SecurityRealm securityRealm;
    private static File temporaryFile;

    private static final String[] otpValues = new String[] {
            "755224",
            "287082",
            "359152",
            "969429",
            "338314",
            "254676",
            "287922",
            "162583",
            "399871",
            "520489" };

    @BeforeClass
    public static void createRealm() throws Exception {
        // We need to copy the realm data as the realm will be rewriting it during the test.
        temporaryFile = File.createTempFile("realm-", ".json", new File("target/"));
        try (InputStream input = new FileInputStream("src/test/java/simple-realm.json");
                OutputStream fos = new FileOutputStream(temporaryFile)) {
            byte[] bytes = new byte[256];
            int read;
            while ((read = input.read(bytes)) > 0) {
                fos.write(bytes, 0, read);
            }
        }

        HotpSecurityRealm securityRealm = new HotpSecurityRealm();
        securityRealm.initialize(Collections.singletonMap(HotpSecurityRealm.PATH, temporaryFile.getAbsolutePath()));

        HotpSecurityRealmTest.securityRealm = securityRealm;
    }

    @AfterClass
    public static void disposeRealm() {
        securityRealm = null;
        if (temporaryFile.exists()) {
            temporaryFile.delete();
        }
        temporaryFile = null;
    }

    @Test(expected = RealmUnavailableException.class)
    public void testNotInitiased() throws RealmUnavailableException {
        SecurityRealm secondRealm = new HotpSecurityRealm();
        secondRealm.getRealmIdentity(new NamePrincipal("TestUser"));
    }

    @Test
    public void testIdentityDoesntExist() throws RealmUnavailableException {
        RealmIdentity identity = securityRealm.getRealmIdentity(new NamePrincipal("IdentityTest"));
        assertFalse("An identity should be returned even if it doesn't exist.", identity.exists());
    }

    @Test
    public void testEvidenceVerification() throws RealmUnavailableException {
        for (String guess : otpValues) {
            singleGuess(guess);
        }
    }

    private void singleGuess(final String guess) throws RealmUnavailableException {
        RealmIdentity identity = securityRealm.getRealmIdentity(new NamePrincipal("TestIdentity"));
        assertTrue("The identity should have been loaded.", identity.exists());

        assertTrue("First use of OTP", identity.verifyEvidence(new PasswordGuessEvidence(guess.toCharArray())));
        assertFalse("Second use of OTP", identity.verifyEvidence(new PasswordGuessEvidence(guess.toCharArray())));

        identity = securityRealm.getRealmIdentity(new NamePrincipal("TestIdentity"));
        assertTrue("The identity should have been loaded.", identity.exists());

        assertFalse("Third use of OTP", identity.verifyEvidence(new PasswordGuessEvidence(guess.toCharArray())));
    }

}
