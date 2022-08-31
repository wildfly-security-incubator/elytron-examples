## Securing an application deployed to WildFly with Bearer Token Authentication

This example demonstrates how to secure an application deployed to WildFly with Bearer Token
Authentication when using the Elytron OpenID Connect (OIDC) Client subsystem.

The OIDC configuration in this example is part of the deployments themselves. Alternatively,
this configuration could be specified via the `elytron-oidc-client` subsystem instead.
For more details, take a look at the [documentation](https://docs.wildfly.org/26.1/Admin_Guide.html#Elytron_OIDC_Client).


### Usage

#### Set up your Keycloak OpenID provider

Follow the steps in this [getting started guide](https://www.keycloak.org/getting-started/getting-started-docker)
to start Keycloak and create a realm called `myrealm`.

Next, create a client called `service`. If you're using Keycloak 19.0.0 or later, in the `Capability config`, be sure to uncheck `Standard flow`
and `Direct access grants` (when no authentication flows are specified, this indicates that the client is a bearer only client). For older
versions of Keycloak, change the `Access Type` to `bearer-only`.

Now, we're going to create a second client called `app`. If you're using Keycloak 19.0.0 or later, in the `Capability config`,
turn on `Client authentication`. For older versions of Keycloak, change the `Access Type` to `confidential`.

For the `app` client, we also need to set the valid redirect URIs to `http://localhost:8090/oidc-app/*` and set the
`Web origins` to `+` to permit all origins of Valid Redirect URIs.

Now, click on `Realm roles` and create two roles, `user` and `admin`.

Finally, create a user called `alice` and assign her the `user` and `admin` roles. Then, create a user called `bob`
and assign him only the `user` role. Steps for assigning roles can be found in the [Keycloak documentation](https://www.keycloak.org/docs/latest/server_admin/#proc-assigning-role-mappings_server_administration_guide).

#### Deploy the service and the app that invokes the service to WildFly

First, we're going to start our WildFly instance. When starting our WildFly instance, we're going to specify the `oidc.client.secret` system
property. Notice that this is [specified](https://github.com/wildfly-security-incubator/elytron-examples/blob/master/oidc-with-bearer/app/src/main/webapp/WEB-INF/oidc.json)
in the OIDC application's `oidc.json` file and is used to specify the secret that should be used when communicating with the Keycloak OpenID provider.
From the Keycloak Admin Console, navigate to the `app` client that we created earlier, then click on `Credentials`, and copy
the value for the `Client secret`. We're going to start our WildFly instance using this copied value. Notice that we're
specifying a port offset here since our Keycloak instance is already exposed on port 8080:

```
./bin/standalone.sh -Djboss.socket.binding.port-offset=10 -Doidc.client.secret=COPIED_VALUE
```

Now, first deploy the service

```
cd oidc-with-bearer/service
mvn wildfly:deploy -Dwildfly.port=10000
```

Then, let's deploy the OIDC app:

```
cd oidc-with-bearer/app
mvn wildfly:deploy -Dwildfly.port=10000
```

#### Access the app

We can access our application using http://localhost:8090/oidc-app/.

Try invoking the different endpoints without logging in. You'll only be able to successfully invoke
the `public` endpoint.

Now try logging in as `alice`. You'll be redirected to Keycloak to log in. Then try invoking
the different endpoints again. This time, you'll be able to successfully invoke all three endpoints
because `alice` has both `user` and `admin` roles.

Finally, try accessing the application again but this time, log in as `bob`. When you try invoking
the endpoints now, you'll see that you can only invoke the `public` and `secured` endpoints
since `bob` does not have the `admin` role.
