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

import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.wildfly.common.iteration.ByteIterator;


/**
 * A simple utility to generate a set of identities and also log the next valid OTP values for each identity.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class IdentityGenerator {

    private static final String NAME_PATTERN = "User%d";

    /*
     * Key generation should be using a secure source of random data but this
     * is sufficient for an example.
     */
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static RawHotpIdentity generateIdentity(final int count) {
        String name = String.format(NAME_PATTERN, count);
        String secret = createSecret();

        return new RawHotpIdentity(name, secret);
    }

    private static String createSecret() {
        byte[] secret = new byte[20];
        RANDOM.nextBytes(secret);

        return ByteIterator.ofBytes(secret).base32Encode().drainToString();
    }

    private static void logNextOTPValues(RawHotpIdentity identity) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] secret = identity.getRawHotpSecret();
        StringBuilder sb = new StringBuilder();
        sb.append("OTP Codes for ").append(identity.getName()).append(" ( ");
        long lastCount = identity.getLastHotpCount();
        for (long i = lastCount + 1; i < lastCount + 11; i++) {
            sb.append(OneTimePasswordAlgorithm.generateOTP(secret, i, 6, false, -1));
            sb.append(" ");
        }
        sb.append(")");
        System.out.println(sb.toString());
    }

    /**
     * @param args
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static void main(String[] args) throws Exception {

        System.out.println("*\n* Generating Set of Identities.\n*\n");

        List<RawHotpIdentity> identities = new ArrayList<>(10);
        for (int i=0; i<10; i++) {
            RawHotpIdentity identity = generateIdentity(i);
            logNextOTPValues(identity);
            identities.add(identity);
        }

        JsonbConfig config = new JsonbConfig()
                .withFormatting(true);

        Jsonb jsonb = JsonbBuilder.create(config);

        RealmData realmData = new RealmData();
        realmData.setIdentities(identities);

        if (args.length < 1) {
            System.out.println("\n*\n* Resulting JSON.\n*\n");

            String jsonString = jsonb.toJson(realmData);
            System.out.println(jsonString);
        } else {
            File file = new File(args[0]);
            try (FileOutputStream output = new FileOutputStream(file)) {
                jsonb.toJson(realmData, output);
            }

            System.out.println(String.format("\n*\n* Identities written to %s.\n*\n", file.getAbsolutePath()));
        }
    }

}
