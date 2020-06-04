## ejb-security:  Configuring filesystem realms with SASL authentication and credential stores

The `ejb-security` quickstart demonstrates how to secure EJBs using a filesystem realm
with SCRAM-SHA-512 SASL authentication along with a credential store on the client side.

Note: The purpose of this example is to demonstrate more advanced security configurations using
the Elytron subsystem. If you haven't already, take a look [here](https://github.com/wildfly/quickstart/tree/master/ejb-security)
 to learn how to use
Jakarta EE declarative security access to control EJB access.

### Overview

This quickstart takes the following steps to implement EJB security using Elytron:

1. Add a filesystem-based identity store to be used by the Elytron subsystem.
2. Update the sasl-authentication-factory used by the Elytron subsystem to also offer SCRAM-SHA-512 as one of its authentication mechanisms.
3. Add an `application-security-domain` mapping in the `ejb3` subsystem to enable Elytron security for the `SecuredEJB`.
4. Configure a credential store on the client side to be used for the authentication configuration

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
describe the purpose of each command. 

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

### Configure the Credential Store
Create the credential store locally as follows:
```$xslt
    $ WILDFLY_HOME/bin/elytron-tool.sh credential-store --create --location "/PATH/TO/mycredstore.cs" --password StorePassword
```

Add the alias ```quickstartUser``` to be used for the authentication configuration: 

```$xslt
    $ WILDFLY_HOME/bin/elytron-tool.sh credential-store --location "/PATH/TO/mycredstore.cs" --password StorePassword --add quickstartUser --secret quickstartPwd1!
``` 

In ```wildfly-config.xml```, make sure to change the path to your credential store i.e. replace
```/PATH/TO/``` with the path to your credential store. 

```$xslt
    <attribute name="location" value="/PATH/TO/mycredstore.cs"></attribute>
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

Principal has admin permission: false

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

The username and credentials to establish the connection to the application server are configured in the ```wildfly-config.xml``` file. As
expected, the ``quickstartUser`` was able to invoke the method available for the ```guest``` role but not for the ```admin``` role. 

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