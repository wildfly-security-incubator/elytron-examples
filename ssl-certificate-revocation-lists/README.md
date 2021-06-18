## ssl-certificate-revocation-lists: WildFly Elytron SSL Configuration using Certificate Revocation Lists

This example demonstrates how configure certificate revocation lists on the client side with one way 
SSL communication using WildFly Elytron.

### Use of the WILDFLY_HOME variable

In the following instructions, replace ``WILDFLY_HOME`` with the actual path to your WildFly installation. 

## Generate a Self Signed Root Certificate Authority

+ Navigate to the directory where you would like to configure your Root Certificate Authority. 
We will refer to this directory as ```SSL_CONFIG``` throughout this README.

+ First, we generate a 4096-bit long RSA key for our Root Certificate Authority and store it in the file ``ca.key``:
```shell script
openssl genrsa -out ca.key 4096
Generating RSA private key, 4096 bit long modulus (2 primes)
..................................................................++++
.........................................................................................................................................................................................................++++
e is 65537 (0x010001)
```

+ Next, we create our self-signed Root CA certificate ``ca.crt`` as follows:

```shell script
openssl req -new -x509 -days 365 -key ca.key -out ca.crt
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [XX]:US
State or Province Name (full name) []:North Carolina
Locality Name (eg, city) [Default City]:Raleigh
Organization Name (eg, company) [Default Company Ltd]:WildFly
Organizational Unit Name (eg, section) []:jboss
Common Name (eg, your name or your server's hostname) []:Root Certificate Authority
Email Address []:
```

Note the information about the CA is not relevant to the configuration. 

### Create the Server Certificate 

+ Navigate to the server ``configuration`` directory:
```shell script
cd WILDFLY_HOME/standalone/configuration/
```

+ First, we generate the private key for the server: 
```shell script
openssl genrsa -out server.key 4096
Generating RSA private key, 4096 bit long modulus (2 primes)
....................++++
............................................................................++++
e is 65537 (0x010001)
```

+ Then, we generate a certificate signing request for our Root Certificate Authority. 
```shell script
openssl req -new -key server.key -out server.csr
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [XX]:US
State or Province Name (full name) []:North Carolina
Locality Name (eg, city) [Default City]:Raleigh
Organization Name (eg, company) [Default Company Ltd]:WildFly
Organizational Unit Name (eg, section) []:jboss
Common Name (eg, your name or your server's hostname) []:localhost
Email Address []:

Please enter the following 'extra' attributes
to be sent with your certificate request
A challenge password []:
An optional company name []:
```

Make sure you enter "localhost" as the Common Name, otherwise you might run into 
issues. 

+ Then, create the following files, which will be helpful when 
creating the certificate revocation list:

```shell script
touch SSL_CONFIG/certindex
echo 01 > SSL_CONFIG/certserial
echo 01 > SSL_CONFIG/crlnumber
```

+ Also create the configuration file ``SSL_CONFIG/ca.conf`` with the following content:
```
[ ca ]
default_ca = myca

[ crl_ext ]
# issuerAltName=issuer:copy  #this would copy the issuer name to altname
authorityKeyIdentifier=keyid:always

 [ myca ]
 dir = ./
 new_certs_dir = $dir
 unique_subject = no
 certificate = $dir/ca.crt
 database = $dir/certindex
 private_key = $dir/ca.key
 serial = $dir/certserial
 default_days = 730
 default_md = sha1
 policy = myca_policy
 x509_extensions = myca_extensions
 crlnumber = $dir/crlnumber
 default_crl_days = 730

 [ myca_policy ]
 commonName = supplied
 stateOrProvinceName = supplied
 countryName = optional
 emailAddress = optional
 organizationName = supplied
 organizationalUnitName = optional

 [ myca_extensions ]
 basicConstraints = CA:false
 subjectKeyIdentifier = hash
 authorityKeyIdentifier = keyid:always
 keyUsage = digitalSignature,keyEncipherment
 extendedKeyUsage = serverAuth
 crlDistributionPoints = URI:http://example.com/root.crl
 subjectAltName  = @alt_names

 [alt_names]
 DNS.1 = example.com
 DNS.2 = *.example.com
```

You must change the ``crlDistributionPoints`` to point to your domain if you wish 
to use distribution points to access the certificate revocation lists. 

