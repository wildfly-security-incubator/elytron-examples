## Using a JDBC Security Realm

This example shows how to use a JDBC security realm with BCrypt passwords.



### Usage

1. Run the wildfly server
```
{path_to_wildfly}/bin/standalone.sh
```
2. Navigate to the source folder of this example and configure elytron
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configure-server.cli
```

3. Build and deploy the artifacts
```
mvn clean package install wildfly:deploy
```
#### Now you can access the the quickstart at http://localhost:8080/jdbc-security-realm-bcrypt-password

When logging in using **quickstartUser** with password **quickstartPwd1!**, you will successfully see some security information
```
Successfully called Secured Servlet

Principal : quickstartUser
Remote User : quickstartUser
Authentication Type : BASIC
```

When logging in using **guest** with password **guestPwd1!**, the browser will display the following error:

```
Forbidden
```

### To restore wildfly to its initial configuration
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restore-configuration.cli
```

### To undeploy the artifact
```
mvn clean package install wildfly:undeploy
```