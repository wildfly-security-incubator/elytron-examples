/*
 * Copyright 2023 Red Hat, Inc.
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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The remote client responsible for making invoking the intermediate bean to demonstrate invocation of custom
 * principals in EJB to remote EJB calls.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
public class RemoteClient {

    public static void main(String[] args) throws Exception {
        System.out.println("Connecting as unauthenticated user, then as authenticated user");
        connect(false);
        connect(true);
    }

    private static void connect(boolean auth) throws NamingException {
        final Hashtable<String, String> jndiProperties = new Hashtable<>();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "remote+http://localhost:8080");
        final Context context = new InitialContext(jndiProperties);

        final String moduleName = "custom-principal-ejb";
        final String beanName = auth ? PrincipalProviderEJBSecured.class.getSimpleName() : PrincipalProviderEJBUnsecured.class.getSimpleName();
        final String interfaceName = PrincipalProviderEJBInterface.class.getName();
        PrincipalProviderEJBInterface beanReference = (PrincipalProviderEJBInterface) context.lookup("ejb:/" + moduleName + "/"
                + beanName + "!" + interfaceName);

        System.out.println("\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
        System.out.println("Successfully called secured bean as " + (auth ? "authenticated user" : "unauthenticated user"));
        System.out.println("EJBContext records principal as " + beanReference.getEJBCallerPrincipal());

        long days = beanReference.daysSinceLogin();
        System.out.println("\nCustom principal usage test:");
        System.out.printf("[Via EJBContext] Last login %s. Secrets access is %s.\n", (days != -1 ? String.format("was %d days ago", days) : "is not applicable" ),
                                                                        (beanReference.secretsAccess() ? "enabled" : "disabled"));
        System.out.println("\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
    }
}
