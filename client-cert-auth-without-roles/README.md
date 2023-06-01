## Using Certificate Based Authentication Without Roles In A Mutual TLS-Connection

  
  

This example will walk you through how to establish a secure connection via mutual TLS connection on a web application deployed to WILDFLY. We will be doing so using CLIENT_CERT HTTP authentication.

  

CLIENT_CERT authentication, is a method where both the client and server authenticate each other using their digital certificates in a TLS connection. During the TLS handshake, the client and server exchange and validate certificates, ensuring the identity of both parties. In this example we will be using certificates signed by an example CA. We will be making use of the `Dynamic-certificate` repository.

  

### Overview

  

* [ Certificate generation ](#certGeneration)

* [ Server configuration ](#serverConfiguration)

* [ Deploying and accessing the application ](#deployingApp)

  

<a  name="certGeneration"></a>

### Certification Generation

  

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

*  ```client1.keystore``` contains a certificate with distinguished name: ```CN=Bob.Smith.123456```

*  ```client2.keystore``` contains a certificate with distinguished name: ```CN=Alice.Smith.456789```

* Both client certificates are issued by an example certificate authority with distinguished name: ```CN=Elytron CA, ST=Elytron, C=UK, EMAILADDRESS=elytron@wildfly.org, O=Root Certificate Authority```

*  ```server.truststore``` contains this certificate authority's certificate

  

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

<a  name="serverConfiguration"></a>

### Server Configuration

  

The following set of instructions are going to be used to configure the wildfly server in order to establish the mutual TLS connection. We will be deploying a simple web application under the `elytron-examples/client-cert-auth-without-roles/simple-webapp` folder.

  

Please navigate to the elytron server home directory and enter the following command.

  

```

$SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/client-cert-auth-without-roles/configure-elytron-server.cli

```

  

Take a look at the ```configure-elytron-server.cli``` file in the ```client-cert-auth-without-roles``` directory for more

details on the configuration.

  

<a  name="deployingApp"></a>

### Deploying and accessing the application

  

We’re going to make use of the ```simple-webapp``` project. It can be deployed using the following commands:

  

```

cd $PATH_TO_ELYTRON_EXAMPLES/client-cert-auth-without-roles/simple-webapp

mvn clean install wildfly:deploy

```

  

<b>Note: </b>there is also a repository under ```elytron-examples``` which contains a `simple-webapp` folder. But we will not be using that. Instead, we will be deploying a slightly modified version of that web application located inside this repository.

  

### Import the Certificate into Your Browser

  

Before you access the application, you must import the _clientCert.p12_, which holds the client certificate, into your browser.

  

### Import the Certificate into Google Chrome

  

 - Click the Chrome menu icon (3 dots) in the upper right on the browser toolbar and choose *Settings*. This takes you to `link:`chrome://settings/`.

- Click on *Privacy and security* and then on *Security*.

- Scroll down to the *Advanced* section and on the *Manage certificates* screen, select the *Your Certificates* tab and click on the *Import* button.

- Select the *client1.keystore.pkcs12* file. You will be prompted to enter the password: `keystorepass`.

- The client certificate is now installed in the Google Chrome browser.



### Import the Certificate into Mozilla Firefox

  

- Click the *Edit* menu item on the browser menu and choose *Settings*.

- A new window will open. Click on *Privacy & Security* and scroll down to the *Certificates* section.

- Click the *View Certificates* button.

- A new window will open. Select the *Your Certificates* tab and click the *Import* button.

- Select the *client1.keystore.pkcs12* file. You will be prompted to enter the password: `keystorepass`.

- The certificate is now installed in the Mozilla Firefox browser.


Now try accessing the application using https://localhost:8443/simple-webapp

Note that since the server's certificate won't be trusted by your browser, you'll need to manually confirm that
this certificate is trusted or configure your browser to trust it.

Select the certificate for Bob.Smith.123456 and then click on
“Access Secured Servlet”. This will authenticate the client and will recognize that Bob accessed the web application and print "Login Successful!". 

Try closing the web app and open it in a new tab. This time select Alice's certificate. Since the server is not authenticated with Alice's CLIENT_CERT, the applciation will not recognize her and will print "Login Unsuccessful.". 
If you are seeing this message, please ensure that you have gone through all the steps outlines in this example and retry. 

Once you are ready to restore your server back to what it was, please enter the following on you terminal: 

```

$SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/client-cert-auth-without-roles/restore-elytron-configuration.cli

```

This example has shown to secure a web application deployed to WildFly using the CLIENT_CERT HTTP authentication
mechanism with two-way SSL with authorization. It has also demonstrated that individual client certificates do not need
to be stored in either the server’s truststore or in its security realm.