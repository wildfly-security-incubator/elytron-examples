## Configuring Elytron Realms with Encryption

The `encryption-filesystem-realm` quickstart demonstrates how to secure a web application using a filesystem realm with encryption configured.

The purpose of this example is to demonstrate how to configure a filesystem realm to encrypt all data with a SecretKey.

NOTE: This example is building from the [simple-webapp](https://github.com/wildfly-security-incubator/elytron-examples/tree/master/simple-webapp)
quickstart which is made to quickly deploy a simple web application that authenicates users with the `BASIC` mechanism.

### Overview

This quickstart takes the following steps to implement Encryption using Elytron:

1. Create a secret key credential store to generate and access a secret key for configuring encryption.
1. Add a filesystem-based identity store to be used by the Elytron subsystem while specifying the `credential-store` and `secret-key` attributes to enable encryption.
1. Add an identity to the Filesystem Realm and sets a password.
1. Configure the `application-security-domain` mapping in the `undertow` subsystem for the web application.

### Use of the WILDFLY_HOME

In the following instructions, replace `WILDFLY_HOME` with the actual path to your WildFly installation.

### Back up the WildFly Standalone Server Configuration

Before you begin, back up your server configuration file.

1. If it is running, stop the WildFly server.
2. Back up the `WILDFLY_HOME/standalone/configuration/standalone.xml` file.

After you have completed testing this example, you can replace this file to restore the server to its original configuration.

### Configuring Elytron

First, clone the `elytron-examples` repo locally:

```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples
```

Next, start the server:

```
    $ WILDFLY_HOME/bin/standalone.sh
```

Review the `configure-elytron.cli` file in the root of this quickstart directory. This script
adds the configuration that enables Elytron security for the quickstart components. Comments in the script

Then, open a new terminal, navigate to the root directory of this quickstart and run the following command, replacing `WILDFLY_HOME`
with the path to your server.

```
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=configure-elytron.cli
```

NOTE: For Windows, use the `WILDFLY_HOME\bin\jboss-cli.bat` script.

You should see the following result when you run the script:

```$xslt
    The batch executed successfully
    process-state: reload-required
```

### Build and Deploy the Quickstart

1. Make sure you start the WildFly server as described above.
2. Open a terminal and navigate to the root directory of this quickstart.
3. Type the following command to build the artifacts.

```$xslt
    $ mvn clean install wildfly:deploy
```

This deploys the `{artifactId}/target/{artifactId}.war` to the running instance of the server.

You should see a message in the server log indicating that the archive deployed successfully.

### Log into the Web App

You can go to `http://localhost:8080/encryption-filesystem` to access the web application now. Selecting the `Access Secured Servlet` link will prompt you for a username and password to log in. If you correctly login with the credentials created in the setup (username: `quickstartUser`, password: `password123!`), you will be greeted by a page that shows you the principal you're logged in with. The successful login indicates that encryption has been configured correctly.

### Investigate the Filesystem Identity

In order to further verify that the encryption features are being used correctly we can navigate to the identity file and check the contents. The file should be located at `WILDFLY_HOME/standalone/configuration/fs-realm/O/F/OF2WSY3LON2GC4TUKVZWK4Q.xml` if the same filesystem realm and identity configuration was used.

Here we can see the format for the password is `enc_base64` specifying that the credentials are encrypted. The attributes should also be stored encrypted instead of plain text.

### Undeploy the Quickstart

1. Make sure you start the WildFly server.
2. Open a terminal to the root directory of this quickstart.
3. Type this command to undeploy the archive:

```$xslt
    $ mvn wildfly:undeploy
```

### Restore the WildFly Standalone Server Configuration

You can restore the original server configuration by either:

1. Running the restore-configuration.cli script provided in the root directory of this quickstart.
2. Manually restoring the configuration using the backup copy of the configuration file.

##### Restore the Wildfly Standalone Server Configuration by Running the JBoss CLI Script

1. Start the WildFly server.
2. Open a new terminal, navigate to the root directory of this quickstart, and run the following command:

```$xslt
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```

NOTE: For Windows, use the `WILDFLY_HOME\bin\jboss-cli.bat` script.

##### Restore the WildFly Standalone Server Configuration Manually

When you have completed testing the quickstart, you can restore the original server configuration by manually restoring the configuration file with your backup copy.

1. If it is running, stop the WildFly server.

2. Replace the `WILDFLY_HOME/standalone/configuration/standalone.xml` file with the backup copy of the file.
