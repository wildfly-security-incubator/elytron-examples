## Using the IP address of a remote client for authorization decisions

This example shows how to make use of the IP address of a remote client for authorization decisions.

### Server configuration

We'll configure a `source-address-role-decoder` in the Elytron subsystem. Take a look at the `configure-elytron-server.cli`
file in the `source-address-role-decoder` directory for more details on the commands used to set this up.

```
    $WILDFLY_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/source-address-role-decoder/configure-elytron-server.cli
```
### Run the client

To run the client, use:

```
mvn exec:exec
```

Now, let's take a closer look at the output:

```
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Successfully called secured bean, caller principal alice

Principal has admin permission: true
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

We can see that we were able to successfully invoke both EJB methods. This is because our filesystem-based
identity store assigns `alice` the `employee` role that's required to invoke the first method. Our
`source-address-role-decoder` also assigns `alice` the `admin` role since the IP address of our remote
client is 127.0.0.2.

Let's update our wildfly-config.xml file so that a different IP address will be used for our remote client
by updating the `bind-address` as follows:

```
<bind-address bind-address="127.0.0.3" bind-port="61111" match="0.0.0.0/0"/>
```

Now, try running the client and inspecting the output again:

```
mvn clean install exec:exec
```

```
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Successfully called secured bean, caller principal alice

Principal has admin permission: false
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
```

Notice that this time, only one of the two EJB methods can be successfully invoked. In particular,
since `alice` is now connecting from 127.0.0.3 (instead of from 127.0.0.2), our `source-address-role-decoder`
no longer assigns `alice` the `admin` role. Thus, she is unable to invoke the method that requires `admin` role.

This example has shown how to make use of the IP address of a remote client for authorization decisions.