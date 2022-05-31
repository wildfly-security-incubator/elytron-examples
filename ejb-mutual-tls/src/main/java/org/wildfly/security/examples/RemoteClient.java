/*
 * Copyright 2022 Red Hat, Inc.
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

import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * The remote client responsible for making invoking the intermediate bean to demonstrate security context propagation
 * in EJB to remote EJB calls.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RemoteClient {

    public static void main(String[] args) throws Exception {

        final Hashtable<String, String> jndiProperties = new Hashtable<>();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "remote+https://localhost:8443");
        final Context context = new InitialContext(jndiProperties);

        final String moduleName = "ejb-mutual-tls";
        final String beanName = SecuredEJB.class.getSimpleName();
        final String interfaceName = SecuredEJBMutualTLS.class.getName();
        SecuredEJBMutualTLS beanReference = (SecuredEJBMutualTLS) context.lookup("ejb:/" + moduleName + "/"
                + beanName + "!" + interfaceName);

        System.out.println("\n\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n");
        System.out.println("Successfully called secured bean, caller principal " + beanReference.getSecurityInfo());
        boolean hasAdminPermission = false;
        try {
            hasAdminPermission = beanReference.administrativeMethod();
        } catch (EJBAccessException ignored) {}
        System.out.println("\nPrincipal has admin permission: " + hasAdminPermission);
        System.out.println("\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n\n");
    }
}
