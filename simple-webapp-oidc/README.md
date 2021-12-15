## Securing an application deployed to WildFly with OpenID Connect (OIDC)

This example demonstrates how to secure an application deployed to WildFly with OpenID Connect
(OIDC) without needing to use the Keycloak client adapter.

The OIDC configuration in this example is part of the deployment itself. Alternatively,
this configuration could be specified via the `elytron-oidc-client` subsystem instead.
For more details, take a look at the [documentation](https://docs.wildfly.org/26/Admin_Guide.html#Elytron_OIDC_Client).


### Usage

#### Set up your Keycloak OpenID provider

Follow the steps in this [getting started guide](https://www.keycloak.org/getting-started/getting-started-docker) to
start Keycloak, create a realm called `myrealm`, create a user called `myuser`, and register a client called `myclient`.

After registering our client, `myclient`, we also need to configure valid redirect URIs. Simply click
on `Clients` and then on `myclient`. In the `Valid Redirect URIs` field, enter http://localhost:8090/simple-webapp-oidc/*.

#### Deploy the app to WildFly

First, we're going to start our WildFly instance (notice that we're specifying a port offset here
since our Keycloak instance is already exposed on port 8080):

```
./bin/standalone.sh -Djboss.socket.binding.port-offset=10
```

Then, we can deploy our app:

```
mvn wildfly:deploy -Dwildfly.port=10000
```

#### Access the app

We can access our application using http://localhost:8090/simple-webapp-oidc/.

Click on "Access Secured Servlet".

Now, you'll be redirected to Keycloak to log in. Log in with `myuser` and the password that you
set when configuring Keycloak.

Next, you'll be redirected back to our application and you should see the "Secured Servlet" page.

We were able to successfully log in to our application via the Keycloak OpenID provider!

