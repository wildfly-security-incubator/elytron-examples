# Using Jakarta Authentication (JASPI) with WildFly Elytron and standalone Undertow

This example demonstrates how to use Jakarta Authentication with WildFly Elytron, and deploy a secured Servlet to a standalone Undertow server. This document is provided for deployment instructions; a more in-depth view and explanation is available in [this blog post](https://darranl.blogspot.com/2018/10/using-wildfly-elytron-jaspi-with.html).

## Usage

### Use of WILDFLY_HOME

In the following instructions, replace `WILDFLY_HOME` with the actual path to your WildFly installation.

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
3. Type the following command to build the artifacts. A JAR will be exported to `undertow-standalone-jaspi/target`.
```shell
    $ mvn clean install
```

### Run the Demo App

The demo app configures Elytron using [the programmatic approach](https://docs.wildfly.org/27/WildFly_Elytron_Security.html#the-programmatic-approach), so a single command will configure Elytron, deploy the application, and launch it. To do so, run the following command in a terminal:
```shell
mvn exec:java
```

### Access the Application

The application will be running at `http://localhost:28080/helloworld`. To see and manipulate the
HTTP headers within the HTTP requests, it is recommended to use a client like `curl` to invoke the servlet.

Open a new terminal, and attempt to access the secured page. It will return a `401 Unauthorized` error:

```
$ curl -v http://localhost:28080/helloworld/secured
...
< HTTP/1.1 401 Unauthorized
< Expires: 0
< Connection: keep-alive
< Cache-Control: no-cache, no-store, must-revalidate
< Pragma: no-cache
< X-MESSAGE: Please resubmit the request with a username specified using the X-USERNAME and a password specified using the X-PASSWORD header.
< Content-Type: text/html;charset=UTF-8
< Content-Length: 71
< 
```

Now, access the page as a user, by using the appropriate username and password headers:

```
$ curl -v http://localhost:28080/helloworld/secured -H "X-Username:elytron" -H "X-Password:Coleoptera"
...
< HTTP/1.1 200 OK
< Expires: 0
< Connection: keep-alive
< Cache-Control: no-cache, no-store, must-revalidate
< Pragma: no-cache
< Content-Length: 154
< 
<html>
  <head><title>Secured Servlet</title></head>
  <body>
    <h1>Secured Servlet</h1>
    <p>
 Current Principal 'elytron'    </p>
  </body>
</html>
```

This time, the user is successfully authenticated by the ServerAuthModule, and the principal is returned.

### Undeploying the Application

Since the application was configured programmatically, it will be undeployed once the application is killed. In the terminal where the app is running, kill it by entering `<Ctrl>C` or by closing the terminal. The WildFly server can also be shutdown now.
