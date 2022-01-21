## Multi-tenancy support for OpenID Connect (OIDC) apps

This example demonstrates how to add multi-tenancy support to an OpenID Connect (OIDC) application
deployed to WildFly.

### Usage

#### Set up your Keycloak Realms

Follow the steps in this [getting started guide](https://www.keycloak.org/getting-started/getting-started-docker) to
start Keycloak, create a realm called `tenant1`, create a user called `alice`, and register a client called `myclient`.

After registering our client, `myclient`, we also need to configure valid redirect URIs. Simply click
on `Clients` and then on `myclient`. In the `Valid Redirect URIs` field, enter http://localhost:8090/multi-tenancy-oidc/*.

Now, because we're going to make use of two Keycloak realms in this example, repeat the above steps again to create
a second realm called `tenant2`, create a user called `bob`, and register a client with the same name we used above, `myclient`.

Again, after registering our client, `myclient`, we also need to configure valid redirect URIs. Simply click on `Clients` and then
on `myclient`. In the `Valid Redirect URIs` field, enter http://localhost:8090/multi-tenancy-oidc/*.


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

We can access our application using http://localhost:8090/multi-tenancy-oidc/tenant1.

Click on "Access Secured Servlet".

Now, you'll be redirected to Keycloak to log in. Log in with `alice` and the password that you
set when configuring Keycloak.

Next, you'll be redirected back to our application and you should see the "Secured Servlet" page.

Notice that if you try logging in as `bob`, authentication will fail since `bob` is not part of
the `tenant1` Keycloak realm.

Next, try accessing our application using http://localhost:8090/multi-tenancy-oidc/tenant2.

Click on "Access Secured Servlet".

Now, you'll be redirected to Keycloak to log in. This time, log in with `bob` and the password that you
set when configuring Keycloak. Now, because `bob` is part of the `tenant2` Keycloak realm,
you'll be redirected back to our application and you should see the "Secured Servlet" page.



