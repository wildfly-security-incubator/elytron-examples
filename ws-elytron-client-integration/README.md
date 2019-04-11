## WS client integration with Elytron client example

### This example demonstrates how WS client can use username, password and SSL context from wildfly-config.xml

* Please clone the following repositories: 

[jbossws-spi](https://github.com/wildfly-security-incubator/jbossws-spi/tree/WS_client_Elytron_integration)

[wildfly-elytron](https://github.com/wildfly-security-incubator/wildfly-elytron/tree/WS_client_Elytron_integration)

[jbossws-common](https://github.com/wildfly-security-incubator/jbossws-common)

* checkout to **WS_client_Elytron_integration** branch in each of these projects.

* Install all these repositories (in this order) with 
``
mvn clean install
``

* Configure mutual SSL on your running wildfly instance like here: [two-way SSL in Wildfly](https://docs.jboss.org/author/display/WFLY/Using+the+Elytron+Subsystem#UsingtheElytronSubsystem-EnableTwowaySSL%252FTLSinWildFlyforApplications)

* Configure path to client's keystore and truststore in this project's wildfly-config.xml accordingly

* Deploy the [helloworld-ws](https://github.com/wildfly/quickstart/tree/master/helloworld-ws) quickstart

* Install this project with 
``
mvn clean install
``
and run tests in Client.java
