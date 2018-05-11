Simple security-event-listener for WildFly Elytron
==================================================

Simple security event listener, which prints information about successful and failed authentication tries to the stderr.

Usage
*****

Compile:

        mvn package

Add the module into the WildFly:

        bin/jboss-cli.sh
        module add --name=org.wildfly.security.examples.custom-security-event-listener --resources=custom-security-event-listener-1.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron

Add a custom-security-event-listener into the subsystem:

        /subsystem=elytron/custom-security-event-listener=myAuditLogger:add(module=org.wildfly.security.examples.custom-security-event-listener, class-name=org.wildfly.security.examples.MySecurityEventListener, configuration={myAttribute="myValue"})

