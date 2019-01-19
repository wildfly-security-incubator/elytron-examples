### Securing an embedded Netty server using Elytron

This example shows how to secure an embedded Netty server making use of the Netty HttpServerCodes and using HTTP BASIC authentication backed by Elytron.

The ```ElytronHandlers```integration classe that is used in this example
application to secure Netty using Elytron can be found in the [elytron-web-netty](https://github.com/wildfly-security/elytron-web-netty) project.

1. Take a look at the ```org.wildfly.security.examples.HelloWorld``` class in this example that starts an embedded
Netty server, creates an Elytron map-backed security realm, and adds the configuration necessary to secure the embedded
Netty server using Elytron. Notice that the created security realm has two users, ```alice``` and ```bob```, with passwords
```alice123+``` and ```bob123+```, respectively.

2. Build and run this example application:

```
    mvn clean install exec:exec
```

3. The application can be accessed at the following location using either username and password.

```
    http://localhost:7776
```
