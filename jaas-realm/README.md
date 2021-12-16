# JAAS realm in WildFly Elytron

This example demonstrates the use of `jaas-realm` resource in the elytron subsystem. 

JAAS security realm initializes a [LoginContext](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/login/LoginContext.html) from [JAAS Login Configuration file](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jgss/tutorials/LoginConfigFile.html) and uses it to authenticate and authorize users.

You can specify custom [LoginModule](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/spi/LoginModule.html) implementation in the JAAS configuration file.

## Principals to attributes mapping 

Authenticated users in Elytron can have attributes associated with them. These can be roles, emails, phone number or other values.
Elytron will map the [Principals](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/Principal.html) obtained from the [Subject](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/Subject.html) to these attributes with the following rule:

 * *key* of the attribute is principal’s simple classname, so the value of `principal.getClass().getSimpleName())`

 * *value* is principal’s name, so the result of `principal.getName()` call. For principals of the same type / key, the values will be appended to the collection under this attribute key.

So to assign roles for the identity, you can implement a Principal interface with a class named `Roles`.
To add a role `Guest` for the user in your Login Module, just create an instance of the Roles principal that returns string `Admin` from the [getName()](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/Principal.html#getName()) method.
  
You can look at `loginmodules.CustomLoginModule2` class in this project to see an example of this.

Optionally, you can implement custom CallbackHandler that should be used with LoginContext.
The method `setSecurityInfo(final Principal principal, final Object evidence)` can be implemented in the CallbackHandler to set any security information before it is used for authentication. You can see an example of this method in the `loginmodules.CustomCallbackHandler` class of this example.

## Create jar with custom Login Modules and custom Callback Handler

Change to `custom-login-modules` directory and build jar containing custom classes:

```
mvn clean install
```

The above created a jar file with login modules `loginmodules.CustomLoginModule1`, `loginmodules.CustomLoginModule2` and custom callback handler `loginmodules.CustomCallbackHandler`.
The custom Callback Handler is optional and Elytron uses default implementation of CallbackHandler when none is provided.


## Configure server to use JAAS realm 

You can look at the `configure.cli` file to see the commands that add a jar file as a module and configure JAAS realm for applications. Change the path to the `custom-login-modules-1.0.jar` and `JAAS-login-modules.conf` file to be accurate.

You can configure the running server instance with the following:

```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configure.cli
```

## Deploy REST endpoint to test the JAAS realm

Directory `server-rest-endpoint` contains a simple REST endpoint that prints out authenticated user.

For the purpose of this example we will deploy this endpoint to verify that the JAAS realm can authenticate and authorize users with provided custom Login Module implementations.

Change to the `server-rest-endpoint` directory of this example and deploy this endpoint to the running WildFly instance:

```   
mvn clean install wildfly:deploy
```

You can connect to [http://localhost:8080/jax-rs-basic-auth/rest/ping](http://localhost:8080/jax-rs-basic-auth/rest/ping) and use the credentials that are accepted by the LoginModule implementations.
Those are `user1` with `passwordUser1`, `user2` with `passwordUser2` and `user3` with `passwordUser3`. 
Only `user1` and `user2` have the `Admin` role necessary to connect to the REST endpoint.

You should see the following output when connecting with user1:

```   
Hello user1!
```

