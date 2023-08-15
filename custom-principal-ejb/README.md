# Using custom principals with EJBs

This example demonstrates how to use custom principals with EJBs. The server uses a custom Elytron
transformer to create the custom principal. When a client is authenticated, the principal is
injected into the EJB using the `SessionContext` class, and it can be retrieved by the client.

This demo is based on the [custom-principal-transformer](../custom-principal-transformer)
and [ejb-security](../ejb-security) examples. For more information on how principal transformers
work, see [this blog post](http://darranl.blogspot.com/2017/07/wildfly-elytron-principal-transformers.html).

## Usage

### Use of WILDFLY_HOME

In the following instructions, replace `WILDFLY_HOME` with the actual path to your WildFly
installation.

## Back up the WildFly Standalone Server Configuration

Before you begin, back up your server configuration file.

1. If it is running, stop the WildFly server.
2. Back up the `WILDFLY_HOME/standalone/configuration/standalone.xml` file.

After you have completed testing this example, you can replace this file to restore the server to
its original configuration.

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

### Build the Demo App

1. Make sure you start the WildFly server as described above.
2. Open a new terminal and navigate to the root directory of this example.
3. Type the following command to build the artifacts. JARs will be exported
   to `custom-principal-ejb/application/target` and `custom-principal-ejb/components/target`.

```shell
    $ mvn clean install
```

### Configure Elytron

#### Deploy custom principal and transformers

Review the `configure-elytron.cli` file in the root of this example directory. This quickstart
includes two Maven modules, and the CLI script performs a number of operations on the `components`
module, which contains our custom principal:

1. The `custom-principal-ejb-components` archive is added as a module to the server. This allows the
   custom components to be used by the server.
2. A filesystem realm is created, and an identity is created for the
   user `customQuickstartUserFinal`.
3. A custom pre-realm-principal-transformer (from the
   class [CustomPreRealmTransformer](./components/src/main/java/org/wildfly/security/examples/CustomPreRealmTransformer.java))
   is added. This transformer converts the internal NamePrincipal into our custom format, and is
   required to use it.
4. Custom post-realm and final principal transformers are added to rename the principal. These classes have similar functionality to the `regex-principal-transformer` resource in Elytron, however the use of a
   custom principal means the existing one can't be used.
5. A security domain is created, referencing the filesystem realm and the pre-realm and post-realm
   transformers.
6. A SASL authentication factory is created, referencing both the security domain and the final
   principal transformer.
7. The EJB subsystem is updated to use the new security domain, and the Remoting subsystem for the
   authentication factory.

In the `configure-elytron.cli` script, replace `/PATH/TO` with the parent directory of
the `elytron-examples`repo. Then, open a new terminal, navigate to the root directory of this
example andrun the following command, replacing `WILDFLY_HOME` with the path to your server.

```shell
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=configure-elytron.cli
```

> NOTE: For Windows, use the `WILDFLY_HOME\bin\jboss-cli.bat` script.

You should see the following result when you run the script:

```shell
    The batch executed successfully
    process-state: reload-required
```

#### Deploy the EJBs

Next, deploy the EJBs, built from the `custom-principal-ejb-application` module, to the server:

1. Open a terminal and navigate to the root directory of this quickstart.
2. Type the following command to deploy the EJB to the server.

```shell
    $ mvn wildfly:deploy
```

This deploys the `custom-principal-ejb/application/target/custom-principal-ejb.jar` to the running
instance of the server. You should see a message in the server log indicating that the archive
deployed successfully.

### Run the Client

Before you run the client, make sure you have successfully deployed both artifacts to the server in
the previous step, and that your terminal is still in the root directory of this example.

Run this command to execute the client:

```shell
    $ mvn exec:exec
```

### Investigate the Console Output

When you run the `mvn exec:exec` command, you should see the following output.

```
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

Successfully called secured bean as unauthenticated user
EJBContext records principal as anonymous

Custom principal usage test:
[Via EJBContext] Last login is not applicable. Secrets access is disabled.

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

Successfully called secured bean as authenticated user
EJBContext records principal as customQuickstartUserPre

Custom principal usage test:
[Via EJBContext] Last login was 10 days ago. Secrets access is enabled.

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

The first block
invokes [PrincipalProviderEJBUnsecured](./application/src/main/java/org/wildfly/security/examples/PrincipalProviderEJBUnsecured.java)
and retrieves the principal using JNDI lookup. This EJB does not require authentication, so calls
are made as an anonymous principal and no permissions can be derived.

The second block
accesses [PrincipalProviderEJBSecured](./application/src/main/java/org/wildfly/security/examples/PrincipalProviderEJBSecured.java),
which requires authentication and logs in the user. The returned principal is the result of the
pre-realm transformer, which converts the principal into
the [CustomPrincipal](./components/src/main/java/org/wildfly/security/examples/CustomPrincipal.java)
class. This class contains additional fields and methods for login times. This principal is then
made available to `SessionContext` (an implementation of `EJBContext`), which is injected into the
EJB.

When the EJB is invoked, the custom principal `customQuickstartUserPre` can be used to perform
class-specific operations. In this example, it retrieves the current and last login date, to
determine if the client can still access secrets.

### Undeploy the App and Restore the WildFly Standalone Server Configuration

You can restore the original server configuration by undeploying the EJB, and running
the `restore-configuration.cli` script provided in the root directory of this example. Perform the
following steps to restore the original configuration:

1. Start the WildFly server.
2. Open a new terminal, navigate to the root directory of this example, and run the following
   commands:

```shell
    $ mvn wildfly:undeploy
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```

> NOTE: For Windows, use the ```WILDFLY_HOME\bin\jboss-cli.bat``` script.

You should see the following output when you run the script:

```shell
    The batch executed successfully
    process-state: reload-required
    The batch executed successfully
    process-state: reload-required
```
