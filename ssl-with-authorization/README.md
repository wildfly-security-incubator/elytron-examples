## Using SSL session negotiation with authorization

This example shows how to use SSL session negotiation with authorization using Elytron.

### Overview

* [ Certificate generation ](#certGeneration)
* [ Server and client two-way SSL setup ](#sslSetup)
* [ Adding authorization ](#addingAuthz)

<a name="certGeneration"></a>
### Certificate generation

First, clone the ```elytron-examples``` repo locally:

```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples
```

Next, let's generate some client and server certificates that will be used in this example to set up two-way SSL between
a server and two clients:

```
   cd dynamic-certificates
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

<a name="sslSetup"></a>
### Server and client two-way SSL setup

Let's now set up a server WildFly instance and two client WildFly instances with two-way SSL between the
server and the two clients. We'll set them up on the same machine for this example. Note that ```client1```
and ```client2``` will be reverse proxies that send requests to the server instance.

```
    cp -r $WILDFLY_HOME server
    cp -r $WILDFLY_HOME client1
    cp -r $WILDFLY_HOME client2
```

We'll use ```$SERVER_HOME```, ```$CLIENT1_HOME```, ```$CLIENT2_HOME``` to refer to the paths to ```server```,
```client1```, and ```client2``` respectively.

Next, copy the keystores and truststores generated in the previous section to the server and client instances:

```
   cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/server.* $SERVER_HOME/standalone/configuration
   cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/client1.* $CLIENT1_HOME/standalone/configuration
   cp $PATH_TO_ELYTRON_EXAMPLES/dynamic-certificates/target/client2.* $CLIENT2_HOME/standalone/configuration
```

Next, start the server and client instances using the following commands:
 
 ```
    $SERVER_HOME/bin/standalone.sh
    $CLIENT1_HOME/bin/standalone.sh -Djboss.socket.binding.port-offset=1000
    $CLIENT2_HOME/bin/standalone.sh -Djboss.socket.binding.port-offset=2000
 ```
 
 The following commands can now be used to set up two-way SSL between the server and clients:
 
```
    $SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/ssl-with-authorization/configure-elytron-server.cli
    $CLIENT1_HOME/bin/jboss-cli.sh --connect --controller=remote+http://localhost:10990 --file=$PATH_TO_ELYTRON_EXAMPLES/ssl-with-authorization/configure-elytron-client1.cli 
    $CLIENT2_HOME/bin/jboss-cli.sh --connect --controller=remote+http://localhost:11990 --file=$PATH_TO_ELYTRON_EXAMPLES/ssl-with-authorization/configure-elytron-client2.cli 
```

Take a look at the ```configure-elytron-server.cli``` and ```configure-elytron-client{1,2}.cli``` files in the
```ssl-with-authorization``` directory for more details on the commands used to set up two-way SSL and to see the
reverse proxy configuration.  

At this point, since both client proxies use a certificate that's been signed by the ```Elytron CA``` certificate authority
and since the server trusts this certificate authority, both clients should be able to connect to the server. To test this,
try accessing the following two URLs:

```
Client proxy 1 URL:
https://localhost:9443/proxy

Client proxy 2 URL:
https://localhost:10443/proxy
```

Note that since the clients' certificates are not trusted by your browser, you'll need to manually confirm that these
certificates are trusted (in Chrome, click on Advanced -> Proceed to localhost). You should then see the WildFly
landing page.

<a name="addingAuthz"></a>
### Adding authorization

Now, let's update the server instance so that only the client with distinguished name ```CN=client1``` is allowed to
connect.

```
$SERVER_HOME/bin/jboss-cli.sh --connect
```
    
First, create a principal decoder that can be used to obtain the CN value from the X500 principal retrieved from a
certificate:

```
/subsystem=elytron/x500-attribute-principal-decoder=CNDecoder:add(oid="2.5.4.3",maximum-segments=1,convert=true)
```
    
Next, create a ```filesystem-realm``` that will be used to store the CN values that are allowed to connect to the server:

```
/subsystem=elytron/filesystem-realm=proxiesRealm:add(path=proxies,relative-to=jboss.server.config.dir)
```
    
Next, add each CN value that the sever should allow to connect to this new ```filesystem-realm``` (in this example,
we'll only allow distinguished names with ```CN=client1```):

```
/subsystem=elytron/filesystem-realm=proxiesRealm:add-identity(identity="client1")
```
    
Now create a ```security-domain``` that references the newly created ```filesystem-realm```. Notice that
this ```security-domain``` makes use of the ```x500-attribute-prinicpal-decoder``` that was previously 
created and also makes use of the ```default-permission-mapper``` to ensure identities loaded from this
```security-domain``` are granted ```LoginPermission```.

```
/subsystem=elytron/security-domain=proxiesDomain:add(realms=[{realm=proxiesRealm}],default-realm=proxiesRealm,permission-mapper=default-permission-mapper,pre-realm-principal-transformer=CNDecoder)
``` 

Then, update the ```server-ssl-context``` that was created earlier by ```configure-elytron-server.cli``` so that it
references the newly created ```security-domain``` and ensure that authentication is mandatory:

```
/subsystem=elytron/server-ssl-context=twoWaySSC:write-attribute(name=security-domain,value=proxiesDomain)
/subsystem=elytron/server-ssl-context=twoWaySSC:write-attribute(name=authentication-optional,value=false)
```    

Finally reload the server:

```
reload
```

Now try accessing the two URLs again:

```
Client proxy 1 URL:
https://localhost:9443/proxy


Client proxy 2 URL:
https://localhost:10443/proxy
```

Note that since the clients' certificates are not trusted by your browser, you'll need to manually confirm that
these certificates are trusted (in Chrome, click on Advanced -> Proceed to localhost). 

This time, you'll only successfully see the WildFly landing page using the first URL.
Using the second URL will result in an error since the certificate used by the second client has
distinguished name ```CN=client2``` and ```client2``` is not one of the CN values that are allowed to connect
to the server.


This example has shown how to set up SSL session negotiation with authorization using Elytron.