Simple role-mapper for WildFly Elytron
======================================

Simple role mapper, which maps roles as configured in custom-role-mapper definition.

Maps role from key in the configuration map to appropriate value.

This can be used as trivial replacement of mapped-role-mapper for AS which does not provide mapped-role-mapper.

Usage
*****

Compile:

        mvn package

Add the module into the WildFly:

        bin/jboss-cli.sh
        module add --name=org.wildfly.security.examples.custom-role-mapper --resources=custom-role-mapper-1.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron,org.wildfly.extension.elytron

Add a custom-role-mapper into the subsystem:

        /subsystem=elytron/custom-role-mapper=myRoleMapper:add(module=org.wildfly.security.examples.custom-role-mapper, class-name=org.wildfly.security.examples.MyRoleMapper, configuration={roleA=role1, roleB=role2})

Example resulting XML:

        <custom-role-mapper name="myRoleMapper" module="org.wildfly.security.examples.custom-role-mapper" class-name="org.wildfly.security.examples.MyRoleMapper">
            <configuration>
                <property name="roleA" value="role1"/>
                <property name="roleB" value="role2"/>
            </configuration>
        </custom-role-mapper>

