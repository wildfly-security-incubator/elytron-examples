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

import java.security.Principal;

/**
 * The interface used to access the SecuredEJB.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
public interface PrincipalProviderEJBInterface {

    /**
     * @return the caller principal from {@link jakarta.ejb.EJBContext}.
     */
    Principal getEJBCallerPrincipal();

    /**
     * @return the number of days since the last login, or -1 if unavailable.
     */
    long daysSinceLogin();

    /**
     * @return whether the authenticated user is permitted to access secrets. If it has been longer than 30 days since
     * the last login, secrets access is disabled.
     */
    boolean secretsAccess();
}
