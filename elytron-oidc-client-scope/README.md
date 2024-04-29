## Configuring Elytron OIDC Client with Additional Scope Values 

The [ Securing WildFly Apps with OpenID Connect ](https://wildfly-security.github.io/wildfly-elytron/blog/securing-wildfly-apps-openid-connect/) example demonstrates how to use OpenID Connect to perform authentication on a web app deployed on WildFly. 

[ OpenID Connect ](https://openid.net/developers/how-connect-works/) is an identity layer on top of the OAuth 2.0 protocol that makes it possible for a client to verify a user’s identity based on authentication that’s performed by an OpenID provider. The [ OpenID Connect specifications ](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest) indicate that there are other scope values which may be included in the authentication request to ask permission to additional and specific resources. The purpose of this example is to demonstrate how to configure additional scope values to request access to additional fields.

### Overview

* [ Setting up Keycloak OpenID Provider ](#setupKeycloak)

* [ Server Configuration ](#serverConfiguration)

* [ Deploying and Accessing the Application ](#deployingApp)

Note: This example demonstrates how to configure scope values in two different ways: 

* [ Deployment Configuration ](#deploymentConfig)

* [ Subsystem Configuration ](#subsystemConfig)

### Setting up Keycloak OpenID Provider

It’s easy to set up Keycloak using Docker. Follow the steps in Keycloak’s [ getting started guide ](https://www.keycloak.org/getting-started/getting-started-docker) to start Keycloak, create a realm called `myrealm`, and a client called `myclient`. 

After registering our client, `myclient`, we also need to configure `valid redirect URIs`. Simply click on `Clients` and then on `myclient`. In the `Valid Redirect URIs` field, enter http://localhost:8090/simple-webapp-oidc/*.

Navigate to the `Client scopes` tab for `myclient` and change the `Assigned type` for all scope values from `default` to `optional`. When `Client scopes` are set to `Default`, the claims associated with them are automatically added to the access token you receive. Changing it to `Optional` ensures that they are not included by default and you will only get access to claims that you request using the scope values you configure. This list of scope are the values that Keycloak allows and it varies for different OpenID providers.

Now, click on `Realm roles` and create two roles, `user` and `admin`.

Finally, create a user called `alice` whose `First name` is Alice and `Last name` is Smith and assign her the `user` and `admin` roles. Steps for assigning roles can be found in the Keycloak [documentation](https://www.keycloak.org/docs/latest/server_admin/#proc-assigning-role-mappings_server_administration_guide).

Next, navigate to the `Credentials` tab and create a password for Alice. Toggle the `Temporary` switch off so you are not prompted to update the password after the first login. 

### Server Configuration

First clone the `elytron-example` repo locally and navigate to this project: 
```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples/elytron-oidc-client-scope
```

In order to secure an application using OpenID Connect, we need to either add configuration in the `elytron-oidc-client` subsystem or add configuration to the application itself. This blog will show you the steps for both. 

## Deployment Configuration

The configuration required to secure an application with OpenID Connect can be specified in the deployment.

The first step is to set up the deployment configuration using the `oidc.json` file in the `WEB-INF` directory of this project. The following will be the contents of the `oidc.json` file once we have specified the scope values:
```
{
    "client-id" : "myclient",
    "provider-url" : "${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/myrealm",
    "public-client" : "true",
    "scope" : "profile email roles web-origins microprofile-jwt offline_access",
    "principal-attribute" : "preferred_username",
    "ssl-required" : "EXTERNAL"
}
```

Note that we have specified the scope values to be `profile`, `email`, `web-origins`, `microprofile-jwt`, `roles` and `offline_access` in a space delimited list inside the `oidc.json` file. `profile`, `email` and `offline_access` are OpenID built-in scopes, while `web-origin`, `microprofile-jwt` and `roles` are Keycloak specific scope values and allow access to additional claims. Read the descriptions under the `Client scope` tab for `myclient` to learn more about the purpose of these scope values. 

Next, navigate to the OIDC application's `web.xml` file and look for the following command: 
```
<login-config>
    <auth-method>OIDC</auth-method>
</login-config>
```

If you would like to configure the `elytron-oidc-client` subsystem instead, then follow the steps outlines in the next section. 

NOTE: If you have both the subsystem and the deployment configured, then the `elytron-oidc-client` subsystem configuration will override the deployment configuration. 

## Subsystem Configuration

To configure the server using the `elytron-oidc-client` subsystem, we will be making use of the `WILDFLY_HOME` project. 

First start the server. We will be using a port offset of 10 since our Keycloak instance is already exposed on port 8080.

Note that since this is a preview level feature, we need to start the server with the stability level set to preview.
```
    $ WILDFLY_HOME/bin/standalone.sh -Djboss.socket.binding.port-offset=10 --stability=preview
```

Review the `configure-elytron.cli` file in the `elytron-oidc-client-scope` project. This will add the `secure-deployment` resource and the appropriate attributes under it, including the `scope` attribute. 

Open a new terminal, navigate to the `WILDFLY_HOME` directory and enter the following command: 
```
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --controller=127.0.0.1:10000 --file=PATH/TO/ELYTRON/EXAMPLES/elytron-oidc-client-scope/configure-elytron.cli
```
We are specifying the port here because of the port offset we used earlier. You should see the following result when you run the script:

```$xslt
    {"outcome" => "success"}
```

### Deploying and Accessing the Application

Now we are ready to deploy the web app using WildFly. From the `elytron-oidc-client-scope` directory run the following commands: 
```
mvn wildfly:deploy -Dwildfly.port=10000
```

Now, let’s try accessing our application using http://localhost:8090/simple-webapp-oidc/.

Click on "Access Secured Servlet".

Now, you’ll be redirected to Keycloak's login page. If you click on the url on the search bar, you will see the scope values specified in the `redirect-uri` field with the different scope values separated by a `+`. You will also notice that a new scope value: `openid`. This indicates that we are going to be using OpenID Connect to authenticate the user. 

Log in with `Alice` and the password that you set when configuring Keycloak.

Next, you’ll be redirected back to our application and you should see the "Secured Servlet" page. That means that we were able to successfully log in to our application using the Keycloak OpenID provider!

This page will display the Current Principal,  and a list of claim values obtained using the scope values you configured. This is what it will look like: 
```
Current Principal 'alice'

Claims received using additional scope values:

By configuring the "profile" scope, the "given_name" and "family_name" claims are present in the access token and have values : Alice and Smith

By configuring the "email" scope, the "email_verified" claim is present in the access token and has value : false

By configuring the "roles" scope, the "realm_access" claim is present in the access token and has value : {roles=[default-roles-myrealm, offline_access, admin, uma_authorization, user]}

By configuring the "microprofile-jwt" scope, the "groups" claim is present in the access token and has value : [default-roles-myrealm, offline_access, admin, uma_authorization, user]

By configuring the "web-origins" scope, the "allowed-origins" claim is present in the access token and has value : [http://localhost:8090]
```

Note that the value for Current Principal may be different, since that is the client secret. 

Notice that there are no claims obtained using the `offline_access` scope. To learn more about what this scope value does, please refer to the [ OpenID Documentation ](https://openid.net/specs/openid-connect-core-1_0.html#OfflineAccess). 


## Invalid Scope Values

Different OpenID providers have their own set of valid scope values and this set varies depending on the OpenID provider. Try changing the scope values to `INVALID_SCOPE`. 

If you used the subsystem configuration, then navigate to the `WILDFLY_HOME/bin` folder and type in the following commands: 
```
./bin/jboss-cli.sh --connect --controller=127.0.0.1:10000

/subsystem=elytron-oidc-client/secure-deployment=simple-webapp-oidc.war:write-attribute(name=scope, value=INVALID_SCOPE)
```

If you used the deployment configuration, then you can go back to the `oidc.json` file inside the `WEB-INF` folder to edit the scope values. 

Deploy and access the webapp again. Since `INVALID_SCOPE` is not one of the acceptable scope values, you will now see a `Bad request` page instead of being redirected to the Keycloak login page. You will notice that the url now contains 
`error=invalid_scope&error_description=Invalid+scopes`
This indicates that your authentication request was rejected because it contains invalid scope values. 

Once you are ready to restore your server back to what it was, please enter the following on your terminal: 
```
$PATH/TO/WILDFLY_HOME/bin/jboss-cli.sh --connect --controller=127.0.0.1:10000 --file=$PATH/TO/ELYTRON/EXAMPLES/elytron-oidc-client-scope/restore-elytron-configuration.cli
```

This example has demonstrated how to secure a web application deployed to WildFly using additional scope values on OpenID Connect. For more details on the `elytron-oidc-client` subsystem, please check out the [ documentation ](https://docs.wildfly.org/32/Admin_Guide.html#Elytron_OIDC_Client) and for more details on OpenID Connect, checkout the [ OpenID documentation ](https://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims) and the documentation of your OpenID provider. 