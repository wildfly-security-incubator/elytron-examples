# ejb-mutual-tls: Accessing Jakarta Enterprise Beans with mutual TLS

The `ejb-mutual-tls` example demonstrates how to securely connect to EJBs deployed to WildFly using mutual TLS
authentication, and control access with the SCRAM-SHA-512-PLUS SASL authentication mechanism.

Note: The purpose of this example is to demonstrate more advanced security configurations using the
Elytron subsystem. If you haven't already, take a look at the following examples:
- [How to connect to an EJB using HTTP and basic authentication](https://github.com/wildfly-security-incubator/elytron-examples/tree/main/ejb-http)
- [How to connect to an EJB using SASL authentication](https://github.com/wildfly-security-incubator/elytron-examples/tree/main/ejb-security)

### Overview

This example takes the following steps to implement EJB security using Elytron:

1. **Generate certificates for mutual TLS authentication**
   1. Generate a public-private key pair on both the client and server side, and store each pair in a key store.
   2. Export self-signed certificates from both sides, and import opposite certificates into a trust store.
   3. Configure the `https-listener` to use the key store and trust store when performing a TLS handshake.
2. **Add an identity for accessing the EJB**
   1. Create a filesystem-based identity store to be used by the Elytron subsystem.
   2. Add roles to an identity to restrict access to EJBs
   3. Add an `tlsApp` security domain mapping in the `ejb3` subsystem to enable Elytron security for the `SecuredEJB`.
3. **Configure an additional SASL authentication mechanism**
   1. Create a `sasl-authentication-factory` used by the Elytron subsystem to offer SCRAM-SHA-512-PLUS as an authentication mechanism.
4. **Secure the EJB using TLS and SASL**
   1. Configure a `http-connector` to use both the `https-listener` and `sasl-authentication-mechanism`
   2. Add the `http-connector` to the `ejb3` subsystem to enable secure connections.
5. **Configure a client credential store**
   1. Create a credential store to be used during SASL authentication
   2. Add an identity to the credential store

### Use of WILDFLY_HOME

In the following instructions, replace `WILDFLY_HOME` with the actual path to your WildFly installation.

### Back up the WildFly Standalone Server Configuration

Before you begin, back up your server configuration file.

1. If it is running, stop the WildFly server.
2. Back up the `WILDFLY_HOME/standalone/configuration/standalone.xml` file.

After you have completed testing this example, you can replace this file to restore the server to its original configuration.

### Start the WildFly Server

First, clone the `elytron-examples` repo locally:
```shell
    $ git clone https://github.com/wildfly-security-incubator/elytron-examples
    $ cd elytron-examples
```
Then, start the server:

```shell
    $ WILDFLY_HOME/bin/standalone.sh
```

### Generate the Client Key Pair and Certificate

Open a new terminal, and navigate to the root directory of this example. Generate the client's key pair, and store it locally in a key store as follows:
```shell
    $ keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -validity 365 -keystore /PATH/TO/tlsClient.keystore -dname "CN=client" -storepass clientKeySecret
```

Export the self-signed certificate and save it locally to a certificate file:
```shell
    $ keytool -exportcert -keystore /PATH/TO/tlsClient.keystore -alias client -storepass clientKeySecret -file /PATH/TO/tlsClient.cer
```

_\(Note: self-signed certificates are not suitable for production environments, and are treated as insecure by
most programs. To obtain a certificate signed by a Certificate Authority, take a look at this [blog post](https://wildfly-security.github.io/wildfly-elytron/blog/obtaining-certificates-from-lets-encrypt-using-the-wildfly-cli/).\)_

### Configure Elytron

Review the `configure-elytron.cli` file in the root of this example directory. This script
adds the configuration that enables Elytron security for the app components. Comments in the script
describe the purpose of each command.

In the section titled `## Server KeyStore and TrustStore ##`, the server generates its own key pair and
certificate, and then imports the client certificate. On the command where the client certificate is
imported into the server's trust store, change `/PATH/TO/` to point to the client's exported certificate, called `tlsClient.cer`:
```
    # Import the client certificate into the trust store. Since the certificate is self-signed, it will not be validated
    /subsystem=elytron/key-store=tlsTrustStore:import-certificate(alias=client,path=/PATH/TO/tlsClient.cer,credential-reference={clear-text=serverTrustSecret},trust-cacerts=true,validate=false)
```

Then, open a new terminal, navigate to the root directory of this example and run the following command,
replacing `WILDFLY_HOME` with the path to your server.

```shell
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=configure-elytron.cli
```

NOTE: For Windows, use the WILDFLY_HOME\bin\jboss-cli.bat` script.

You should see the following result when you run the script:
```shell
    The batch executed successfully
    The batch executed successfully
    The batch executed successfully
    The batch executed successfully
    process-state: reload-required
```

### Import the Server Certificate and Client TLS Configuration

Import the server certificate into the client's trust store, and save the store locally by running the
following command, replacing `WILDFLY_HOME` with the path to your server:
```shell
    $ keytool -importcert -keystore /PATH/TO/tlsClient.truststore -storepass clientTrustSecret -alias localhost -trustcacerts -file /WILDFLY_HOME/standalone/configuration/tlsServer.cer -noprompt
```

In `wildfly-config.xml`, make sure to change the path to your key and trust stores (i.e. replace `/PATH/TO/` with
the path to the key store, and the trust store):
```xml
    <key-stores>
        <key-store name="tlsClientTrustStore" type="PKCS12">
            <file name="/PATH/TO/tlsClient.truststore"/>
            <key-store-clear-password password="clientTrustSecret"/>
        </key-store>
        <key-store name="tlsClientKeyStore" type="PKCS12">
            <file name="/PATH/TO/tlsClient.keystore"/>
            <key-store-clear-password password="clientKeySecret"/>
        </key-store>
    </key-stores>
```

### Configure the Credential Store

Open a new terminal, and navigate to the root directory of this example. Create the credential
store locally as follows:
```shell
    $ WILDFLY_HOME/bin/elytron-tool.sh credential-store --create --location "/PATH/TO/tlsCredStore.cs" --password clientStorePassword
```

Add the alias `example_user` to be used for the authentication:
```shell
    $ WILDFLY_HOME/bin/elytron-tool.sh credential-store --location "/PATH/TO/tlsCredStore.cs" --password clientStorePassword --add example_user --secret examplePwd1!
```

In `wildfly-config.xml`, make sure to change the path to your credential store (i.e. replace `/PATH/TO/` with
the path to your credential store):

```xml
    <attributes>
        <attribute name="keyStoreType" value="JCEKS"/>
        <attribute name="location" value="/PATH/TO/tlsCredStore.cs"/>
    </attributes>
```

### Build and Deploy the Demo App
1. Make sure you start the WildFly server as described above.
2. Open a new terminal and navigate to the root directory of this example.
3. Type the following command to build the artifacts.
```shell
    $ mvn clean install wildfly:deploy
```

This deploys the `ejb-mutual-tls/target/ejb-mutual-tls.jar` to the running instance of the server.

You should see a message in the server log indicating that the archive deployed successfully.

### Run the Client
Before you run the client, make sure you have successfully deployed the EJBs to the server in
the previous step and that your terminal is still in the root directory of this example.

Run this command to execute the client:
```shell
    $ mvn exec:exec
```

### Investigate the Console Output

When you run the `mvn exec:exec` command, you should see the following output.

```shell
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

Successfully called secured bean, caller principal example_user

Principal has admin permission: false

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

The server and client were able to verify each other's certificates as matching their trust
stores, as configured in the client-side `wildfly-config.xml` file and the server-side `configure-elytron.cli`
script. Additionally, the username and credentials stored in the credential store were used to log
into the application server, as configured in `wildfly-config.xml`. As expected, the ``example_user``
was able to invoke the method available for the ```guest``` role but not for the ```admin``` role.

### Undeploy the Demo App

1. Make sure you start the WildFly server.
2. Open a terminal to the root directory of this example.
3. Type this command to undeploy the archive:
```bash
    $ mvn wildfly:undeploy
```

### Restore the WildFly Standalone Server Configuration

You can restore the original server configuration by either:
1. Manually restoring the configuration using the backup copy of the configuration file.
2. Modifying and running the restore-configuration.cli script provided in the root directory of this example.

##### Restore the Wildfly Standalone Server Configuration by Running the JBoss CLI Script

Perform the following steps to restore the original configuration:

1. Start the WildFly server.
2. Open a new terminal, navigate to the root directory of this example, and run the following command:
```shell
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```

NOTE: For Windows, use the ```WILDFLY_HOME\bin\jboss-cli.bat``` script.

You should see the following output when you run the script:

```shell
    The batch executed successfully
    process-state: reload-required 
    The batch executed successfully
    process-state: reload-required 
    The batch executed successfully
    process-state: reload-required 
    The batch executed successfully
    process-state: reload-required 
```

##### Restore the WildFly Standalone Server Configuration Manually
You can also restore the original server configuration
by manually restoring the configuration file with your backup copy.

1. If it is running, stop the WildFly server.

2. Replace the ```WILDFLY_HOME/standalone/configuration/standalone.xml``` file with the backup copy of the file.
