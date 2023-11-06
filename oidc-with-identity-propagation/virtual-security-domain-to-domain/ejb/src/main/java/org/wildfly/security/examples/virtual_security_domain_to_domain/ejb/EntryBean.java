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

package org.wildfly.security.examples.virtual_security_domain_to_domain.ejb;

import java.security.Principal;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.wildfly.security.examples.ejb_basic.ejb.Management;


/**
 * A simple EJB that can be called to obtain the current caller principal and to check the role membership for
 * that principal.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@Stateless
public class EntryBean {

    @Resource
    private SessionContext sessionContext;

    @PermitAll
    public Principal getCallerPrincipal() {
        return sessionContext.getCallerPrincipal();
    }

    @PermitAll
    public boolean userHasRole(final String roleName) {
        return sessionContext.isCallerInRole(roleName);
    }

    @PermitAll
    public String invokeManagementBean() {
        Management management = lookup(Management.class, "java:global/virtual-security-domain-to-domain/ejb-basic-ejb/ManagementBean!org.wildfly.security.examples.ejb_basic.ejb.Management");
        return management.adminOnlyMethod();
    }



    public static <T> T lookup(Class<T> clazz, String jndiName) {
        Object bean = lookup(jndiName);
        return clazz.cast(bean);
    }

    private static Object lookup(String jndiName) {
        Context context = null;
        try {
            context = new InitialContext();
            return context.lookup(jndiName);
        } catch (NamingException ex) {
            throw new IllegalStateException("Lookup failed ", ex);
        } finally {
            try {
                context.close();
            } catch (NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

}
