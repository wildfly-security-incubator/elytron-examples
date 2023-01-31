/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;

import jakarta.security.auth.message.config.AuthConfigFactory;

import org.wildfly.elytron.web.undertow.server.servlet.AuthenticationManager;
import org.wildfly.security.auth.jaspi.ElytronAuthConfigFactory;
import org.wildfly.security.auth.jaspi.JaspiConfigurationBuilder;
import org.wildfly.security.auth.permission.LoginPermission;
import org.wildfly.security.auth.realm.SimpleMapBackedSecurityRealm;
import org.wildfly.security.auth.realm.SimpleRealmEntry;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.authz.RoleMapper;
import org.wildfly.security.authz.Roles;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.examples.jaspi.SimpleServerAuthModule;
import org.wildfly.security.examples.servlet.SecuredServlet;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.permission.PermissionVerifier;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.WebResourceCollection;

/**
 * A simple project demonstrating how the WildFly Elytron JASPI integration can be programatically configured and used for stand
 * alone integration with Undertow.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HelloWorld {

    private static final WildFlyElytronPasswordProvider elytronPasswordProvider = new WildFlyElytronPasswordProvider();

    private static final String HOST = "localhost";
    private static final int PORT = 28080;
    private static final String PATH = "/helloworld";
    private static final String SERVLET = "/secured";

    public static void main(String[] args) throws Exception {
        SecurityDomain securityDomain = createSecurityDomain();

        configureJaspi();

        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(SecuredServlet.class.getClassLoader())
                .setContextPath(PATH)
                .setDeploymentName("helloworld.war")
                .addSecurityConstraint(new SecurityConstraint()
                        .addWebResourceCollection(new WebResourceCollection()
                                .addUrlPattern("/secured/*"))
                        .addRoleAllowed("Users")
                        .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.DENY))
                .addServlets(Servlets.servlet(SecuredServlet.class)
                        .addMapping(SERVLET));

        AuthenticationManager authManager = AuthenticationManager.builder()
                .setSecurityDomain(securityDomain)
                .build();
        authManager.configure(deploymentInfo);

        DeploymentManager deployManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deployManager.deploy();

        PathHandler path = Handlers.path(Handlers.redirect(PATH))
                .addPrefixPath(PATH, deployManager.start());

        Undertow server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(path)
                .build();
        server.start();

        System.out.println();
        System.out.println(String.format("Ready for requests to http://%s:%d%s%s", HOST, PORT, PATH, SERVLET));
        System.out.println();
        System.out.println(String.format("e.g. 'curl http://%s:%d%s%s' \n", HOST, PORT, PATH, SERVLET));
    }

    private static String configureJaspi() {
        AuthConfigFactory authConfigFactory = new ElytronAuthConfigFactory();
        AuthConfigFactory.setFactory(authConfigFactory);

        return JaspiConfigurationBuilder.builder(null, null)
                .setDescription("Default Catch All Configuration")
                .addAuthModuleFactory(SimpleServerAuthModule::new)
                .register(authConfigFactory);
    }

    private static SecurityDomain createSecurityDomain() throws Exception {
        PasswordFactory passwordFactory = PasswordFactory.getInstance(ALGORITHM_CLEAR, elytronPasswordProvider);

        Map<String, SimpleRealmEntry> identityMap = new HashMap<>();
        identityMap.put("elytron", new SimpleRealmEntry(Collections.singletonList(new PasswordCredential(passwordFactory.generatePassword(new ClearPasswordSpec("Coleoptera".toCharArray()))))));

        SimpleMapBackedSecurityRealm simpleRealm = new SimpleMapBackedSecurityRealm(() -> new Provider[] { elytronPasswordProvider });
        simpleRealm.setIdentityMap(identityMap);

        SecurityDomain.Builder builder = SecurityDomain.builder()
                .setDefaultRealmName("TestRealm");

        builder.addRealm("TestRealm", simpleRealm).build();
        builder.setRoleMapper(RoleMapper.constant(Roles.of("Test")));

        builder.setPermissionMapper((principal, roles) -> PermissionVerifier.from(new LoginPermission()));

        return builder.build();
    }

}
