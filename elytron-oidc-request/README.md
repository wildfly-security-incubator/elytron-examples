## Sending an OpenID Connect Authentication Requests Using Request Parameters

[ OpenID Connect ](https://openid.net/developers/how-connect-works/) is an identity layer on top of the OAuth 2.0 protocol that makes it possible for a client to verify a user’s identity based on authentication that’s performed by an OpenID provider. The OAuth 2.0 request syntax sends the Request Object in the request by adding them directly to the URL. However, OpenID Connect allows the Request Object to be sent as a JWT using the [request parameter](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests), which can be signed and optionally encrypted. This adds an added layer of protection, as the parameters are no longer human readable. 

WildFly 33 includes the ability to configure these request parameters to send the request as a JWT using either a `request` or a `request_uri`. The feature also includes the ability to sign and/or encrypt the JWT for added security. 

This guide demonstrates how to secure an example application using OpenID Connect while sending the request object as an optionally signed and/or encrypted Json Web Token (JWT). We will start with an example that sends the request object as an unsigned JWT, and add to the configuration to send the authentication request as a signed and and encrypted JWT. 

### Overview

* [ Setting up the KeyPairs ](#keypairsetup) 

* [ Setting up Keycloak OpenID Provider ](#setupKeycloak)

* [ Server Configuration ](#serverConfiguration)

* [ Deploying and Accessing the Application ](#deployingApp)

Note: This example demonstrates how to configure the request parameter values in two different ways: 

* [ Deployment Configuration ](#deploymentConfig)

* [ Subsystem Configuration ](#subsystemConfig)

### Setting up the KeyPairs

In order for us to sign the JWT's, we will need to use key pairs corresponding to the algorithms specified. These are tokens communicated between the client and the server and are used to sign and verify the JWT. Key pairs are also used to encrypt and decrypt the JWT. But we will be using a different key pair for that. 

#### About Key Pairs

Typically key pairs have 3 components: 

* Private Key: We will be using this to sign the JWT. In this case, we will create the key pair and have access to the private key. Private keys are also used to decrypt JWT's. Keycloak also shares the public keys of some key pairs with us. Keycloak will be using the private keys of those keyPairs to decrypt the JWT's we send, while we will be using the public keys to encrypt them.

* Public Key: This is used to encrypt the JWT. We use the public key that keycloak shares with us to encrypt our JWT's. Public keys can be extracted from the certificate. We will be able to identify the purpose of a key using the keyUse header, which can have either `sig` or `enc` values. We will be using the keys with `enc` to encrypt. 

* Certificate: This is used to verify the signatures. This is communicated to the keycloak sever. Certificate contains the public key. 

Some keys are symmetric, meaning they only have 1 secret key that is used by both the client and the server. An example of a symmetric algorithm is HS256, where the same key is used by the client and the server to sign a JWT and verify the signature respectively. 

#### Generating Key Pairs

We can use the `keytool` tool in the cli to generate our keys. We can generate keystore files of type `JKS` and `PKCS12` for our application. The general format for doing this is as follows: 
```
keytool -genkeypair -alias <keystore_alias> -keyalg <algorithm> -keysize <key_size> -validity <validity_in_days>
-keystore <keystore_name> -dname "<distinguished_name>" -keypass <private_key_password> -storepass <keystore_password>
```

#### Generating JKS Keystore

Let us first create a JKS file with 2 different keys. Let's first generate an `RSA` keyPair: 
```
keytool -genkey -alias rsaKey -keyalg RSA -validity 365 -keystore server.keystore -storetype JKS -storepass password -keypass password
```
You will see some prompts, for which you can press `enter` to leave them blank. For more information visit [here](https://access.redhat.com/documentation/en-us/red_hat_jboss_data_virtualization/6.2/html/security_guide/create_a_privatepublic_key_pair_with_keytool). 

To generate an elliptical curve key, enter the following commands: 
```
keytool -genkey -alias rsaKey -keyalg RSA -validity 365 -keystore server.keystore -storetype JKS -storepass password -keypass password
```

#### Generating PKCS12 Keystore

For RSA, please use the following commands: 
```
keytool -genkeypair -alias rsaKey -keyalg RSA -keysize 2048 -validity 365 -keystore keycloak.keystore.pkcs12 -dname "CN=client" -keypass password -storepass password
```
And for Elliptical Curve keys, use the following commands: 
```
keytool -genkeypair -alias ecKey -keyalg EC -groupname secp256r1 -validity 365 -keystore keycloak.keystore.pkcs12 -dname "CN=client" -keypass password -storepass password
``` 
Note that in this case, we have specified the key size for the RSA keys, and the groupname for Elliptical Curve keys. For the RSA keys, the same keysize can be used for RSA algorithms with different SHA values (i.e. RSA256, RSA512). However, this is not the case for Elliptical Curve keys. For ES256 algorithms, you need to use `-groupname secp256r1` and for ES384, use `-groupname secp384r1` and so on. 

### Setting up Keycloak OpenID Provider

It’s easy to set up Keycloak using Docker. Follow the steps in Keycloak’s [ getting started guide ](https://www.keycloak.org/getting-started/getting-started-docker) to start Keycloak, create a realm called `myrealm`, and a client called `myclient`. When registering the client, toggle the `Client authentication` option to be on.

After registering our client, `myclient`, we also need to configure `valid redirect URIs`. Simply click on `Clients` and then on `myclient`. In the `Valid Redirect URIs` field, enter http://localhost:8090/simple-webapp-oidc/*. Once you hit `Save`, you should see a new tab called `Keys` appear. 

Navigate to that tab and click on `Import`. Under `Archive format`, choose `PKCS12` from the dropdown. Under `Key alias`, enter the alias of the RSA key you just created, under `Store password`, enter the password of the keystore you created and click on the `Browse` button to import the keystore file. Note that when we created the keystore, we specified a key password and a keystore password. Here, we are only using the keystore password. While we set them both to be the same, they do not have to be. 

Finally, create a user called `alice` whose `First name` is Alice and `Last name` is Smith. 

Next, navigate to the `Credentials` tab and create a password for Alice. Toggle the `Temporary` switch off so you are not prompted to update the password after the first login. 

### Server Configuration

First clone the `elytron-example` repo locally and navigate to this project: 
```
    git clone https://github.com/wildfly-security-incubator/elytron-examples
    cd elytron-examples/elytron-oidc-request
```

In order to secure an application using OpenID Connect, we need to either add configuration in the `elytron-oidc-client` subsystem or add configuration to the application itself. This example will show you the steps for both. 

## Deployment Configuration

The configuration required to secure an application with OpenID Connect can be specified in the deployment. We will first be sending a plaintext request, which will be unsigned, signified by the "none" algorithm. 

The first step is to set up the deployment configuration using the `oidc.json` file in the `WEB-INF` directory of this project. The following will be the contents of the `oidc.json` file once we have specified the request object related attributes:
```
{
    "client-id" : "myclient",
    "provider-url" : "${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/myrealm",
    "public-client" : "false",
    "authentication-request-format" : "request",
    "request-object-signing-algorithm" : "none",
    "principal-attribute" : "preferred_username",
    "ssl-required" : "EXTERNAL"
    "credentials" : {
    	"secret" : "<INSERT CLIENT SECRET>"
    }
}
```

Note that we have specified the `authentication-request-format` to be `request`, meaning, we are sending it by value. We have specified the signing algorithm to be `none`. Alternatively, we could not have specified it too, since the default value is already `none`. Since we are not signing the JWT, we don't need to configure the keystore. 

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
```
    $ WILDFLY_HOME/bin/standalone.sh -Djboss.socket.binding.port-offset=10
```

Review the `configure-elytron.cli` file in the `elytron-oidc-request` project. This will add the `secure-deployment` resource and the appropriate attributes under it. 

Open a new terminal, navigate to the `WILDFLY_HOME` directory and enter the following command: 
```
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --controller=127.0.0.1:10000 --file=PATH/TO/ELYTRON/EXAMPLES/elytron-oidc-request/configure-elytron.cli
```
We are specifying the port here because of the port offset we used earlier. You should see the following result when you run the script:

```$xslt
    {"outcome" => "success"}
```

### Deploying and Accessing the Application

Now we are ready to deploy the web app using WildFly. From the `elytron-oidc-request` directory run the following commands: 
```
    $ mvn wildfly:deploy -Dwildfly.port=10000
```

Now, let’s try accessing our application using http://localhost:8090/simple-webapp-oidc/.

Click on "Access Secured Servlet".

Now, you’ll be redirected to Keycloak's login page. If you click on the url on the search bar, you will see the request value specified in the URL along with `client-id`, `response_type`, `redirect_uri` and the openid scope. These parameters are required to be included in the auth request according to the OAuth2 specifications. 

You will also notice a new `request` field added to the url. The url can look like the following: 
```
http://localhost:8080/realms/myrealm/protocol/openid-connect/auth?
response_type=code&
client_id=myclient&
scope=openid&
redirect_uri=http%3A%2F%2Flocalhost%3A8090%2Fsimple-webapp-oidc%2Fsecured&
request=eyJhbGciOiJSU0EtT0FFUCI...
```

Log in with `Alice` and the password that you set when configuring Keycloak.

Next, you’ll be redirected back to our application and you should see the "Secured Servlet" page. That means that we were able to successfully log in to our application using the Keycloak OpenID provider!

Now try changing the value for `authentication-request-format` to `request_uri` and keep everything else the same. You can do this either in the deployment configuration by editing the `oidc.json` file or in the subsystem configuration by typing in the following commands: 
```
/subsystem=elytron-oidc-client/secure-deployment=simple-webapp-oidc.war:write-attribute(name=authentication-request-format, value=request_uri)
```

Save the oidc.json file and deploy it again. If you are using the subsystem configuration, please type in `reload` to apply the changes and access http://localhost:8090/simple-webapp-oidc/. You will now see the request_uri appear in the url. 

Lastly, lets try to send a signed and encrypted request. For this, please update the `oidc.json` file to have the following contents: 
```
{
    "client-id" : "myclient",
    "provider-url" : "${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/myrealm",
    "public-client" : "false",
    "authentication-request-format" : "request_uri",
    "request-object-encryption-alg-value" : "RSA-OAEP",
    "request-object-encryption-enc-value" : "A128CBC-HS256",
    "request-object-signing-algorithm" : "RS512",
    "request-object-signing-keystore-file" : "/path/to/server.keystore",
    "request-object-signing-keystore-password" : "password",
    "request-object-signing-key-password" : "password",
    "request-object-signing-key-alias" : "rsakey",
    "request-object-signing-keystore-type" : "JKS",
    "ssl-required" : "EXTERNAL", 
    "credentials" : {
    	"secret" : "<INSERT CLIENT SECRET>"
    }
}
```
Save the file and deploy it again. 

To use subsystem configuration, you can run the following commands to configure the server as well: 
```
    $ WILDFLY_HOME/bin/jboss-cli.sh --connect --controller=127.0.0.1:10000 --file=PATH/TO/ELYTRON/EXAMPLES/elytron-oidc-request/configure-elytron-with-keystore.cli
```

This time, we are using the keyPairs we created earlier to sign the JWT. The JWT will be signed first and then encrypted using the public key that Keycloak shared with us. To see what this key looks like, you can either go to http://localhost:8080/realms/myrealm/protocol/openid-connect/certs or you can go to the keycloak console and under the `Realm settings` tab, click on the `keys` tab. You will see that there console includes 2 other key providers in addition to the ones on the link. These are symmetrical key algorithms. 

For symmetric Request Object signing algorithms, we do not need to configure keystores, since these keys don't have private/public keypairs. For WildFly, we sign these JWT's using a secret key and the keycloak server is easily able to verify it. 

Once you are ready to restore your server back to what it was, please enter the following on your terminal: 
```
    $ PATH/TO/WILDFLY_HOME/bin/jboss-cli.sh --connect --controller=127.0.0.1:10000 --file=$PATH/TO/ELYTRON/EXAMPLES/elytron-oidc-request/restore-elytron-configuration.cli
```

This example has demonstrated how to secure a web application deployed to WildFly by sending the request parameters as a JWT. For more details on the `elytron-oidc-client` subsystem, please check out the [ documentation ](https://docs.wildfly.org/30/Admin_Guide.html#Elytron_OIDC_Client) and for more details on OpenID Connect, checkout the [ OpenID documentation ](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests) and the documentation of your OpenID provider. 