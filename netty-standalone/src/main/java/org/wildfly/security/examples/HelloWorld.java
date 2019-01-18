/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.examples;

import static org.wildfly.security.password.interfaces.ClearPassword.ALGORITHM_CLEAR;

import java.security.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.wildfly.elytron.web.netty.server.ElytronHandlers;
import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.auth.permission.LoginPermission;
import org.wildfly.security.auth.realm.SimpleMapBackedSecurityRealm;
import org.wildfly.security.auth.realm.SimpleRealmEntry;
import org.wildfly.security.auth.server.MechanismConfiguration;
import org.wildfly.security.auth.server.MechanismConfigurationSelector;
import org.wildfly.security.auth.server.MechanismRealmConfiguration;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.authz.RoleDecoder;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.http.HttpServerAuthenticationMechanismFactory;
import org.wildfly.security.http.util.FilterServerMechanismFactory;
import org.wildfly.security.http.util.SecurityProviderServerMechanismFactory;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.permission.PermissionVerifier;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HelloWorld {

    private static final WildFlyElytronProvider elytronProvider = new WildFlyElytronProvider();

    private static final int PORT = 7776;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Here we go");

        SecurityDomain securityDomain = createSecurityDomain();
        securityDomain.registerWithClassLoader(HelloWorld.class.getClassLoader());

        EventLoopGroup parentGroup = new NioEventLoopGroup(1);
        EventLoopGroup childGroup = new NioEventLoopGroup(1);

        ElytronHandlers securityHandlers = ElytronHandlers.newInstance()
                .setSecurityDomain(securityDomain)
                .setFactory(createHttpAuthenticationFactory())
                .setMechanismConfigurationSelector(MechanismConfigurationSelector.constantSelector(
                        MechanismConfiguration.builder()
                                .addMechanismRealm(MechanismRealmConfiguration.builder().setRealmName("Elytron Realm").build())
                                .build()));

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(parentGroup, childGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new TestInitialiser(securityHandlers));

        bootstrap.bind(PORT).sync();
    }

    private static HttpServerAuthenticationMechanismFactory createHttpAuthenticationFactory() {
        HttpServerAuthenticationMechanismFactory factory = new SecurityProviderServerMechanismFactory(() -> new Provider[] {elytronProvider});

        return  new FilterServerMechanismFactory(factory, true, "BASIC");
    }

    private static SecurityDomain createSecurityDomain() throws Exception {
        // Create an Elytron map-backed security realm
        SimpleMapBackedSecurityRealm simpleRealm = new SimpleMapBackedSecurityRealm(() -> new Provider[] { elytronProvider });
        Map<String, SimpleRealmEntry> identityMap = new HashMap<>();

        // Add user alice
        identityMap.put("alice", new SimpleRealmEntry(getCredentialsForClearPassword("alice123+"), getAttributesForRoles("employee", "admin")));

        // Add user bob
        identityMap.put("bob", new SimpleRealmEntry(getCredentialsForClearPassword("bob123+"), getAttributesForRoles("employee")));

        simpleRealm.setIdentityMap(identityMap);

        // Add the map-backed security realm to a new security domain's list of realms
        SecurityDomain.Builder builder = SecurityDomain.builder()
                .addRealm("ExampleRealm", simpleRealm).build()
                .setPermissionMapper((principal, roles) -> PermissionVerifier.from(new LoginPermission()))
                .setDefaultRealmName("ExampleRealm");

        return builder.build();
    }

    private static List<Credential> getCredentialsForClearPassword(String clearPassword) throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ALGORITHM_CLEAR, elytronProvider);
        return Collections.singletonList(new PasswordCredential(passwordFactory.generatePassword(new ClearPasswordSpec(clearPassword.toCharArray()))));
    }

    private static MapAttributes getAttributesForRoles(String... roles) {
        MapAttributes attributes = new MapAttributes();
        HashSet<String> rolesSet = new HashSet<>();
        if (roles != null) {
            for (String role : roles) {
                rolesSet.add(role);
            }
        }
        attributes.addAll(RoleDecoder.KEY_ROLES, rolesSet);
        return attributes;
    }

}
