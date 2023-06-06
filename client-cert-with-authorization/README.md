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
   client1.keystore.pkcs12
   client1.truststore.pkcs12
   client2.keystore.pkcs12
   client2.truststore.pkcs12
   server.keystore.pkcs12
   server.truststore.pkcs12
```

* ```client1.keystore.pkcs12``` contains a certificate with distinguished name: ```CN=Bob.Smith.123456```
* ```client2.keystore.pkcs12``` contains a certificate with distinguished name: ```CN=Alice.Smith.456789```
* Both client certificates are issued by an example certificate authority with distinguished name: ```CN=Elytron CA, ST=Elytron, C=UK, EMAILADDRESS=elytron@wildfly.org, O=Root Certificate Authority```
* ```server.truststore.pkcs12``` contains this certificate authority's certificate

Finally, copy the server.keystore.pkcs12 and server.truststore.pkcs12 files to your WildFly server instance:

```
cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/server.* $WILDFLY_HOME/standalone/configuration
cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/client* $WILDFLY_HOME/standalone/configuration
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

### Import the Certificate into Your Browser

Before you access the application, you must import the _client1.keystore.pkcs12_, which holds the client certificate, into your browser.

### Import the Certificate into Google Chrome

 - Click the Chrome menu icon (3 dots) in the upper right on the browser toolbar and choose *Settings*. This takes you to link:`chrome://settings/`.

- Click on *Privacy and security* and then on *Security*.

- Scroll down to the *Advanced* section and on the *Manage certificates* screen, select the *Your Certificates* tab and click on the *Import* button.

- Select the *client1.keystore.pkcs12* file. You will be prompted to enter the password: `keystorepass`.

- The client certificate is now installed in the Google Chrome browser.

You can follow the same instructions to import `client2.keystore.pkcs12` into your browser.

### Import the Certificate into Mozilla Firefox

- Click the *Edit* menu item on the browser menu and choose *Settings*.

- A new window will open. Click on *Privacy & Security* and scroll down to the *Certificates* section.

- Click the *View Certificates* button.

- A new window will open. Select the *Your Certificates* tab and click the *Import* button.

- Select the *client1.keystore.pkcs12* file. You will be prompted to enter the password: `keystorepass`.

- The certificate is now installed in the Mozilla Firefox browser.

You can follow the same instructions to import `client2.keystore.pkcs12` into your browser. Now try accessing the application using https://localhost:8443/simple-webapp

Note that since the server's certificate won't be trusted by your browser, you'll need to manually confirm that this certificate is trusted or configure your browser to trust it.

First, select the certificate with for Alice.Smith.456789. Then try clicking on “Access Secured Servlet”. Notice that a Forbidden message occurs. This is because accessing the secured servlet requires “Users” role but the “456789” identity
that we configured has no roles.

Now, try accessing the application again. This time, select the certificate for Bob.Smith.123456 and then click on
“Access Secured Servlet”. This time, this succeeds since the “123456” identity that we configured has “Users” role.

Once you are ready to restore your server back to what it was, please enter the following on your terminal: 
```
$SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/client-cert-with-authorization/restore-elytron-configuration.cli
```
 
This example has shown to secure a web application deployed to WildFly using the CLIENT_CERT HTTP authentication
mechanism with two-way SSL with authorization. It has also demonstrated that individual client certificates do not need
to be stored in either the server’s truststore or in its security realm.

