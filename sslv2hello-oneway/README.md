## sslv2hello-oneway: WildFly Elytron SSL Configuration using SSLv2Hello Example 

 This example demonstrates how to configure a server to enable support for SSLv2Hello messages, 
 and how to configure clients to send SSLv2Hello messages. 

### Use of the WILDFLY_HOME variable

In the following instructions, replace ``WILDFLY_HOME`` with the actual path to your WildFly installation. 

## Generate a Keystore and Self-signed Certificate 

+ Open a terminal and navigate to the server `configuration` directory:

```shell script
$ cd WILDFLY_HOME/standalone/configuration/
```

+ Create a certificate for your server using the following command:
```shell script
$>keytool -genkey -alias localhost -keyalg RSA -sigalg MD5withRSA -keystore server.keystore -storepass secret -keypass secret -validity 9999

What is your first and last name?
   [Unknown]:  localhost
What is the name of your organizational unit?
   [Unknown]:  wildfly
What is the name of your organization?
   [Unknown]:  jboss
What is the name of your City or Locality?
   [Unknown]:  Raleigh
What is the name of your State or Province?
   [Unknown]:  Carolina
What is the two-letter country code for this unit?
   [Unknown]:  US
Is CN=localhost, OU=wildfly, O=jboss, L=Raleigh, ST=Carolina, C=US correct?
   [no]:  yes
```

Make sure you enter your desired "hostname" for the `first and last name` field, otherwise you might run into issues while permanently accepting this certificate as an exception in some browsers. Chrome does not currently exhibit this issue.

### Configure the Server

You configure the SSL context by running JBoss CLI commands. For your convenience, this quickstart batches the commands into a `configure-ssl.cli` script provided in the root directory of this example.

#### A note on enabling SSLv2Hello in newer JDK versions

Newer JDK versions have disabled TLSv1. Since we need the TLSv1 protocol to make use of 
SSLv2Hello, we will walk you through how to re-enable it on the server side. 

1. Access the following file: ``WILDFLY_HOME/bin/standalone.conf``. 
2. Look for the following line 
```shell script
# JAVA_OPTS="$JAVA_OPTS -Djava.security.properties==/path/to/custom/java.security"
```
3. Make sure to uncomment it and edit the path to the file ``enabled_tlsv1.properties`` in this example application. 
Additionally, make sure you use one ``=`` as opposed to two. That way you will not completely 
override the ``java.security`` file. 

Now you can continue with the rest of the server configuration.

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

#### Test the Server SSL Configuration 

To test the connection to the SSL port of your server instance, open a browser and navigate to
https://localhost:8443/.

You get the privacy error because the
server certificate is self-signed. If you need to use a fully signed certificate, you must
get a PEM file from the Certificate Authority and then import the PEM into the keystore.

#### Configure the Client's Trust Store 

Export the server's certificate to a file by navigating to ``WILDFLY_HOME/standalone/configuration`` and
running the following command:

```shell script
keytool -export -alias localhost -keystore server.keystore -file myCert.cer -storepass secret

```

Import the certificate into the client's trust store. Note the following command will create a trust store
if it does not exist at the location specified.

```shell script
keytool -import -file myCert.cer -alias localhost -keystore client.keystore -storepass secret
```
 
You should see the following output next:


```shell script
Owner: CN=localhost, OU=wildfly, O=jboss, L=Raleigh, ST=North Carolina, C=US
Issuer: CN=localhost, OU=wildfly, O=jboss, L=Raleigh, ST=North Carolina, C=US
Serial number: 7af5ad9991816bbc
Valid from: Wed Nov 04 16:15:09 EST 2020 until: Thu Nov 04 16:15:09 EDT 2021
Certificate fingerprints:
	 SHA1: 24:6E:96:9A:8B:D2:FF:2B:7B:58:87:84:03:F3:ED:C6:56:8B:96:5B
	 SHA256: 27:51:F6:7A:F8:51:7D:6E:CB:DA:4A:9A:75:E5:9A:AD:06:88:1B:AE:40:73:7D:D8:E4:4F:22:CA:8E:03:94:37
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Extensions:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: FF BA 34 E8 94 94 BE EB   25 B5 C5 4E B3 B2 A4 34  ..4.....%..N...4
0010: 97 EE A2 24                                        ...$
]
]

Trust this certificate? [no]: y
Certificate was added to keystore
```

You have now created the client's trustore and imported the server's certificate.

### Configure the Client to use SSLv2Hello Messages

The example application presents a simple client ``Client.java`` which checks the connection
to the server is successful. To ensure the client uses ``SSLv2Hello`` in its initial
handshake, we have provided a common configuration framework in
``wildfly-config.xml``.

Review this file to see how ``SSLv2Hello`` is enabled in the client SSL context along with
``TLSv1``.

As a final step in configuring the client, update the path to the client's trust store to
``WILDFLY_HOME/standalone/configuration/client.keystore``.

NOTE: If you are running a newer JDK version, the client already has code in place to re-enable 
TLSv1. 