+ Sign the server's certificate signing request with your Root Certificate Authority: 
```shell script
openssl ca -batch -config SSL_CONFIG/ca.conf -notext -in server.csr -out server.crt
Using configuration from ca.conf
Check that the request matches the signature
Signature ok
The Subject's Distinguished Name is as follows
countryName           :PRINTABLE:'US'
stateOrProvinceName   :ASN.1 12:'North Carolina'
localityName          :ASN.1 12:'Raleigh'
organizationName      :ASN.1 12:'WildFly'
organizationalUnitName:ASN.1 12:'jboss'
commonName            :ASN.1 12:'localhost'
Certificate is to be certified until Dec  9 19:15:58 2022 GMT (730 days)

Write out database with 1 new entries
Data Base Updated
```

### Create the Server KeyStore 

The server's keystore should include our server's certificate along with its entire certificate chain 
i.e. server.crt -> ca.crt.

+ Create the certificate chain as follows:
```shell script
cat server.crt SSL_CONFIG/ca.crt > chained.crt
```
For Windows, use notepad to concatenate certificates. 

+ Pack all the certificates, and the server private key into a pkcs12 file as follows:
```shell script
openssl pkcs12 -export -inkey server.key -in chained.crt -out chained.pkcs12
Enter Export Password:
Verifying - Enter Export Password:
```

Enter ``secret`` as the password. 

+ Import this pkcs12 file into a java keystore:
```shell script
keytool -importkeystore -srckeystore chained.pkcs12 -srcstoretype PKCS12 -destkeystore server.keystore
Enter destination keystore password:  
Re-enter new password: 
Enter source keystore password:  
Entry for alias 1 successfully imported.
Import command completed:  1 entries successfully imported, 0 entries failed or cancelled
```

Again, you can use the password ``secret`` for ``server.keystore``.

### Configure Client TrustStore 

For the client trust store, it suffices if it holds the certificate for the Root Certificate 
Authority. You can configure it as follows:

```shell script
keytool -import -file SSL_CONFIG/ca.crt -alias ca -keystore client.truststore -storepass secret 
Owner: CN=Root Certificate Authority, OU=jboss, O=WildFly, L=Raleigh, ST=North Carolina, C=US
Issuer: CN=Root Certificate Authority, OU=jboss, O=WildFly, L=Raleigh, ST=North Carolina, C=US
Serial number: 66724cf6a35ff0fd504e6b30bbbad73e8ae4e7b3
Valid from: Wed Dec 09 14:10:06 EST 2020 until: Thu Dec 09 14:10:06 EST 2021
Certificate fingerprints:
	 SHA1: 4A:63:B8:AA:D2:9A:D4:E4:21:14:C6:AA:4A:74:00:AD:02:DE:91:EB
	 SHA256: EC:06:81:49:F7:E6:D9:54:5E:77:79:EC:EE:D2:16:90:60:F6:B9:92:F3:4D:D9:2E:C2:F6:E8:28:4E:4D:52:98
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 4096-bit RSA key
Version: 3

Extensions: 

#1: ObjectId: 2.5.29.35 Criticality=false
AuthorityKeyIdentifier [
KeyIdentifier [
0000: CD CA 95 5D C3 6E 03 C4   A3 59 A5 0C 4D 8C 20 9E  ...].n...Y..M. .
0010: 52 0A C3 86                                        R...
]
]

#2: ObjectId: 2.5.29.19 Criticality=true
BasicConstraints:[
  CA:true
  PathLen:2147483647
]

#3: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: CD CA 95 5D C3 6E 03 C4   A3 59 A5 0C 4D 8C 20 9E  ...].n...Y..M. .
0010: 52 0A C3 86                                        R...
]
]

Trust this certificate? [no]:  y
Certificate was added to keystore
```

### Configure the Server

You configure the SSL context by running JBoss CLI commands. For your convenience, this quickstart batches the commands into a `configure-ssl.cli` script provided in the root directory of this example.

Before you begin, make sure you do the following:

+ Start the server with the standalone default profile as follows:
```shell script
WILDFLY_HOME/bin/standalone.sh
```

+ Review the `configure-ssl.cli` file in the root of this directory. Comments in the script describe the purpose of each block of commands.

