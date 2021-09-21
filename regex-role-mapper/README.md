## Using a regex role mapper 

This example demonstrates the use of `regex-role-mapper` resource that was added to elytron subsystem in order to simplify a mapping of roles in the Elytron security domain to other roles with the use of regular expressions.

### Set Up

Clone the ```elytron-examples``` repo locally:

```
git clone https://github.com/wildfly-security-incubator/elytron-examples
cd elytron-examples/regex-role-mapper
```

### Server configuration

Run the server with:

```
$SERVER_HOME/bin/standalone.sh
```

The following command can now be used to configure the ```regex-role-mapper``` in the subsystem.

```
$SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/regex-role-mapper/configure-elytron.cli
```

The configure-elytron.cli script configured the server to use the regular expression role mapper that maps the roles to only represent a last letters of a role that are after the last `-` sign.
In other words it maps roles that match .*-([a-z]*) to the role $1.

Then the script added a user `joe` with the following roles: `["123-user","123-admin"]`


### Test regex role mapper

Now connect to the WildFly CLI with 

```
$SERVER_HOME/bin/jboss-cli.sh --connect 
```

and run the following command:

```
/subsystem=elytron/security-domain=mySD:read-identity(name=joe)
```

you will see the following output:
```
{
    "outcome" => "success",
    "result" => {
        "name" => "joe",
        "attributes" => {"Roles" => [
            "123-user",
            "123-admin"
        ]},
        "roles" => [
            "admin",
            "user"
        ]
    }
}
```

The above output means that the regex role mapper successfully mapped roles from the `Roles` attribute to be `admin` and `user` only.
