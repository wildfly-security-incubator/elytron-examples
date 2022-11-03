Implementing and making use of an ejb-client-interceptor
=============================================================

This example demonstrates how to implement and make use of a simple EJB client side interceptor
that will always runAs the current {@code SecurityIdentity} to activate any outflow identities.

Usage
*****

Compile:

        mvn clean install

Add a module that contains our EJB client side interceptor:

        bin/jboss-cli.sh
        module add --name=org.wildfly.security.examples.ejb-client-interceptor --resources=/PATH_TO_ELYTRON_EXAMPLES/elytron-examples/ejb-client-interceptor/target/ejb-client-interceptor-1.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.jboss.ejb-client,org.wildfly.security.elytron

Add a `client-interceptor` in the ejb3 subsystem that references this new module and our EJB client interceptor class that is contained in this module:

        /subsystem=ejb3:write-attribute(name=client-interceptors,value=[{class=org.wildfly.security.examples.IdentityEJBClientInterceptor,module=org.wildfly.security.examples.ejb-client-interceptor}])
        reload


This results in the following XML in the ejb3 subsystem:

        <client-interceptors>
            <interceptor module="org.wildfly.security.examples.ejb-client-interceptor" class="org.wildfly.security.examples.IdentityEJBClientInterceptor"/>
        </client-interceptors>

