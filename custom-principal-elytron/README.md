# Using custom principals with Elytron

This example demonstrates how to use custom principals in Elytron. The
examples [custom-principal-ee](../custom-principal-ee)
and [custom-principal-ejb](../custom-principal-ejb) demonstrate standard use cases, where the
principal can be retrieved from `SecurityContext` or `SessionContext` respectively. However, it is
also possible to get a custom principal from `SecurityContext` without a full Jakarta Security
implementation, which is explored in this example.

The server uses a custom Elytron transformer to create the custom principal. When a client is
authenticated, the principal is injected into the Servlet using the Jakarta `SecurityContext` class
and the Elytron `SecurityDomain` class. The client can then retrieve the principal using either of
these classes.

This demo is based on the [custom-principal-transformer](../custom-principal-transformer)
and [simple-webapp](../simple-webapp) examples. For more information on how principal transformers
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
   to `custom-principal-elytron/application/target`
   and `custom-principal-elytron/components/target`.

```shell
    $ mvn clean install
```

### Configure Elytron

#### Deploy custom principal and transformers

Review the `configure-elytron.cli` file in the root of this example directory. This quickstart
includes two Maven modules, and the CLI script performs a number of operations on the `components`
module, which contains our custom principal:

#### Deploy custom principal and transformers

Review the `configure-elytron.cli` file in the root of this example directory. This quickstart
includes two Maven modules, and the CLI script performs a number of operations on the `components`
module, which contains our custom principal:

1. The `custom-principal-elytron-components` archive is added as a module to the server. This allows
   the custom components to be used by the server.
2. A filesystem realm is created, and an identity is created for the
   user `customQuickstartUserPost`.
3. A custom pre-realm-principal-transformer (from the
   class [CustomPreRealmTransformer](./components/src/main/java/org/wildfly/security/examples/CustomPreRealmTransformer.java))
   is added. This transformer converts the internal NamePrincipal into our custom format, and is
   required to use it.
4. A custom post-realm transformer is added to rename the principal. This class has similar functionality to the `regex-principal-transformer` resource in Elytron, however the use of a
   custom principal means the existing one can't be used.
5. A security domain is created, referencing the filesystem realm and the pre-realm and post-realm
   transformers.
6. The `application-security-domain` mapping in the Undertow subsystem is updated to reference this
   security domain.

In the `configure-elytron.cli` script, replace `/PATH/TO` with the parent directory of
the `elytron-examples` repo. Then, open a new terminal, navigate to the root directory of this
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

#### Deploy the Web App

Next, deploy the web app, built from the `custom-principal-elytron-application` module, to the
server:

1. Open a terminal and navigate to the root directory of this quickstart.
2. Type the following command to deploy the web app to the server.

```shell
    $ mvn wildfly:deploy
```

This deploys the `custom-principal-elytron/application/target/custom-principal-elytron.war` to the
running instance of the server. You should see a message in the server log indicating that the
archive deployed successfully.

### Log into the Web App

You can go to `http://localhost:8080/custom-principal-elytron` to access the web application now.
Selecting the `Access Secured Servlet` link will prompt you for a username and password to log in.
If you correctly login with the credentials created in the setup (username: `quickstartUser`,
password: `password123!`), you will be greeted by a page that shows you the principal you're logged
in with, as well as how the principal was handled internally. The raw HTML is shown below:

```html
<!DOCTYPE html>
<html lang="en">
   <head><title>SecuredServlet - doGet()</title></head>
   <body>
      <h1>Custom Principal - Elytron Demo</h1>
      <p>For reference, transform sequence is quickstartUser -> <em>customQuickstartUserPre</em> -> customQuickstartUserPost.</p>
      <p>Injection check - these values should match:</p>
      <ul>
          <li>Identity as available from Jakarta Security (<code>SecurityContext</code>): <strong>customQuickstartUserPre</strong></li>
          <li>Identity as available from injection (<code>SecurityIdentity</code>): <strong>customQuickstartUserPre</strong></li>
          <li>Identity as provided in HTTPServletRequest: <strong>customQuickstartUserPre</strong></li>
      </ul>
      <p>Custom Principal check - these values should match:</p>
      <ul>
          <li>Caller principal<code>(SecurityContext.getCallerPrincipal)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUserPre</strong></li>
          <li>Custom principal<code>(SecurityContext.getPrincipalsByType)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUserPre</strong></li>
          <li>Injection<code>(SecurityIdentity.getPrincipal)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUserPre</strong></li>
      </ul>
      <p>Custom Principal usage - this check will return a result of <em>enabled</em> if the
          CustomPrincipal was injected properly:</p>
      <ul>
          <li>SecurityContext reports last login <strong>was 10 days ago</strong>. Secrets access is <strong>enabled</strong>.</li>
          <li>SecurityIdentity reports last login <strong>was 10 days ago</strong>. Secrets access is<strong>enabled</strong>.</li>
   </body>
</html>
```

### Understanding the Results

When the client logs into the page, Elytron's authentication checks to see if the principal is
present in the security realm. During authentication, the pre-realm transformer first converts the
principal into
the [CustomPrincipal](./components/src/main/java/org/wildfly/security/examples/CustomPrincipal.java)
class. This class contains additional fields and methods for login times. The transformer also
renames the principal to customQuickstartUserPre.

When a principal is requested from
the [SecuredServlet](./application/src/main/java/org/wildfly/security/examples/SecuredServlet.java),
the custom principal can be returned. In this example, `customQuickstartUserPre` is used to retrieve
the current and last login date. The servlet then uses this info to report if the user has access to
secrets.

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
