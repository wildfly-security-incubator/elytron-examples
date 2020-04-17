## Using server side SNI matching

This example shows how to use server side SNI matching using Elytron. This makes it possible for
a WildFly instance to have multiple virtual hosts that share a single IP and present the correct
certificate based on the hostname specified by the client in the SNI extension. 

### Virtual hosts

First, use the following command to configure two virtual hosts:

```
    $WILDFLY_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/server-ssl-sni-context/configure-virtual-hosts.cli
```

### Deploy two applications

```
   cd app1
   mvn clean install wildfly:deploy
   
   cd app2
   mvn clean install wildfly:deploy
```

### Server configuration

We'll now configure one-way SSL and configure server side SNI matching. Take a look at the `configure-sni-matching.cli`
file in the `server-ssl-sni-context` directory for more details on the commands used to set this up.

```
    $WILDFLY_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/server-ssl-sni-context/configure-sni-matching.cli
```
### Try accessing both hostnames via HTTPS

Try accessing both hostnames and inspect the certificate that is presented by the server. Notice that app1.com's
certificate is presented when accessing https://app1.com:8443 and app2.com's certificate is presented when accessing
https://app2.com:8443.

```
https://app1.com:8443

https://app2.com:8443
```

This example has shown how to configure server side SNI matching.