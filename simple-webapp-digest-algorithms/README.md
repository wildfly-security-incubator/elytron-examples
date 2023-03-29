## Securing an application deployed on WildFly using HTTP DIGEST SHA-256 AND HTTP DIGEST-SHA-512-256

The default algorithm used with the HTTP DIGEST authentication mechanism is MD5. This example demonstrates how to configure a different algorithm for HTTP Digest on the WildFly server.

The commands to be used can be found in the `configure-server.cli` file. The required algorithms can be configured with the `mechanism-configurations` attribute of the `http-authentication-factory'`:

```
/subsystem=elytron/http-authentication-factory=example-fs-http-auth:add(http-server-mechanism-factory=global,security-domain=exampleFsSD,mechanism-configurations=[{mechanism-name=DIGEST-SHA-256,mechanism-realm-configurations=[{realm-name=exampleApplicationDomain}]},{mechanism-name=DIGEST-SHA-512-256,mechanism-realm-configurations=[{realm-name=exampleApplicationDomain}]}]
```

The authentication method `DIGEST-SHA-256` or `DIGEST-SHA-512-256` or both can be specified in the `web.xml` file.

Run the `configure-server.cli` commands to configure the WildFly server.

Then you can deploy the application:

```
mvn clean install wildfly:deploy 
```

#### Accessing the application

We can access our application using a browser at http://localhost:8080/simple-webapp/.

Click on "Access Secured Servlet". If you examine the network traffic, you will see that a server has sent an authentication challenge requiring a `Digest` HTTP mechanism with the algorithm `SHA-256` or with the algorithm `SHA-512-256`.

You can log in with the user `user` and password `passwordUser` configured by commands in the `configure-server.cli` file.
Your browser can choose which of the 2 algorithms it prefers and log in. Either `SHA-512-256` or `SHA-256` would be accepted by the server.
