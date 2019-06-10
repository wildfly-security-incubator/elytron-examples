/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.examples;


import java.security.Provider;
import java.security.SecureRandom;
import java.util.Base64;

import org.wildfly.common.iteration.ByteIterator;
import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

/**
 * Creates and prints the hashed password and salt for passwords, with default base64 encoding. Hex encoding or modular
 * crypt can be specified
 *
 * @author <a href="mailto:aabdelsa@redhat.com">Ashley Abdel-Sayed</a>
 *  
 */
public class GenerateBCryptPassword {

    static final Provider ELYTRON_PROVIDER = new WildFlyElytronProvider();

    /*
     * args[0] should be set to "true" if hex encoding is desired, false otherwise
     * args[1] should be set to "true" if modular crypt encoding is desired, false otherwise
     * args[2] should be set to the clear text password you would like to encode
     *
     */

    public static void main(String[] args) throws Exception {

        boolean hexEncoding = Boolean.parseBoolean(args[0]);
        boolean modEncoding = Boolean.parseBoolean(args[1]);
        String PASSWORD = args[2];

        createBCryptPassword(PASSWORD, hexEncoding, modEncoding);


    }
    /*
     * Generates and prints a BCrypt password and salt with default 10 iteration counts and base64 encoding.
     * Hex encoding rather than base64 encoding can be specified.
     * Modular crypt encoding can be specified and the password, salt, and iteration counts can be encoded
     * in a single string.
     *
     */
    public static void createBCryptPassword(String password, boolean hexEncoded, boolean modEncoding) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, ELYTRON_PROVIDER);

        int iterationCount = 10;

        byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt);
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(password.toCharArray(), iteratedAlgorithmSpec);

        BCryptPassword original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);


        if(modEncoding) {
            String modularCryptString = ModularCrypt.encodeAsString(original);
            System.out.println("Modular Crypt = " + modularCryptString);
        } else {
            byte[] hash = original.getHash();
            String encodedHash;
            String encodedSalt;
            if(hexEncoded) {
                encodedHash = ByteIterator.ofBytes(hash).hexEncode().drainToString();
                encodedSalt = ByteIterator.ofBytes(salt).hexEncode().drainToString();

            } else {
                Base64.Encoder encoder = Base64.getEncoder();
                encodedHash = encoder.encodeToString(hash);
                encodedSalt = encoder.encodeToString(salt);
            }
            System.out.println("Encoded Hash = " + encodedHash);
            System.out.println("Encoded Salt = " + encodedSalt);
        }
    }
}
