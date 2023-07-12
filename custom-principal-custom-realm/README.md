# Using custom principals with a custom realm

This example demonstrates how to use custom principals with a custom realm.

## Usage

### Use of WILDFLY_HOME

In the following instructions, replace `WILDFLY_HOME` with the actual path to your WildFly
installation.

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
   to `custom-principal-custom-realm/application/target` and `custom-principal-custom-realm/components/target`.

```shell
    $ mvn clean install
```

### Configure Elytron

#### Configure a custom-realm

Review the `configure-elytron.cli` file in the root of this example directory. This quickstart
includes two Maven modules, and the CLI script performs a number of operations on the `components`
module, which contains our custom principal:

1. The `custom-principal-custom-realm-components` archive is added as a module to the server. This allows the
   custom components to be used by the server.
2. A `custom-realm` is configured using our [MyRealm](./components/src/main/java/org/wildfly/security/examples/MyRealm.java) implementation.
3. A custom `pre-realm-principal-transformer` is configured using our [CustomPreRealmTransformer](./components/src/main/java/org/wildfly/security/examples/CustomPreRealmTransformer.java)
   implementation. This transformer converts a principal into our custom principal.
4. A `security-domain` is configured that references our `custom-realm` and `pre-realm-principal-transformer`.
5. The `application-security-domain` mapping in the Undertow subsystem is updated to reference our new security domain.

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

#### Deploy the Web App

Next, deploy the web app, built from the `custom-principal-custom-realm` module, to the
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
Log in with `myadmin` and `mypassword`. The raw HTML you'll see is shown below:

```html
<!DOCTYPE html>
<html><head><title>SecuredServlet - doGet()</title></head>
    <body>
        <h1>Custom Principal - Elytron Demo</h1>
        <ul>
            <li>Principal name from <code>HttpServletRequest.getUserPrincipal()</code>: <strong>myadmin</strong></li>
        </ul>
        <p>Invoking a method from our custom principal class:</p>
        <ul>
            <li>Calling <code>CustomPrincipal.getLastLoginTime()</code>: <strong>2023-07-02T16:14:55.396852</strong></li>
        </ul>
    </body>
</html>
```

