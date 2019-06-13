## Dynamic Certificates

This example can be used to generate certificates for two way ssl.

### Usage

* To run with default distinguished names (CN=client1 and CN=client2), navigate to the home directory of this project
and run the following command
```
mvn clean install exec:java -Dexec.mainClass="org.wildfly.security.examples.CertificateGenerationExample"
```
* To run with custom distinguished names, navigate to the home directory of this project and run the following command
```
mvn clean install exec:java -Dexec.mainClass="org.wildfly.security.examples.CertificateGenerationExample" -Dexec.args="CN=customName1 CN=customName2"
```
