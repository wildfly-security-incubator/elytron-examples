  Implementing and making use of a custom-principal-transformer
=============================================================

This example demonstrates how to implement and make use of a simple principal transformer that converts a KeycloakPrincipal to a NamePrincipal.

Usage
*****

Compile:

        mvn clean install

Add a module that contains our custom principal transformer:

        bin/jboss-cli.sh
        module add --name=org.wildfly.security.examples.keycloak-principal-transformer --resources=/PATH_TO_ELYTRON_EXAMPLES/elytron-examples/keycloak-principal-transformer/target/keycloak-principal-transformer-2.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron,org.wildfly.extension.elytron,org.keycloak.keycloak-adapter-subsystem

Add a ```custom-principal-transformer``` in the Elytron subsystem that references this new module and our custom principal transformer class that is contained in this module:

        /subsystem=elytron/custom-principal-transformer=keycloakPrincipalTransformer:add(module=org.wildfly.security.examples.keycloak-principal-transformer, class-name=org.wildfly.security.examples.KeycloakPrincipalTransformer)

This results in the following XML in the Elytron subsystem:

        <custom-principal-transformer name="keycloakPrincipalTransformer" module="org.wildfly.security.examples.keycloak-principal-transformer" class-name="org.wildfly.security.examples.KeycloakPrincipalTransformer"/>

Now, make use of the ```custom-principal-transformer```:

        /subsystem=elytron/security-domain=myDomain:write-attribute(name=post-realm-principal-transformer,value=keycloakPrincipalTransformer)
        reload
