## Dynamic Certificates

This example can be used to generate certificates for two way ssl.

### Usage

* To run with default distinguished names (CN=client1 and CN=client2), navigate to the home directory of this project
and run the following command
```
mvn clean install exec:java -Dexec.mainClass="org.wildfly.security.examples.CertificateGenerationExample"
```

The above command generates the following keystores and truststores in the ```dynamic-certificates/target``` directory:

```
   client1.keystore
   client1.truststore
   client2.keystore
   client2.truststore
   server.keystore
   server.truststore
```

* ```client1.keystore``` contains a certificate with distinguished name: ```CN=client1```
* ```client2.keystore``` contains a certificate with distinguished name: ```CN=client2```
* Both client certificates are issued by a certificate authority with distinguished name: ```CN=Elytron CA, ST=Elytron, C=UK, EMAILADDRESS=elytron@wildfly.org, O=Root Certificate Authority```
* ```server.truststore``` contains this certificate authority's certificate

* To run with custom distinguished names instead, navigate to the home directory of this project and run the following command
```
mvn clean install exec:java -Dexec.mainClass="org.wildfly.security.examples.CertificateGenerationExample" -Dexec.args="CN=customName1 CN=customName2"
```
