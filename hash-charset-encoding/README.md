## Configuring Elytron Realms with Hash Charsets and Hash Encoding

The `hash-charset-encoding` quickstart demonstrates how to secure EJBs using a filesystem realm with a hash charset and
hash encoding configured.

The purpose of this example is to demonstrate how to configure a hash charset to use when converting the client provided 
password string to a byte array for hashing calculations. Additionally, the quickstart demonstrates how to specify the 
string format for the hashed password if the password is not being stored in plain text in our realm.

Although the example only demonstrates how to configure these 
attributes in the filesystem realm, all realms that support storing hashed passwords can be configured 
similarly. Namely, the Legacy Properties Realm, JDBC Realm and LDAP Realm can be configured using the same 
attributes. 

NOTE:  This example is building from the [ejb-security](https://github.com/wildfly-security-incubator/elytron-examples/tree/master/ejb-security)
            quickstart which more closely explains how to secure EJBs. The focus of this example is the ``hash-encoding`` 
            and ``hash-charset`` attributes in the Elytron Security Realms.

### Overview

This quickstart takes the following steps to implement EJB security using Elytron:

1. Add a filesystem-based identity store to be used by the Elytron subsystem configuring ``KOI8-R`` as the ``hash-charset`` and
``hex`` encoding as the string encoding.
2. Add an identity to the Filesystem Realm which sets a password using the ``KOI8-R`` character set.
3. Update the sasl-authentication-factory used by the Elytron subsystem to also offer ``PLAIN`` as one of its authentication mechanisms.
This is the authentication mechanism we will be using. 
4. Add an `application-security-domain` mapping in the `ejb3` subsystem to enable Elytron security for the `SecuredEJB`.

### Use of the WILDFLY_HOME

In the following instructions, replace ```WILDFLY_HOME``` with the actual path to your WildFly installation. 

### Back up the WildFly Standalone Server Configuration 

Before you begin, back up your server configuration file. 

1. If it is running, stop the WildFly server.
2. Back up the ```WILDFLY_HOME/standalone/configuration/standalone.xml``` file.

After you have completed testing this example, you can replace this file to restore the server to its original configuration. 


### Configuring Elytron

First, clone the ```elytron-examples``` repo locally:
```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples
```
Next, start the server:

```
    $ WILDFLY_HOME/bin/standalone.sh
```

Review the ```configure-elytron.cli``` file in the root of this quickstart directory. This script 
adds the configuration that enables Elytron security for the quickstart components. Comments in the script 

Then, open a new terminal, navigate to the root directory of this quickstart and run the following command, replacing ```WILDFLY_HOME```
with the path to your server. 

```
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=configure-elytron.cli
```

NOTE: For Windows, use the ```WILDFLY_HOME\bin\jboss-cli.bat``` script.

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

This deploys the ```{artifactId}/target/{artifactId}.jar``` to the running instance of the server.

You should see a message in the server log indicating that the archive deployed successfully. 

### Run the Client 
Before you run the client, make sure you have successfully deployed the EJBs to the server in the previous step and that your 
terminal is still in the root directory of this quickstart. 

Run this command to execute the client: 
```$xslt
    $ mvn exec:exec
```

### Investigate the Console Output
When you run the ```mvn exec:exec``` command, you should see the following output. 

```$xslt
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

Successfully called secured bean, caller principal quickstartUser

Principal has admin permission: true

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

The username and credentials to establish the connection to the application server are configured in the ```wildfly-config.xml``` file. As
expected, the ``quickstartUser`` was able to invoke the method available for the ```Guest``` role and the ```admin``` role. 

###  Undeploy the Quickstart 
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

NOTE: For Windows, use the ```WILDFLY_HOME\bin\jboss-cli.bat``` script.

##### Restore the WildFly Standalone Server Configuration Manually 
When you have completed testing the quickstart, you can restore the original server configuration by manually restoring the configuration file with your backup copy.

1. If it is running, stop the WildFly server.

2. Replace the ```WILDFLY_HOME/standalone/configuration/standalone.xml``` file with the backup copy of the file.