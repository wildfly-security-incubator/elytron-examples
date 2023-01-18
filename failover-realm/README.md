# Failover realm in WildFly Elytron

Purpose of failover realm is to support fail over to an alternative realm in cases some realm is unavailable.
Common use case is to fail over to a local file based realm containing administrator identities when an LDAP or database server has gone down allowing at least administrators to gain access to the server.

In this example we will add LDAP security realm to the elytron subsystem. You do not have to worry about configuring of LDAP server as this security realm is expected to fail in this example.
We will also configure filesystem realm. Both realms will have user `frank` configured with role `Admin` and password `secret123`.
We then configure `failover-realm` to ensure that in cases when LDAP realm is unavailable, we will log this event and use the backup filesystem realm instead.

This is how the failover-realm is added:

```
/subsystem=elytron/failover-realm=failoverRealm:add(delegate-realm=exampleLdapRealm,failover-realm=exampleFSRealm)
```

We will also add security domain that uses this `failoverRealm` and add HTTP authentication factory that uses said security domain. 
We configured undertow subsystem to use this http authentication factory with authentication mechanism `BASIC`.
Please take a look at the `configureFailoverRealm.cli` file located in this folder for more details.

## Usage

Run wildfly server and then move to source folder of this example. To configure server you can use:

```bash
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configureFailoverRealm.cli
```

Compile the secured servlet included in this example and deploy it to the running server:

```bash
mvn clean install wildfly:deploy
```

Access http://localhost:8080/failover-realm-demo/secure URL with your browser and provide username `frank` and password `secret123`.
Note that you have been successfully authenticated even though the LDAP server has not been configured. 
This is because the fail over filesystem realm has been used to successfully authenticate frank after the LDAP realm failed to respond.
When you look at the log of your running WildFly instance, you will see the following warning:

```
WARN  [org.wildfly.security] (default task-1) ELY13001: Realm is failing over.: org.wildfly.security.auth.server.RealmUnavailableException: ELY01125: Ldap-backed realm failed to obtain context
```

This warning message notifies you that the LDAP realm was unavailable and fail over realm was used instead.

## Restoring configuration

Once you are finished with the demo, undeploy the servlet from the running server:

```bash
mvn wildfly:undeploy
```

Then, restore the server configuration by running the `restoreFailoverRealm.cli` script:

```bash
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restoreFailoverRealm.cli
```
