## System Properties and Encrypted Expressions Example

First enable support for encrypted expressions by running the `enable-encrypted-expressions.cli` script as follows:

```
$WILDFLY_HOME/bin/jboss-cli.sh --connect --file=enable-encrypted-expressions.cli
```

Next let's encrypt a clear text value and convert it to an encrypted expression:

```
[standalone@localhost:9990 /] history --disable
[standalone@localhost:9990 /] /subsystem=elytron/expression=encryption:create-expression( \
    resolver=initial-resolver, clear-text=MyPassword)
{
    "outcome" => "success",
    "result" => {"expression" => \
        "${ENC::initial-resolver:RUxZAUMQEH6CP3xXyAqYzqsC3oNayyeGH32wsdAZ8VLkkxaEmWc=}"}
}
```

Copy the resulting encrypted expression and update the value of the `secret.password` property in the [system.properties](https://github.com/fjuma/elytron-examples/blob/systemProperties/system-properties/system.properties) file in this example's root directory.

Start WildFly using this `system.properties` file:

```
$WILDFLY_HOME/bin/standalone.sh -P PATH_TO_ELYTRON_EXAMPLES/system-properties/system.properties
```

Next, let's add another system property that refers to the system property from our file:

```
[standalone@localhost:9990 /] /system-property=myproperty:add(value=${secret.password})
```

Finally, let's deploy the example application from the `system-properties` directory:

```
mvn clean install wildfly:deploy
```

Now try accessing http://localhost:8080/system-properties/properties.

Notice that `System.getProperty("secret.password")` returns the encrypted expression without decrypting it.

Notice that `System.getProperty("myproperty")` returns the decrypted value, `MyPassword`.

