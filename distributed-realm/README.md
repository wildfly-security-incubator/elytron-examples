# Distributed realm in WildFly Elytron

Simple demonstration of distributed security realm that can be configured in the elytron subsystem.
Purpose of distributed realm is to combine identities across multiple realms.

In this example we add 2 file system realms; FsRealm1 and FsRealm2, to the elytron subsystem. 
We will add `user1` with password `passwordUser1` to the first filesystem realm and `user2` with password `passwordUser2` to the second filesystem realm.
Both users will have a role `Admin`.
We will then configure `distributed-realm` that combines this 2 realms with the following: 

```
/subsystem=elytron/distributed-realm=distributedRealm:add(realms=[FsRealm1, FsRealm2])
```
**Note** that lookup is sequential across all realms in the list in the order they were provided. So authentication request will first be attempted on FsRealm1 and then FsRealm2. 
Note also that both realms must be available.

We will also add security domain that uses this `distributedRealm` and add HTTP authentication factory that uses said security domain. 
We configured undertow subsystem to use this http authentication factory with authentication mechanism `BASIC`.
Please take a look at the `configureDistributedRealm.cli` file located in this folder for more details.

## Usage

Run wildfly server and then move to source folder of this example. To configure server you can use:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configureDistributedRealm.cli
```

Compile the secured servlet included in this example and deploy it to the running server:

```
mvn clean install wildfly:deploy
```

Access http://localhost:8080/distributed-realm-demo/secure URL with your browser and provide credentials of either user1 or user2.
You will see that  both users can connect successfully even though they reside in different security realms. 
This is because we have configured `distributed-realm` which combines both realms.

Run the following to restore the changes made in this example:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restoreDistributedRealm.cli
```