#### Build and Deploy the Application

Deploy the application by navigating to the root directory for this example and running 
the following commands:

```
cd server
mvn clean package wildfly:deploy
```

This deploys the ``server-sslv2hello.war`` to the running instance of the server.

You should see a message in the server log indicating that the archive deployed successfully.

### Run Client 

Run client test that shows the connection was successful. 

```
cd ../client
mvn clean install -Dtest=Client
```

### Verifying the Client Sends SSLv2Hello Messages

In the following section, we will be inspecting the SSL debug logs to ensure the Client Hello messages
make use of ``SSLv2Hello``. You will need to enable SSL debug logs in your client and server using
the ``javax.net.debug`` system property as follows:

To run your client with SSL debug logs:
```shell script
mvn clean install -Dtest=Client -Djavax.net.debug=ssl,handshake
```

To run your server with SSL debug logs:
```shell script
WILDFLY_HOME/bin/standalone.sh -Djavax.net.debug=ssl,handshake
```

#### Verifying SSLv2Hello Messages Succeed When SSLv2Hello is Configured on the Server

Run your client.

In your client SSL logs, you should see a ``ClientHello`` message similar to the following:

```shell script
"ClientHello": {
  "client version"      : "TLSv1",
  "random"              : "28 D9 B0 EB 0E A1 5B 07 B6 0D 21 B1 87 F8 42 14 EE 11 6A 11 8B B6 19 7D 2B CF DB B5 B1 A1 43 01",
  "session id"          : "",
  "cipher suites"       : "[TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(0xC00A), TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(0xC014), TLS_RSA_WITH_AES_256_CBC_SHA(0x0035), TLS_DHE_RSA_WITH_AES_256_CBC_SHA(0x0039), TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(0xC009), TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(0xC013), TLS_RSA_WITH_AES_128_CBC_SHA(0x002F), TLS_DHE_RSA_WITH_AES_128_CBC_SHA(0x0033)]",
  "compression methods" : "00",
  "extensions"          : [
    "status_request (5)": {
      "certificate status type": ocsp
      "OCSP status request": {
        "responder_id": <empty>
        "request extensions": {
          <empty>
        }
      }
    },
    "supported_groups (10)": {
      "versions": [secp256r1, secp384r1, secp521r1, ffdhe2048, ffdhe3072, ffdhe4096, ffdhe6144, ffdhe8192]
    },
    "ec_point_formats (11)": {
      "formats": [uncompressed]
    },
    "status_request_v2 (17)": {
      "cert status request": {
        "certificate status type": ocsp_multi
        "OCSP status request": {
          "responder_id": <empty>
          "request extensions": {
            <empty>
          }
        }
      }
    },
    "extended_master_secret (23)": {
      <empty>
    },
    "supported_versions (43)": {
      "versions": [TLSv1, SSLv2Hello]
    },
    "renegotiation_info (65,281)": {
      "renegotiated connection": [<no renegotiated connection>]
    }
  ]
}
```


Notice how ``TLSv1`` and ``SSLv2Hello`` are specified under ``supported_versions``. This triggers
an ``SSLv2Hello ClientHello``. Your tests should succeed, and you should be able to see the
negotiated protocol is ``TLSv1`` in both your client and server logs.

#### Verifying SSLv2Hello messages fail when SSLv2Hello is not configured on the server

You can modify your ``server-ssl-context`` to accept only ``TLSv1`` as follows:

```shell script
/subsystem=elytron/server-ssl-context=qsSSLContext:write-attribute(name=protocols,value=[TLSv1])
```

Now, run your client.
You should be able to see a similar ``ClientHello`` as the one in the section above, but you should also see a
``handshake_failure`` in the log.

Upon reviewing your server log, it should provide more details as to what causes the failure. You should see a
message similar to the following:

```shell script
ERROR [stderr] (default I/O-2) javax.net.ssl|ERROR|01 14|default I/O-2|2020-11-05
14:29:27.677 EST|TransportContext.java:318|Fatal (HANDSHAKE_FAILURE): SSLv2Hello is not enabled (
14:29:27,678 ERROR [stderr] (default I/O-2) "throwable" : {
14:29:27,678 ERROR [stderr] (default I/O-2)   javax.net.ssl.SSLHandshakeException: SSLv2Hello is not enabled
```

Now, you have verified that enabling ``SSLv2Hello`` on the client side triggers an ``SSLv2Hello`` message,
which if not supported by the server, causes a handshake exception.

### Undeploy the Application 

When you are finished testing the application, follow these steps to undeploy the archive:

1. Make sure you start the WildFly server. 
2. Open a terminal and navigate to the root directory of this example. 
3. Type the following commands to undeploy the archive:

```shell script
cd server
mvn wildfly:undeploy
```

### Restore the WildFly Standalone Server Configuration 
You can restore the original server configuration by running the ``restore-configuration.cli``
script provided by navigating to the root directory of this application and running: 

```shell script
WILDFLY_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```
