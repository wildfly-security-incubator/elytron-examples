Simple custom-credential store for WildFly Elytron
=======================================

Usage
*****

Compile:

	mvn package


Commands to load the provider into the wilfly app:


	bash-4.4$ /opt/jboss/keycloak/bin/jboss-cli.sh 
	You are disconnected at the moment. Type 'connect' to connect to the server or 'help' for the list of supported commands.
	[disconnected /] connect
	[standalone@localhost:9990 /] module add --name=org.wildfly.examples.my-credential-provider --resources=/home/iamj/uta-provider/custom-credential-provider-1.0.0.Alpha1-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron
	[standalone@localhost:9990 /] /subsystem=elytron/provider-loader=my-credential-provider:add(class-names=[org.wildfly.examples.MyCredentialStoreProvider],module=org.wildfly.examples.my-credential-provider)
	[standalone@localhost:9990 /] /subsystem=elytron:write-attribute(name=initial-providers,value=my-credential-provider)
	[standalone@localhost:9990 /] reload
	[standalone@localhost:9990 /] /subsystem=elytron/provider-loader=my-credential-provider:read-attribute(name=loaded-providers)

