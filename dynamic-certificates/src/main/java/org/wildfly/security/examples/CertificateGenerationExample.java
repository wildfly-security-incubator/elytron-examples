/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.examples;

import java.io.File;
import java.util.ArrayList;

/**
 * Examples for how to use CertificateGenerator
 * @author <a href="mailto:jucook@redhat.com">Justin Cook</a>
 */
public class CertificateGenerationExample {
    private ArrayList<String> aliases = new ArrayList<>();
    private ArrayList<String> distinguishedNames = new ArrayList<>();
    private ArrayList<char[]> keyPasswords = new ArrayList<>();
    private static String outputLocation = CertificateGenerationExample.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "dynamic-certificates/custom/";

    public static void main(String[] args) throws Exception {
        ArrayList<String> distinguishedNamesList = new ArrayList<>();
        ArrayList<String> subjectAltNamesList = new ArrayList<>();
        if(args.length == 0) {
            distinguishedNamesList.add("CN=client1");
            distinguishedNamesList.add("CN=client2");
        } else {
            for (int i = 0; i < 2; i++) {
                distinguishedNamesList.add(args[i]);
            }
            if (args.length == 4) {
                for (int i = 2; i < 4; i++) {
                    subjectAltNamesList.add(args[i]);
                }
            }
        }
        generateKeyStoresForTwoWaySSL(distinguishedNamesList, subjectAltNamesList);
    }

    public static void generateKeyStoresForTwoWaySSL(ArrayList<String> distinguishedNamesList) throws Exception {
        generateKeyStoresForTwoWaySSL(distinguishedNamesList, null);


    }

    public static void generateKeyStoresForTwoWaySSL(ArrayList<String> distinguishedNamesList, ArrayList<String> subjectAltNamesList) throws Exception {
        new CertificateGenerator.Builder()
                .setOutputLocation(new File(CertificateGenerationExample.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().toString())
                .build()
                .generateTwoWaySSL(distinguishedNamesList, subjectAltNamesList);


    }

    public void generateKeyStoresWithDefaults() throws Exception {
        setAll();
        new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation)
                .setAliases(aliases)
                .setDNs(distinguishedNames)
                .withKeyPasswords(keyPasswords)
                .build()
                .generate();
    }

    public void generateKeyStoresWithSingleCertNoKeyPassword() throws Exception {
        new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation + "/single-no-key-pass/")
                .addAlias("alias1")
                .addDN("CN=dn1")
                .build()
                .generate();
    }

    public void generateKeyStoresWithSingleCert() throws Exception {
        new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation + "/single-key-pass/")
                .addAlias("alias1")
                .addDN("CN=dn1")
                .addKeyPassword("single_cert_keypass".toCharArray())
                .build()
                .generate();
    }

    public void generateKeyStoresByMethods() throws Exception {
        setAll();
        new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation + "/methods-key-pass/")
                .setAliases(aliases)
                .setDNs(distinguishedNames)
                .withKeyPasswords(keyPasswords)
                .build()
                .generate();
    }

    public void generateKeyStoresFullyCustomized() throws Exception {
        setAll();
        CertificateGenerator generator = new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation + "/fully-key-pass/")
                .setAliases(aliases)
                .setDNs(distinguishedNames)
                .withKeyPasswords(keyPasswords)
                .build();
        setCustomProperties(generator);
        generator.generate();
    }

    public void generateKeyStoresFullyCustomizedNoKeyPasswords() throws Exception {
        setAliases();
        setDistinguishedNames();
        CertificateGenerator generator = new CertificateGenerator.Builder()
                .setOutputLocation(outputLocation + "/fully-no-key-pass/")
                .setAliases(aliases)
                .setDNs(distinguishedNames)
                .build();
        setCustomProperties(generator);
        generator.generate();
    }

    private void setCustomProperties(CertificateGenerator generator) {
        generator.setKeyStorePassword("custom_password_keystore".toCharArray());
        generator.setTrustStorePassword("custom_password_truststore".toCharArray());
        generator.setAuthorityAlias("CA_Custom");
        generator.setAuthorityDN("CN=CA_Custom");
        generator.setKeyStoreType("pkcs12");
        generator.setKeyStoreName("custom_keystore.pkcs12");
        generator.setTrustStoreName("custom_truststore.pkcs12");
        generator.setKeySize(1024);
    }

    private void setAliases() {
        aliases.add("alias1");
        aliases.add("alias2");
        aliases.add("alias3");
    }

    private void setDistinguishedNames() {
        distinguishedNames.add("CN=dn1");
        distinguishedNames.add("CN=dn2");
        distinguishedNames.add("CN=dn3");
    }

    private void setKeyPasswords() {
        keyPasswords.add("key1".toCharArray());
        keyPasswords.add("key2".toCharArray());
        keyPasswords.add("key3".toCharArray());
    }

    private void setAll() {
        setAliases();
        setDistinguishedNames();
        setKeyPasswords();
    }
}
