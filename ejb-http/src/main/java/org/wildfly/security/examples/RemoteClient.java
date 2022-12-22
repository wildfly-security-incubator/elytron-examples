/*
 * Copyright 2020 Red Hat, Inc.
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

import jakarta.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;

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
        jndiProperties.put(Context.PROVIDER_URL, "http://localhost:8080/wildfly-services");
        final Context context = new InitialContext(jndiProperties);

        // This is the module name of the deployed EJBs on the server. This is typically the jar name of the
        // EJB deployment, without the .jar suffix, but can be overridden via the ejb-jar.xml
        final String moduleName = "ejb-http";
        // The EJB name which by default is the simple class name of the bean implementation class
        final String beanName = SecuredEJB.class.getSimpleName();
        // the remote view fully qualified class name
        final String viewClassName = SecuredEJBHttp.class.getName();
        // let's do the lookup
        SecuredEJBHttp reference = (SecuredEJBHttp) context.lookup("ejb:/" + moduleName + "/" + beanName + "!" + viewClassName);

        System.out.println("\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n\n");
        System.out.println("Successfully called secured bean, caller principal " + reference.getSecurityInfo());
        boolean hasAdminPermission = false;
        try {
            hasAdminPermission = reference.administrativeMethod();
        } catch (EJBAccessException e) {
        }
        System.out.println("\nPrincipal has admin permission: " + hasAdminPermission);
        System.out.println("\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n\n");
    }

}