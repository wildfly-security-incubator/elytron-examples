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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.wildfly.security.x500.GeneralName;
import org.wildfly.security.x500.cert.SelfSignedX509CertificateAndSigningKey;
import org.wildfly.security.x500.cert.SubjectAlternativeNamesExtension;
import org.wildfly.security.x500.cert.X509CertificateBuilder;

/**
 * Utility to dynamically generate certificates
 * @author <a href="mailto:jucook@redhat.com">Justin Cook</a>
 */
public class CertificateGenerator {

    private static final String RFC_822_NAME = "rfc822Name";
    private static final String DNS_NAME = "dNSName";
    private static final String DIRECTORY_NAME = "directoryName";
    private static final String URI_NAME = "uniformResourceIdentifier";
    private static final String IP_ADDRESS = "iPAddress";
    private static final String REGISTERED_ID = "registeredID";

    public final String ALGORITHMS_RSA = "RSA";
    public final String ALGORITHMS_SHA_1_RSA = "SHA1withRSA";
    public final String ALGORITHMS_SHA_256_RSA = "SHA256withRSA";
    public final String ALGORITHMS_MD_5_RSA = "MD5withRSA";
    public final String KEYSTORE_TYPES_JKS = "JKS";
    public final String KEYSTORE_TYPES_PKCS12 = "PKCS12";
    public final int KEY_SIZES_1024 = 1024;
    public final int KEY_SIZES_2048 = 2048;

    private char[] keyStorePassword = "keystorepass".toCharArray();
    private char[] trustStorePassword = "truststorepass".toCharArray();
    private String outputLocation;
    private String authorityAlias = "CA";
    private String authorityDN = "CN=Elytron CA, ST=Elytron, C=UK, EMAILADDRESS=elytron@wildfly.org, O=Root Certificate Authority";
    private String keyStoreType = KEYSTORE_TYPES_JKS;
    private String keyStoreName = "keystore.jks";
    private String trustStoreName = "truststore.jks";
    private int keySize = KEY_SIZES_2048;
    private ArrayList<String> aliases;
    private ArrayList<String> distinguishedNames;
    private ArrayList<String> subjectAltNames;
    private ArrayList<char[]> keyPasswords;
    private ArrayList<String> serialNumbers;
    private KeyStore keyStore;
    private KeyStore trustStore;
    private String keyAlg = ALGORITHMS_RSA;
    private String sigAlg = ALGORITHMS_SHA_256_RSA;
    private ArrayList<KeyPair> keyPairs = new ArrayList<>();

