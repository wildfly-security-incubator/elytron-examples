/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022 Red Hat, Inc., and individual contributors
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

import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;

/**
 * An {@code EJBClientInterceptor} that will always runAs the current {@code SecurityIdentity}
 * to activate any outflow identities.
 *
 * @author <a href="mailto:fjuma@redhat.com">Farah Juma</a>
 */
public class IdentityEJBClientInterceptor implements EJBClientInterceptor {

    @Override
    public void handleInvocation(EJBClientInvocationContext context) throws Exception {
        SecurityDomain securityDomain = SecurityDomain.getCurrent();
        if (securityDomain != null) {
            SecurityIdentity identity = securityDomain.getCurrentSecurityIdentity();
            identity.runAsSupplierEx(() -> {
                context.sendRequest();
                return null;
            });
        } else {
            context.sendRequest();
        }
    }
    @Override
    public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
        return context.getResult();
    }

}

