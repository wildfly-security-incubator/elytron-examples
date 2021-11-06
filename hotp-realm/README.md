HOTP Security Realm for WildFly Elytron
=======================================

This project demonstrates how a simple security realm can be developed to
support verification of HOTP passwords.

HOTP should really be used as part of two factor authentication so that the
user can demonstrate something they know and something they have, this is a
simplified example which just demonstrates the something they have.

As a simplified example this also does not consider other items such as look
ahead and token deactivation.

In the future it is hoped that WildFly Elytron will support both HOTP and TOTP
across multiple security realms but for now this project illustrates how a 
custom realm can be developed for evidence verification.

Project Build
-------------

Before proceeding with the following instructions the example project needs to
be compiled.

        mvn package

Realm Data
----------

Working with HOTP the first piece required is a set of identities along with
HOTP secrets and the ability to identify the first OTP values for testing
purposes.

The `IdentityGenerator` can be used for this purpose, this utility will 
dynamically create 10 identities, each with a randomly generated secret.

The utility can be executed with the following command:

````
mvn exec:exec -Ddestination="/home/darranl/tmp/hotp-realm.json"
````

This command will output the first ten OTPs for each of the generated identities:

````
*
* Generating Set of Identities.
*

OTP Codes for User0 ( 814575 251769 841067 945885 017208 976249 270192 630132 565884 115377 )
OTP Codes for User1 ( 818476 428724 996517 171927 894724 239981 795773 347317 381253 787530 )
OTP Codes for User2 ( 563289 005994 706076 669655 028308 316747 476859 849202 534407 155660 )
OTP Codes for User3 ( 302845 665686 791653 023907 162721 973759 117174 423242 228784 881758 )

....

````

The command will also write the identies to the specified destination for use by the realm:

````
*
* Identities written to /home/darranl/tmp/hotp-realm.json.
*
````

If you omit the destination the contents of the file will also be written to the console.

An individual entry in the file looks like:

````json
        {
            "groups": [
                "Users"
            ],
            "hotpSecret": "2YCFCQXCTMOYIZYVGGW652QC6Z7GMC3G",
            "lastHotpCount": 0,
            "name": "User0"
        }
````

It is worth noting that the secret is Base32 encoded so could also be added to 
an authenticator app or key instead of relying on the generated values.


Installation to WildFly
-----------------------

Before the realm can be added to the WildFly configuration the realm
implementation needs adding to WildFly as a module using the
following CLI command.

````
module add --name=org.wildfly.security.examples.hotp-realm \
           --resources=hotp-realm-1.0.0.Alpha1-SNAPSHOT.jar \
           --dependencies=javax.json.bind.api,org.eclipse.yasson,org.wildfly.common,org.wildfly.security.elytron-base
````

The newly added `module.xml` will also need to be edited to import the services for `org.eclipse.yasson`.

````xml
<module name="org.eclipse.yasson" services="import" />
````

Sill within the CLI a new realm definition can be added to the Elytron
subsystem referencing the json file created previously.

````
/subsystem=elytron/custom-realm=hotp-realm:add( \
    class-name=org.wildfly.examples.hotp.HotpSecurityRealm, \
    module=org.wildfly.security.examples.hotp-realm, \
    configuration={org.wildfly.examples.hotp.HotpSecurityRealm.path=/home/darranl/tmp/hotp-realm.json})
````

The existing `ApplicationDomain` resource can now be modified to use this new
security realm instead of the current properties realm.

````
batch
/subsystem=elytron/security-domain=ApplicationDomain:write-attribute( \
    name=realms[0].realm, \
    value=hotp-realm)
/subsystem=elytron/security-domain=ApplicationDomain:write-attribute( \
    name=default-realm, \
    value=hotp-realm)
run-batch
:reload
````

The newly added security realm will now be active, any applications secured 
using `ApplicationDomain` will now make use of the HOTP authentication of
the realm.

As the HOTP realm is truly single use passwords it can only be used with
authentication mechanisms that store the identity in a session such as
HTTP FORM authentication or the JBoss Remoting SASL authentication which
treats the connection as an authenticated session.


