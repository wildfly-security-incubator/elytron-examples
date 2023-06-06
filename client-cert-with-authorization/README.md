## Using certificate-based authentication with authorization

This example shows how to secure a web application deployed to WildFly using the CLIENT_CERT HTTP authentication
mechanism with two-way SSL and authorization. The server’s truststore will only contain an example CA certificate,
it won’t contain any client certificates. Similarly, the security realm used for authorization in this example
won’t contain any client certificates. It will contain principals that are derived from a portion of the CN value
from the subject name from a client’s X.509 certificate.

### Overview

* [ Certificate generation ](#certGeneration)
* [ Server configuration ](#serverConfiguration)
* [ Deploying and accessing the application ](#deployingApp)

<a name="certGeneration"></a>
### Certificate generation

First, clone the ```elytron-examples``` repo locally:

```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples
```

Next, let's generate some client and server certificates that will be used in this example to set up two-way SSL between
a server and two clients:

```
   cd dynamic-certificates
   mvn clean install exec:java -Dexec.mainClass="org.wildfly.security.examples.CertificateGenerationExample" -Dexec.args="CN=Bob.Smith.123456 CN=Alice.Smith.456789"
```

The above command generates the following keystores and truststores in the ```dynamic-certificates/target``` directory:

```
   client1.keystore
   client1.truststore
   client2.keystore
   client2.truststore
   server.keystore
   server.truststore
```

* ```client1.keystore``` contains a certificate with distinguished name: ```CN=Bob.Smith.123456```
* ```client2.keystore``` contains a certificate with distinguished name: ```CN=Alice.Smith.456789```
* Both client certificates are issued by an example certificate authority with distinguished name: ```CN=Elytron CA, ST=Elytron, C=UK, EMAILADDRESS=elytron@wildfly.org, O=Root Certificate Authority```
* ```server.truststore``` contains this certificate authority's certificate

Next, convert the client keystores into PKCS12 format and import them into your browser so you can pick which one to
present to the server later on:

```
keytool -importkeystore -srckeystore client1.keystore -srcstoretype jks -destkeystore client1.keystore.pkcs12 -deststoretype pkcs12 -srcstorepass keystorepass -deststorepass keystorepass
keytool -importkeystore -srckeystore client2.keystore -srcstoretype jks -destkeystore client2.keystore.pkcs12 -deststoretype pkcs12 -srcstorepass keystorepass -deststorepass keystorepass
```

Finally, copy the server.keystore and server.truststore files to your WildFly server instance:

```
cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/server.* $WILDFLY_HOME/standalone/configuration
```

<a name="serverConfiguration"></a>
### Server configuration

The following command can now be used configure WildFly to secure our web application using the CLIENT_CERT HTTP
authentication mechanism with two-way SSL and authorization. Only the client with ID 123456 will be able to access
our secured web application
 
```
    $SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/client-cert-with-authorization/configure-elytron-server.cli 
```

Take a look at the ```configure-elytron-server.cli``` file in the ```client-cert-with-authorization``` directory for more
details on the configuration.

<a name="deployingApp"></a>
### Deploying and accessing the application

We’re going to make use of the ```simple-webapp``` project. It can be deployed using the following commands:

```
cd $PATH_TO_ELYTRON_EXAMPLES/simple-webapp
mvn clean install wildfly:deploy
```

Then try accessing the application using https://localhost:8443/simple-webapp

Note that since the server's certificate won't be trusted by your browser, you'll need to manually confirm that
this certificate is trusted or configure your browser to trust it.

First, select the certificate with for Alice.Smith.456789. Then try clicking on “Access Secured Servlet”. Notice that a
Forbidden message occurs. This is because accessing the secured servlet requires “Users” role but the “456789” identity
that we configured has no roles.

Now, try accessing the application again. This time, select the certificate for Bob.Smith.123456 and then click on
“Access Secured Servlet”. This time, this succeeds since the “123456” identity that we configured has “Users” role.

Once you are ready to restore your server back to what it was, please enter the following on you terminal: 
```
$SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/client-cert-with-authorization/restore-elytron-configuration.cli
```

This example has shown to secure a web application deployed to WildFly using the CLIENT_CERT HTTP authentication
mechanism with two-way SSL with authorization. It has also demonstrated that individual client certificates do not need
to be stored in either the server’s truststore or in its security realm.

