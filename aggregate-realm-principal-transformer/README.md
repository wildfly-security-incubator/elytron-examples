## Using a principal-transformer in aggregate-realm

This example shows how to configure an ```aggregate-realm``` with a ```principal-transformer```. We will create two filesystem 
realms to be used in aggregate in our ```aggregate-realm```: one will hold the credentials for authentication of the identity 
and one will hold the attributes for authorization of the identity. The principal (name) of the identity in the two realms 
will be different and so we will configure our aggregate realm with a ```principal-transformer``` to be able to correctly 
load the full aggregate identity.

### Overview

* [ Set Up ](#setUp)
* [ Server configuration ](#serverConfiguration)
* [ Deploying and accessing the application ](#deployingApp)

<a name="setUp"></a>
### Set Up

Clone the ```elytron-examples``` repo locally:

```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples
```

<a name="serverConfiguration"></a>
### Server configuration

The following command can now be used to configure the ```aggregate-realm``` and secure the web application
with an HTTP authentication mechanism which will use our aggregate-realm.

```
    $SERVER_HOME/bin/jboss-cli.sh --connect --file=$PATH_TO_ELYTRON_EXAMPLES/aggregate-realm-principal-transformer/configure-elytron-server.cli
```

It creates a new identity with the username: "guest" and password: "guestPwd1!". It also adds the attribute Roles=Users to the 
identity as that role is required to be authorized to access the application. The password is stored in the authentication realm 
under the name "guest" and the attribute is stored in the authorization realm under the name "guest-attributes" and so 
we configure the ```agggregate-realm``` with a constant principal transformer which return the principal "guest-attributes".

Take a look at the ```configure-elytron-server.cli``` file in the ```aggregate-realm-principal-transformer``` directory 
for more details on the configuration.

<a name="deployingApp"></a>
### Deploying and accessing the application

We’re going to make use of the ```simple-webapp``` project. It can be deployed using the following commands:

```
cd $PATH_TO_ELYTRON_EXAMPLES/simple-webapp
mvn clean install wildfly:deploy
```

You can access the application at localhost:8080/simple-webapp. 
Try logging in with the username: "guest" and password: "guestPwd1!".
You are successfully authorized to access the secured application, which means our transformer worked and the attribute
Roles=Users was successfully loaded from the ```authorization-realm``` using the transformed principal. You can try 
removing the ```principal-transformer``` from the ```aggregate-realm``` configuration and seeing that you will be
forbidden from accessing the application. 
