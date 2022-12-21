Implementing and making use of a custom-principal-transformer
=============================================================

This example demonstrates how to implement and make use of a simple principal transformer which converts a principal name to all upper case characters.

Usage
*****

Compile:

        mvn clean install

Add a module that contains our custom principal transformer to WildFly:

        bin/jboss-cli.sh
        module add --name=org.wildfly.security.examples.custom-principal-transformer --resources=/PATH_TO_ELYTRON_EXAMPLES/elytron-examples/custom-principal-transformer/target/custom-principal-transformer-2.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron,org.wildfly.extension.elytron

Add a ```custom-principal-transformer``` in the Elytron subsystem that references this new module and our custom principal transformer class that is contained in this module:

        /subsystem=elytron/custom-principal-transformer=myPrincipalTransformer:add(module=org.wildfly.security.examples.custom-principal-transformer, class-name=org.wildfly.security.examples.CasePrincipalTransformer)

This results in the following XML in the Elytron subsystem:

        <custom-principal-transformer name="myPrincipalTransformer" module="org.wildfly.security.examples.custom-principal-transformer" class-name="org.wildfly.security.examples.CasePrincipalTransformer"/>

Now, make use of the ```custom-principal-transformer```:

        /subsystem=elytron/security-domain=ManagementDomain:write-attribute(name=pre-realm-principal-transformer,value=myPrincipalTransformer)