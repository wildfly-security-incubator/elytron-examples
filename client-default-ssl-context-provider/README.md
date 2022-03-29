## WildFly Elytron client default SSLContext provider

### This example demonstrates how WildFlyElytronClientDefaultSSLContextProvider can be used to register JVM wide default SSL context.

This example uses java security API to dynamically register a provider. Alternatively, you can use static provider registration and add the `WildFlyElytronClientDefaultSSLContextProvider` to the list of providers in java.security file.

* Configure mutual SSL on your running WildFly instance. You can read how to do it here: [two-way SSL in Wildfly](https://docs.jboss.org/author/display/WFLY/Using%20the%20Elytron%20Subsystem.html#110231569_UsingtheElytronSubsystem-EnableTwowaySSL%2FTLSinWildFlyforApplications)

* Configure path to client's keystore and truststore in this project's *wildfly-config-two-way-tls.xml* accordingly

* Deploy application:

```
cd server
mvn clean install wildfly:deploy
```

* Run the client's test: 

```
cd ../client
mvn clean install -Dtest=Client.java
```

The test registers the `WildFlyElytronClientDefaultSSLContextProvider` with *wildfly-config-two-way-tls.xml* file to have the highest priority. The RESTEasy client which had `SSLContext.getDefault()` configured as its SSLContext can therefore connect to the running server instance.

* To restore wildfly server configuration go to the source folder of this example and run:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restore.cli
```