    /**
     * Creates the object with defaults and required parameters
     */
    public static class Builder {
        private ArrayList<String> aliases = new ArrayList<>();
        private ArrayList<String> distinguishedNames = new ArrayList<>();
        private ArrayList<String> subjectAltNames = new ArrayList<>();
        private ArrayList<char[]> keyPasswords = new ArrayList<>();
        private ArrayList<String> serialNumbers = new ArrayList<>();
        private String outputLocation = new File(CertificateGenerationExample.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().toString();

        /**
         * Create the builder
         */
        public Builder() {
        }

        /**
         * Sets a single certificate's alias, can be reused to avoid creating an ArrayList
         *
         * @param alias The alias for a certificate
         * @return The Builder object so far
         */
        public Builder addAlias(String alias) {
            this.aliases.add(alias);
            return this;
        }

        /**
         * Sets multiple aliases to be used over multiple certificates at once, overrides setAlias.
         * The order of the ArrayList corresponds to the order key passwords, serial numbers, and DNs are set.
         *
         * @param aliases The ArrayList of aliases to be used
         * @return The Builder object so far
         */
        public Builder setAliases(ArrayList<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        /**
         * Sets a single certificate's DN, can be reused to avoid creating an ArrayList
         *
         * @param distinguishedName The DN for a certificate
         * @return The Builder object so far
         */
        public Builder addDN(String distinguishedName) {
            this.distinguishedNames.add(distinguishedName);
            return this;
        }

        /**
         * Sets multiple DNs to be used over multiple certificates at once, overrides setDN.
         * The order of the ArrayList corresponds to the order key passwords, aliases, and serial numbers are set.
         *
         * @param distinguishedNames The ArrayList of DNs to be used
         * @return The Builder object so far
         */
        public Builder setDNs(ArrayList<String> distinguishedNames) {
            this.distinguishedNames = distinguishedNames;
            return this;
        }

        /**
         * Sets a single certificate's password, can be reused to avoid creating an ArrayList
         *
         * @param keyPassword The password for a certificate
         * @return The Builder object so far
         */
        public Builder addKeyPassword(char[] keyPassword) {
            this.keyPasswords.add(keyPassword);
            return this;
        }

        /**
         * Sets multiple passwords to be used over multiple certificates at once, overrides withKeyPassword.
         * The order of the ArrayList corresponds to the order serial numbers, aliases, and DNs are set.
         *
         * @param keyPasswords The ArrayList of key passwords to be used
         * @return The Builder object so far
         */
        public Builder withKeyPasswords(ArrayList<char[]> keyPasswords) {
            this.keyPasswords = keyPasswords;
            return this;
        }

        /**
         * Sets a single certificate's serial number, can be reused to avoid creating an ArrayList
         *
         * @param serialNumber The DN for a certificate
         * @return The Builder object so far
         */
        public Builder addSerialNumber(String serialNumber) {
            this.serialNumbers.add(serialNumber);
            return this;
        }

        /**
         * Sets multiple serial numbers to be used over multiple certificates at once, overrides setSerialNumber.
         * The order of the ArrayList corresponds to the order key passwords, aliases, and DNs are set.
         *
         * @param serialNumbers The ArrayList of DNs to be used
         * @return The Builder object so far
         */
        public Builder withSerialNumbers(ArrayList<String> serialNumbers) {
            this.serialNumbers = serialNumbers;
            return this;
        }

        /**
         * Changes the output directory from the default of target/keystores_truststores
         *
         * @param outputLocation The path to the output directory
         */
        public Builder setOutputLocation(String outputLocation) {
            this.outputLocation = outputLocation;
            return this;
        }

        /**
         * Builds the CertificateGenerator and checks that equal amounts of aliases, DNs, and key passwords are set.
         *
         * @return The CertificateGenerator
         * @throws Exception Error occurring due to unequal amount of aliases, DNs, and/or key passwords
         */
        public CertificateGenerator build() throws Exception {
            if (this.aliases.size() != this.distinguishedNames.size()) {
                if(this.aliases.size() != this.keyPasswords.size() && !this.keyPasswords.isEmpty()) {
                    throw new Exception("Unequal amounts of aliases, distinguished names, and keyPasswords.");
                } else {
                    throw new Exception("Unequal amounts of aliases and distinguished names.");
                }
            }
            if (!outputLocation.endsWith("/")) outputLocation += "/";
            return new CertificateGenerator(this);
        }
    }

    private CertificateGenerator(Builder builder) {
        this.aliases = builder.aliases;
        this.distinguishedNames = builder.distinguishedNames;
        this.subjectAltNames = builder.subjectAltNames;
        this.keyPasswords = builder.keyPasswords;
        this.outputLocation = builder.outputLocation;
    }

    /**
     * Sets a new keystore password
     *
     * @param password The password for the keystore
     */
    public void setKeyStorePassword(char[] password) {
        this.keyStorePassword = password;
    }

    /**
     * Sets a new truststore password
     *
     * @param password The password for the truststore
     */
    public void setTrustStorePassword(char[] password) {
        this.trustStorePassword = password;
    }

    /**
     * Sets a new output directory for the keystores/truststores
     *
     * @param outputLocation The path to the output directory
     */
    public void setOutputLocation(String outputLocation) {
        if (!outputLocation.endsWith("/")) outputLocation += "/";
        this.outputLocation = outputLocation;
    }

    /**
     * Sets a new certificate authority alias
     *
     * @param alias The new certificate authority alias
     */
    public void setAuthorityAlias(String alias) {
        this.authorityAlias = alias;
    }

    /**
     * Sets a new certificate authority DN
     *
     * @param distinguishedName The new certificate authority DN
     */
    public void setAuthorityDN(String distinguishedName) {
        this.authorityDN = distinguishedName;
    }

    /**
     * Sets a new keystore (and truststore) type
     *
     * @param keyStoreType The keystore type
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * Sets a new keystore name
     *
     * @param keyStoreName The new keystore name
     */
    public void setKeyStoreName(String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    /**
     * Sets a new truststore name
     *
     * @param trustStoreName The new truststore name
     */
    public void setTrustStoreName(String trustStoreName) {
        this.trustStoreName = trustStoreName;
    }

    /**
     * Sets a new key size
     *
     * @param keySize The new key size
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    /**
     * Sets a new alias
     *
     * @param alias The new alias
     */
    public void addAlias(String alias) {
        this.aliases.add(alias);
    }

    /**
     * Sets a single new DN
     *
     * @param distinguishedName The new DN
     */
    public void addDistinguishedName(String distinguishedName) {
        this.distinguishedNames.add(distinguishedName);
    }

    /**
     * Sets a single new subject alt name.
     *
     * @param subjectAltName the new subject alt name
     */
    public void addSubjectAltName(String subjectAltName) {
        this.subjectAltNames.add(subjectAltName);
    }

    /**
     * Sets a single new key password
     *
     * @param keyPassword The new password
     */
    public void addKeyPassword(char[] keyPassword) {
        this.keyPasswords.add(keyPassword);
    }

    /**
     * Sets a single new serial number
     *
     * @param serialNumber The new serial number
     */
    public void addSerialNumber(String serialNumber) {
        this.serialNumbers.add(serialNumber);
    }

    /**
     * Wipes the current aliases
     */
    public void clearAliases() {
        this.aliases.clear();
    }

    /**
     * Wipes the current DNs
     */
    public void clearDistinguishedNames() {
        this.distinguishedNames.clear();
    }

    /**
     * Wipes the current subject alt name.
     */
    public void clearSubjectAltNames() {
        this.subjectAltNames.clear();
    }

    /**
     * Wipes the current passwords
     */
    public void clearKeyPasswords() {
        this.keyPasswords.clear();
    }

    /**
     * Wipes the current serial numbers
     */
    public void clearSerialNumbers() {
        this.serialNumbers.clear();
    }

    /**
     * Wipes the current aliases and sets new ones. See Builder for more details.
     *
     * @param aliases The new ArrayList of aliases
     */
    public void setAliases(ArrayList<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * Wipes the current DNs and sets new ones. See Builder for more details.
     *
     * @param distinguishedNames The new ArrayList of DNs
     */
    public void setDistinguishedNames(ArrayList<String> distinguishedNames) {
        this.distinguishedNames = distinguishedNames;
    }

    /**
     * Wipes the current passwords and sets new ones. See Builder for more details.
     *
     * @param keyPasswords The new ArrayList of passwords
     */
    public void setKeyPasswords(ArrayList<char[]> keyPasswords) {
        this.keyPasswords.clear();
        this.keyPasswords.addAll(keyPasswords);
    }

    /**
     * Wipes the current serial numbers and sets new ones. See Builder for more details.
     *
     * @param serialNumbers The new ArrayList of serial numbers
     */
    public void setSerialNumbers(ArrayList<String> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }

    /**
     * Sets a new key algorithm
     *
     * @param keyAlg The new algorithm
     */
    public void setKeyAlg(String keyAlg) {
        this.keyAlg = keyAlg;
    }

    /**
     * Sets a new signature algorithm
     *
     * @param sigAlg The new algorithm
     */
    public void setSigAlg(String sigAlg) {
        this.sigAlg = sigAlg;
    }

    /**
     * Single method to generate a keystore and truststore and then saves them to file
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void generate() throws Exception {
        SelfSignedX509CertificateAndSigningKey authority = createAuthority();
        List<X509Certificate> certificates = createSignedCertificates(authority);
        createKeyStoreAndTrustStoreAndSaveToFile(authority, certificates);
    }

    /**
     * Method to generate truststores and keystores for 2-way-ssl using some default values
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void generateTwoWaySSL() throws Exception {
        ArrayList<String> distinguishedNamesList = new ArrayList<>();
        ArrayList<String> subjectAltNamesList = new ArrayList<>();
        distinguishedNamesList.add("CN=client1");
        distinguishedNamesList.add("CN=client2");
        generateTwoWaySSL(distinguishedNamesList, subjectAltNamesList);
    }

    public void generateTwoWaySSL(ArrayList<String> distinguishedNames) throws Exception {
        generateTwoWaySSL(distinguishedNames, null);
    }

    public void generateTwoWaySSL(ArrayList<String> distinguishedNames, ArrayList<String> subjectAltNames) throws Exception {
        boolean subjectAltNamesSpecified = subjectAltNames != null && ! subjectAltNames.isEmpty();
        addAlias("client1");
        addDistinguishedName(distinguishedNames.get(0));
        if (subjectAltNamesSpecified) {
            addSubjectAltName(subjectAltNames.get(0));
        }
        setTrustStoreName("server.truststore");
        setKeyStoreName("client1.keystore");
        SelfSignedX509CertificateAndSigningKey authority = createAuthority();

        List<X509Certificate> certificates = createSignedCertificates(authority);
        createKeyStoreAndTrustStore(authority, certificates);
        saveKeyStoreToFile();

        setKeyStoreName("client2.keystore");
        clearAliases();
        clearDistinguishedNames();
        clearSubjectAltNames();
        addAlias("client2");
        addDistinguishedName(distinguishedNames.get(1));
        if (subjectAltNamesSpecified) {
            addSubjectAltName(subjectAltNames.get(1));
        }
        certificates = createSignedCertificates(authority);
        createKeyStoreAndTrustStore(authority, certificates);
        saveKeyStoreToFile();

        setKeyStoreName("server.keystore");
        clearAliases();
        clearDistinguishedNames();
        if (subjectAltNamesSpecified) {
            clearSubjectAltNames();
        }
        addAlias("server");
        addDistinguishedName("CN=server");
        certificates = createSignedCertificates(authority);
        createKeyStore(authority.getSelfSignedCertificate(), certificates);
        saveKeyStoreAndTrustStoreToFile();

        setTrustStoreName("client1.truststore");
        saveTrustStoreToFile();

        setTrustStoreName("client2.truststore");
        saveTrustStoreToFile();
    }

    /**
     * Creates a self signed certificate to be used as a certificate authority
     *
     * @return The certificate and signing key for the authority
     */
    public SelfSignedX509CertificateAndSigningKey createAuthority() {
        return SelfSignedX509CertificateAndSigningKey.builder()
                .setDn(new X500Principal(authorityDN))
                .setKeyAlgorithmName(keyAlg)
                .setSignatureAlgorithmName(sigAlg)
                .setKeySize(keySize)
                .build();
    }

    /**
     * Creates a certificate for each given DN, signing each with the given certificate authority
     *
     * @param authority The certificate authority's certificate and signing key. Can be generated with createAuthority()
     * @return The list of signed certificates
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public List<X509Certificate> createSignedCertificates(SelfSignedX509CertificateAndSigningKey authority) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        keyPairs.clear();
        boolean subjectAltNamesSpecified = subjectAltNames != null && ! subjectAltNames.isEmpty();
        int i = 0;
        for (String distinguishedName : distinguishedNames) {
            String subjectAltName = subjectAltNamesSpecified ? subjectAltNames.get(i) : null;
            certificates.add(createCertificate(authority, generateKeyPairAndReturnPublicKey(), distinguishedName, subjectAltName));
            if (subjectAltNamesSpecified) {
                i++;
            }
        }
        return certificates;
    }

    /**
     * Creates a certificate for each given DN, signing each with the given certificate authority. Allows for a
     * serial number to be set for each certificate
     *
     * @param authority The certificate authority's certificate and signing key. Can be generated with createAuthority()
     * @param serialNumbers The list of serial numbers to be used for the certificates
     * @return The list of signed certificates
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public List<X509Certificate> createSignedCertificates(SelfSignedX509CertificateAndSigningKey authority, List<String> serialNumbers) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        keyPairs.clear();
        for (int i = 0; i < distinguishedNames.size(); i++) {
            certificates.add(createCertificate(authority, generateKeyPairAndReturnPublicKey(), distinguishedNames.get(i), serialNumbers.get(i)));
        }
        return certificates;
    }

    /**
     *  Creates the keystore using the authority certificate and signed certificates
     *
     * @param authorityCertificate The certificate authority's certificate
     * @param certificates The list of certificates signed by the certificate authority
     * @throws KeyStoreException Error from loadKeyStore() or error encountered while attempting to set the key entry in the keystore
     * @throws IOException Described in loadKeyStore()
     * @throws NoSuchAlgorithmException Described in loadKeyStore()
     * @throws CertificateException Described in loadKeyStore()
     */
    public void createKeyStore(X509Certificate authorityCertificate, List<X509Certificate> certificates) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keyStore = loadKeyStore();
        for (int i = 0; i < aliases.size(); i++) {
            String alias = aliases.get(i);
            X509Certificate certificate = certificates.get(i);
            PrivateKey privateKey = keyPairs.get(i).getPrivate();
            if (keyPasswords.isEmpty()) {
                keyStore.setKeyEntry(alias, privateKey, keyStorePassword, new X509Certificate[]{certificate, authorityCertificate});
            } else {
                keyStore.setKeyEntry(alias, privateKey, keyPasswords.get(i), new X509Certificate[]{certificate, authorityCertificate});
            }
        }
    }

    /**
     * Creates a trust store that trusts the certificate authority
     *
     * @param authorityCertificate The trusted certificate of the certificate authority
     * @throws KeyStoreException Error from loadKeyStore() or error encountered while attempting to set the certificate entry in the truststore
     * @throws IOException Described in loadKeyStore()
     * @throws NoSuchAlgorithmException Described in loadKeyStore()
     * @throws CertificateException Described in loadKeyStore()
     */
    public void createTrustStore(X509Certificate authorityCertificate) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        trustStore = loadKeyStore();
        trustStore.setCertificateEntry(authorityAlias, authorityCertificate);
    }

    /**
     * Creates both a keystore and truststore. See createKeyStore() and createTrustStore() for more details
     *
     * @param authority The certificate authority's certificate and signing key. Can be generated with createAuthority()
     * @param certificates The list of certificates signed by the certificate authority
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void createKeyStoreAndTrustStore(SelfSignedX509CertificateAndSigningKey authority, List<X509Certificate> certificates) throws Exception {
        X509Certificate authorityCertificate = authority.getSelfSignedCertificate();
        createTrustStore(authorityCertificate);
        createKeyStore(authority.getSelfSignedCertificate(), certificates);
    }

    /**
     * Saves the keystore to file. See createKeyStoreFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void saveKeyStoreToFile() throws Exception {
        createKeyStoreFile(keyStore, keyStoreName, keyStorePassword);
    }

    /**
     * Saves the truststore to file. See createKeyStoreFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void saveTrustStoreToFile() throws Exception {
        createKeyStoreFile(trustStore, trustStoreName, trustStorePassword);
    }

    /**
     * Saves the keystore and truststore to file. See createKeyStoreFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void saveKeyStoreAndTrustStoreToFile() throws Exception {
        createKeyStoreFile(trustStore, trustStoreName, trustStorePassword);
        createKeyStoreFile(keyStore, keyStoreName, keyStorePassword);
    }

    /**
     * Creates the keystore and then saves it to file. See createKeyStore() and saveKeyStoreToFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void createKeyStoreAndSaveToFile(SelfSignedX509CertificateAndSigningKey authority, List<X509Certificate> certificates) throws Exception {
        createKeyStore(authority.getSelfSignedCertificate(), certificates);
        saveKeyStoreToFile();
    }

    /**
     * Creates the truststore and then saves it to file. See createTrustStore() and saveTrustStoreToFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void createTrustStoreAndSaveToFile(SelfSignedX509CertificateAndSigningKey authority) throws Exception {
        createTrustStore(authority.getSelfSignedCertificate());
        saveTrustStoreToFile();
    }

    /**
     * Creates the keystore and truststore and then saves them to file. See createKeyStoreAndTrustStore() and saveKeyStoreAndTrustStoreToFile() for more details
     *
     * @throws Exception Errors encountered during generation, described in their corresponding methods
     */
    public void createKeyStoreAndTrustStoreAndSaveToFile(SelfSignedX509CertificateAndSigningKey authority, List<X509Certificate> certificates) throws Exception {
        createKeyStoreAndTrustStore(authority, certificates);
        saveKeyStoreAndTrustStoreToFile();
    }

    /**
     * Creates an X509Certificate signed by the certificate authority with a random serial number
     *
     * @param issuerSelfSignedX509CertificateAndSigningKey The certificate and signing key of the certificate authority
     * @param publicKey The public key for the certificate
     * @param subjectDN The DN of the certificate's subject
     * @return The X509Certificate that is generated
     * @throws CertificateException Errors encountered generating the X509Certificate
     */
    private X509Certificate createCertificate(SelfSignedX509CertificateAndSigningKey issuerSelfSignedX509CertificateAndSigningKey, PublicKey publicKey, String subjectDN) throws CertificateException {
        return createCertificate(issuerSelfSignedX509CertificateAndSigningKey, publicKey, subjectDN, null, null);
    }

    private X509Certificate createCertificate(SelfSignedX509CertificateAndSigningKey issuerSelfSignedX509CertificateAndSigningKey, PublicKey publicKey, String subjectDN, String subjectAltName) throws CertificateException {
        return createCertificate(issuerSelfSignedX509CertificateAndSigningKey, publicKey, subjectDN, null, subjectAltName);
    }

    /**
     * Creates an X509Certificate signed by the certificate authority with a specific serial number
     *
     * @param issuerSelfSignedX509CertificateAndSigningKey The certificate and signing key of the certificate authority
     * @param publicKey The public key for the certificate
     * @param subjectDN The DN of the certificate's subject
     * @param serialNumber The serial number of the certificate
     * @return The X509Certificate that is generated
     * @throws CertificateException Errors encountered generating the X509Certificate
     */
    private X509Certificate createCertificate(SelfSignedX509CertificateAndSigningKey issuerSelfSignedX509CertificateAndSigningKey, PublicKey publicKey, String subjectDN, String serialNumber, String subjectAltName) throws CertificateException {
        boolean subjectAltNameSpecified = subjectAltName != null;
        GeneralName generalName = null;
        BigInteger serialNumberValue;
        if (serialNumber == null) {
            serialNumberValue = new BigInteger(Integer.toString((int) (Math.random() * 10000 + 1)));
        } else {
            serialNumberValue = new BigInteger(serialNumber);
        }

        if (subjectAltNameSpecified) {
            String[] subjectAltNameKeyAndValue = subjectAltName.split("=");
            switch (subjectAltNameKeyAndValue[0]) {
                case RFC_822_NAME:
                    generalName = new GeneralName.RFC822Name(subjectAltNameKeyAndValue[1]);
                    break;
                case DNS_NAME:
                    generalName = new GeneralName.DNSName(subjectAltNameKeyAndValue[1]);
                    break;
                case DIRECTORY_NAME:
                    generalName = new GeneralName.DirectoryName(subjectAltNameKeyAndValue[1]);
                    break;
                case URI_NAME:
                    generalName = new GeneralName.URIName(subjectAltNameKeyAndValue[1]);
                    break;
                case IP_ADDRESS:
                    generalName = new GeneralName.IPAddress(subjectAltNameKeyAndValue[1]);
                    break;
                case REGISTERED_ID:
                    generalName = new GeneralName.RegisteredID(subjectAltNameKeyAndValue[1]);
                    break;
            }
        }
        X509CertificateBuilder builder = new X509CertificateBuilder();
        builder.setIssuerDn(issuerSelfSignedX509CertificateAndSigningKey.getSelfSignedCertificate().getIssuerX500Principal())
                .setSubjectDn(new X500Principal(subjectDN))
                .setSignatureAlgorithmName(sigAlg)
                .setSigningKey(issuerSelfSignedX509CertificateAndSigningKey.getSigningKey())
                .setPublicKey(publicKey)
                .setSerialNumber(serialNumberValue);
        if (generalName != null) {
            builder.addExtension(new SubjectAlternativeNamesExtension(true, Arrays.asList(generalName)));
        }
        return builder.build();
    }

    /**
     * Generates a key pair for a certificate which is added to the list of key pairs and then returns the public key
     *
     * @return The public key of the generated key pair
     * @throws NoSuchAlgorithmException Error encountered if the given key algorithm is unknown, using the provided key algorithms should prevent this error
     */
    private PublicKey generateKeyPairAndReturnPublicKey() throws NoSuchAlgorithmException {
        KeyPair keyPair = KeyPairGenerator.getInstance(keyAlg).generateKeyPair();
        keyPairs.add(keyPair);
        return keyPair.getPublic();
    }

    /**
     * Loads a new KeyStore instance
     *
     * @return the KeyStore
     * @throws KeyStoreException Error encountered if the keystore type is unknown, using the provided keystore types should prevent this error
     * @throws IOException See KeyStore.load(), should never happen
     * @throws NoSuchAlgorithmException See KeyStore.load(), should never happen
     * @throws CertificateException See KeyStore.load(), should never happen
     */
    private KeyStore loadKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(null, null);
        return ks;
    }

    /**
     * Creates the keystore on the file system
     *
     * @param keyStore KeyStore to be saved
     * @param outputFile Path to where the keystore should be saved
     * @param password The password to save the keystore with
     * @throws KeyStoreException See KeyStore.store()
     * @throws IOException See KeyStore.store()
     * @throws NoSuchAlgorithmException See KeyStore.store()
     * @throws CertificateException See KeyStore.store()
     */
    private void createKeyStoreFile(KeyStore keyStore, String outputFile, char[] password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        new File(outputLocation).mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outputLocation + outputFile)){
            keyStore.store(fos, password);
        }
    }
}
