# Demonstrate the use of dynamic client SSL context by configuring 2 reverse proxies with different SSL context required

## Generate certificates for 2 different mutual ssl contexts in $WILDFLY_HOME/standalone/configuration

```
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 365 -keystore server1.keystore -dname "CN=localhost" -keypass secret -storepass secret

keytool -genkeypair -alias client1 -keyalg RSA -keysize 2048 -validity 365 -keystore client1.keystore -dname "CN=client1" -keypass secret -storepass secret

keytool -exportcert  -keystore server1.keystore -alias localhost -keypass secret -storepass secret -file server1.cer

keytool -exportcert  -keystore client1.keystore -alias client1 -keypass secret -storepass secret -file client1.cer

keytool -importcert -keystore server1.truststore -storepass secret -alias client1 -trustcacerts -file client1.cer

keytool -importcert -keystore client1.truststore -storepass secret -alias localhost -trustcacerts -file server1.cer

keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 365 -keystore server2.keystore -dname "CN=localhost" -keypass secret -storepass secret

keytool -genkeypair -alias client2 -keyalg RSA -keysize 2048 -validity 365 -keystore client2.keystore -dname "CN=client2" -keypass secret -storepass secret

keytool -exportcert  -keystore server2.keystore -alias localhost -keypass secret -storepass secret -file server2.cer

keytool -exportcert  -keystore client2.keystore -alias client2 -keypass secret -storepass secret -file client2.cer

keytool -importcert -keystore server2.truststore -storepass secret -alias client2 -trustcacerts -file client2.cer

keytool -importcert -keystore client2.truststore -storepass secret -alias localhost -trustcacerts -file server2.cer
```

## Run the configure.cli file

Examine the commands in the `configure.cli` file. The file configures ports 9443 and 10443 so that they require a different two-way TLS connection. The URL 8080/proxy has been configured as a reverse proxy, forwarding requests to port 9443, where there is a WildFly welcome page. Similarly, the URL 8080/proxy2 forwards requests to port 10443. These ports require different certificates as there is a different two-way TLS configured. 

# Test the dynamic client ssl context
Try accessing the http://localhost:8080/proxy and the http://localhost:8080/proxy2 . Both of these URLs will successfully return Welcome to WildFly page. The requests are able to connect and display the Welcome page on both of these URLs because the dynamic client SSL context has selected the appropriate SSL contexts to use for the connections.
