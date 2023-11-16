# Distributed realm and ignoring of unavailable realms in WildFly Elytron

`ignore-unavailable-realms` attribute allows users to specify that the search should continue to the next realm if a realm happens to be unavailable.

In this example, we add to the elytron subsystem two file system realms: FsRealm1 and FsRealm2, and one LDAP realm: LdapRealm.
We will not be adding LDAP server as this security realm is expected to fail as unavailable.
The first filesystem realm will have `user1` with password `passwordUser1` and the second filesystem realm will have `user2` with password `passwordUser2`.
Both users will have a role `Admin`.
At the first, we will configure `distributed-realm` that combines these 3 realms with the following: 

```
/subsystem=elytron/distributed-realm=distributedRealm:add(realms=[FsRealm1, LdapRealm, FsRealm2])
```
**Note** that lookup is sequential across all realms in the list in the order they were provided. So authentication request will first be attempted on FsRealm1 then in LdapRealm and finally in FsRealm2. 
With the above configuration and unavailable LDAP realm trying to authenticate with user2 credentials will be unsuccessful.

We will also add a security domain that uses this `distributedRealm` and add HTTP authentication factory that uses said security domain. 
Undertow subsystem will be configured to use this HTTP authentication factory with authentication mechanism `BASIC`.
You can look at the `configureDistributedRealm.cli` file located in this folder for more details.

## Usage

Run WildFly server and then move to the source folder of this example. To configure the server you can use:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configureDistributedRealm.cli
```

Compile the secured servlet included in this example and deploy it to the running server:

```
mvn clean install wildfly:deploy
```

Access http://localhost:8080/distributed-realm-ignore-unavailable-realms-demo/secure URL with your browser and provide the credentials of user1.
You can see that user1 can authenticate successfully as it is present in FsRealm1 which is predeceasing unavailable LDAP realm. 
On the other hand, trying to authenticate with user2 credentials will result in an error as the unavailable LDAP realm is checked before FsRealm2.

Run the following to reset the `distributedRealm` configuration to use `ignore-unavailable-realms` attribute:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=resetDistributedRealmToIgnoreUnavailableRealms.cli
```

Now the `distributed-realm` is configured with the following:

```
/subsystem=elytron/distributed-realm=distributedRealm:add(realms=[FsRealm1, LdapRealm, FsRealm2], ignore-unavailable-realms=true)
```

Access http://localhost:8080/distributed-realm-ignore-unavailable-realms-demo/secure URL again, and now you should be able to successfully authenticate with user2 credentials.
Running WildFly instance will also log the following warning:

```
WARN  [org.wildfly.security] (default task-1) ELY13012: A realm within the distributed realm is unavailable. This realm will be ignored.: org.wildfly.security.auth.server.RealmUnavailableException: ELY01125: Ldap-backed realm failed to obtain context
```

Run the following to restore the changes made in this example:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restoreDistributedRealm.cli
```