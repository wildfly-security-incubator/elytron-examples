## RESTEasy client integration with Elytron client example

### This example demonstrates how RESTEasy client can use credentials and SSL context from Elytron client.

* Run wildfly server and then move to source folder of this example. To configure elytron:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configure.cli
```

We have added new user jane to the security domain and set HTTP BASIC authentication for application security domain.
Please see content of configure.cli for more information.

* Configure mutual SSL on your running wildfly instance. You can read how to do it here: [two-way SSL in Wildfly](https://docs.wildfly.org/29/WildFly_Elytron_Security.html#enable-two-way-ssltls-in-wildfly-for-applications)

* Configure path to client's keystore and truststore in this project's *wildfly-config.xml* accordingly

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

* To restore wildfly server configuration go to the source folder of this example and run:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restore.cli
```


