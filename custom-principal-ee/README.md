# Using custom principals with Jakarta Security

This example demonstrates how to use custom principals with Jakarta Security. The server uses a
custom Elytron transformer to create the custom principal. When a client is authenticated, the
principal is injected into the Servlet using the Jakarta `SecurityContext` class and the
Elytron `SecurityDomain` class. The client can then retrieve the principal using either of these
classes.

This demo is based on the [custom-principal-transformer](../custom-principal-transformer) example,
and the [WildFly ee-security](https://github.com/wildfly/quickstart/tree/main/ee-security)
quickstart. For more information on how principal transformers work,
see [this blog post](http://darranl.blogspot.com/2017/07/wildfly-elytron-principal-transformers.html).

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
3. Type the following command to build the artifacts. A WAR will be exported
   to `custom-principal-ee/application/target`, and a JAR will be exported
   to  `custom-principal-ee/components/target`.

```shell
$ mvn clean install
```

### Configure Elytron

#### Deploy custom principal and transformers

Review the `configure-elytron.cli` file in the root of this example directory. This quickstart
includes two Maven modules, and the CLI script performs a number of operations on the `components`
module, which contains our custom principal:

1. The `custom-principal-ee-components` archive is added as a module to the server. This allows the
   custom components to be used by the server.
2. A new security domain is created, and the `application-security-domain` mapping in the Undertow
   subsystem is updated to reference this security domain.
3. The `integrated-jaspi` attribute for the `application-security-domain` is disabled, to allow
   ad-hoc identities to be created dynamically.

In the `configure-elytron.cli` script, replace `/PATH/TO` with the parent directory of
the `elytron-examples` repo. Then, open a new terminal, navigate to the root directory of this
example and run the following command, replacing `WILDFLY_HOME` with the path to your server.

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

Next, deploy the web app, built from the `custom-principal-ee-application` module, to the server:

1. Open a terminal and navigate to the root directory of this quickstart.
2. Type the following command to deploy the web app to the server.

```shell
$ mvn wildfly:deploy
```

This deploys the `custom-principal-ee/application/target/custom-principal-ee.war` to the running
instance of the server. You should see a message in the server log indicating that the archive
deployed successfully.

### Access the Application

The web app will be running at `http://localhost:8080/custom-principal-ee`. To see and manipulate
the HTTP headers within the HTTP requests, it is recommended to use a client like `curl` to invoke
the servlet.

Open a terminal, and attempt to access the secured page. It will return a `401 Unauthorized` error.:

```shell
$ curl -v http://localhost:8080/custom-principal-ee/secured

[...]
< HTTP/1.1 401 Unauthorized
< Expires: 0
< Connection: keep-alive
< WWW-Authenticate: Basic realm="principalRealm"
< Cache-Control: no-cache, no-store, must-revalidate
< Pragma: no-cache
< Content-Type: text/html;charset=UTF-8
< Content-Length: 71
< Date: Fri, 13 Jan 2023 21:21:07 GMT
<
<html><head><title>Error</title></head><body>Unauthorized</body></html>
```

Now, access the page as a user, by using the `Authorization` header. The BASIC authentication shown
below uses the Base64 encoding of `quickstartUser:password123!`:

```shell
$ curl -v http://localhost:8080/custom-principal-ee/secured -H "Authorization: Basic cXVpY2tzdGFydFVzZXI6cGFzc3dvcmQxMjMh"

[...]
< HTTP/1.1 200 OK
< Expires: 0
< Connection: keep-alive
< Cache-Control: no-cache, no-store, must-revalidate
[...]
<!DOCTYPE html>
<html lang="en">
    <head><title>SecuredServlet - doGet()</title></head>
    <body>
        <h1>Custom Principal - Elytron Demo</h1>
        <p>For reference, transform sequence is quickstartUser -> <em>customQuickstartUser</em>.</p>
        <p>Injection check - these values should match:</p>
        <ul>
            <li>Identity as available from Jakarta Security (<code>SecurityContext</code>): <strong>customQuickstartUser</strong></li>
            <li>Identity as available from injection (<code>SecurityIdentity</code>): <strong>customQuickstartUser</strong></li>
            <li>Identity as provided in HTTPServletRequest: <strong>customQuickstartUser</strong></li>
        </ul>
        <p>Custom Principal check - these values should match:</p>
        <ul>
            <li>Caller principal<code>(SecurityContext.getCallerPrincipal)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUser</strong></li>
            <li>Custom principal<code>(SecurityContext.getPrincipalsByType)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUser</strong></li>
            <li>Injection<code>(SecurityIdentity.getPrincipal)</code>: Class -> <strong>org.wildfly.security.examples.CustomPrincipal</strong>, Name -> <strong>customQuickstartUser</strong></li>
        </ul>
        <p>Custom Principal usage - this check will return a result of <em>enabled</em> if the CustomPrincipal was injected properly:</p>
        <ul>
            <li>SecurityContext reports last login <strong>was 10 days ago</strong>. Secrets access is <strong>enabled</strong>.</li>
            <li>SecurityIdentity reports last login <strong>was 10 days ago</strong>. Secrets access is <strong>enabled</strong>.</li>
    </body>
</html>
```

### Understanding the Console Output

When the client accesses the secured servlet,
the [CustomAuthenticationMechanism](./application/src/main/java/org/wildfly/security/examples/CustomAuthenticationMechanism.java)
passes the BASIC authentication credential to
the [CustomIdentityStore](./application/src/main/java/org/wildfly/security/examples/CustomIdentityStore.java),
forming a basic Jakarta Security implementation. If the credential is authenticated, a principal
transformer is used to create an instance of
the [CustomPrincipal](./components/src/main/java/org/wildfly/security/examples/CustomPrincipal.java),
before notifying the container of the login. _(NOTE: the use of a custom `PrincipalTransformer`
implementation is not required; the transformation can be done directly from
the `CustomAuthenticationMechanism` if desired.)_

This principal is then made available to Elytron and Jakarta Security classes, which are injected
into the servlet. When the servlet is invoked, the custom principal `customQuickstartUser` can be
used to perform class-specific operations. In this example, it retrieves the current and last login
date, to determine if the client can still access secrets.

### Undeploy the Quickstart

1. Make sure you start the WildFly server.
2. Open a terminal to the root directory of this quickstart.
3. Type this command to undeploy the archive:

```$xslt
$ mvn wildfly:undeploy
```

### Undeploy the App and Restore the WildFly Standalone Server Configuration

You can restore the original server configuration by undeploying the web app, and running
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
```
