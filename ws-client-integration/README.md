## WS client integration with Elytron client example

### This example demonstrates how WS client can use username, password and SSL context from Elytron client.

* Run wildfly server and then move to source folder of this example. To configure elytron:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configure.cli
```

We have added new user jane to the security domain and set HTTP BASIC authentication for application security domain.
Please see content of configure.cli for more information.

* Configure mutual SSL on your running wildfly instance like here: [two-way SSL in Wildfly](https://docs.jboss.org/author/display/WFLY/Using+the+Elytron+Subsystem#UsingtheElytronSubsystem-EnableTwowaySSL%252FTLSinWildFlyforApplications)

* Configure path to client's keystore and truststore in this project's wildfly-config.xml accordingly

* Deploy application:

```
cd server
mvn clean install wildfly:deploy
```

* Run client test that shows that correct credentials and SSLContext were set.

```
cd ../client
mvn clean install -Dtest=Client.java
```