+ Open a new terminal, navigate to the root directory of this quickstart, and run the following command, replacing `WILDFLY_HOME` with the path to your server:
```shell script
$ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=configure-ssl.cli
```

NOTE: For Windows, use the `WILDFLY_HOME\bin\jboss-cli.bat` script.

You should see the following result when you run the script:

```shell script
The batch executed successfully
process-state: reload-required
```

### Create the Certificate Revocation Lists 

+ Navigate to your ``SSL_CONFIG`` directory. 

+ Generate an empty CRL file as follows:
```shell script
openssl ca -config ca.conf -gencrl -keyfile ca.key -cert ca.crt -out ca.crl.pem
Using configuration from ca.conf 
```

+ Revoke the server's certificate using this command:
```shell script
openssl ca -config ca.conf -revoke WILDFLY_HOME/standalone/configuration/server.crt -keyfile ca.key -cert ca.crt
Using configuration from ca.conf
Revoking Certificate 01.
Data Base Updated 
```

+ Regenerate the CRL file as follows:
```shell script
openssl ca -gencrl -keyfile ca.key -cert ca.crt -out ca.crl.pem -config ca.conf 
Using configuration from ca.conf
```

+ Test the CRL with the following commands 
```shell script
cat ca.crt ca.crl.pem > test.pem 

openssl verify -extended_crl -verbose -CAfile test.pem -crl_check WILDFLY_HOME/standalone/configuration/server.crt
error 23 at 0 depth lookup: certificate revoked
error server.crt: verification failed
```

Error 23 indicates the certificate has been revoked, as expected. 

### Configuring wildfly-config.xml

The example application presents a simple client ``SslClient.java`` which checks the connection
to the server is successful. To ensure the client configures
certificate revocation lists, we have provided a common configuration framework in
``wildfly-config.xml``.

Review this file to see how the certificate revocation lists are configured in the client SSL 
context: 
```xml
<certificate-revocation-lists>
    <certificate-revocation-list path="PATH/TO/CRL"/>
</certificate-revocation-lists>
```
 
Notice how in this example we have only configured one CRL under the ``certificate-revocation-lists``
tag, as we are only concerned that our client accepts certificates signed by the one CA we have configured. If we wanted 
our client to trust certificates from various CAs, and therefore be able to access the CRLs corresponding 
to each of those CAs, we could simply add additional ``certificate-revocation-list`` objects. 

As a final step in configuring the client, update the path to the client's trust store with the 
path to your trust store. Additionally, also update the ``certificate-revocation-list`` tag to the 
path the CRL we configured earlier. 

#### Build and Deploy the Application

Deploy the application by navigating to the root directory for this example and running 
the following commands:

```
cd ssl-server
mvn clean package wildfly:deploy
```

This deploys the ``ssl-server.war`` to the running instance of the server.

You should see a message in the server log indicating that the archive deployed successfully.

### Run Client and Verify Certificate Rejection

Run client test as follows:
```
cd ../ssl-client
mvn clean install -Dtest=SslClient
```

You should see the following error in the log:
``` 
  test(SslClient): RESTEASY004655: Unable to invoke request: 
javax.net.ssl.SSLHandshakeException: PKIX path validation failed: java.security.cert.CertPathValidatorException: 
Certificate has been revoked, reason: UNSPECIFIED, revocation date: Wed Dec 09 14:26:04 EST 2020, 
authority: CN=Root Certificate Authority, OU=jboss, O=WildFly, L=Raleigh, ST=North Carolina, C=US, extension OIDs: []
```

Alternatively, you could comment out the entire ``certificate-revocation-lists`` tag in your 
``wildfy-config.xml`` file and verify that communication succeeds otherwise. 

### Undeploy the Application 

When you are finished testing the application, follow these steps to undeploy the archive:

1. Make sure you start the WildFly server. 
2. Open a terminal and navigate to the root directory of this example. 
3. Type the following commands to undeploy the archive:

```shell script
cd ssl-server
mvn wildfly:undeploy
```

### Restore the WildFly Standalone Server Configuration 
You can restore the original server configuration by running the ``restore-configuration.cli``
script provided by navigating to the root directory of this application and running: 

```shell script
WILDFLY_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```