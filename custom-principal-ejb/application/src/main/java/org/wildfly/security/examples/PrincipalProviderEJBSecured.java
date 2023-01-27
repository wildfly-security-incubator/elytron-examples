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

import static java.time.temporal.ChronoUnit.DAYS;

import java.security.Principal;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * Simple secured EJB using EJB security annotations.
 *
 * @author Sherif Makary
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
@Stateless
@Remote(PrincipalProviderEJBInterface.class)
@RolesAllowed({ "Login" })
@SecurityDomain("principalEJB")
public class PrincipalProviderEJBSecured implements PrincipalProviderEJBInterface {

    // Inject the Session Context
    @Resource
    private SessionContext ejbCtx;

    /**
     * @return the authenticated principal
     */
    @Override
    public Principal getEJBCallerPrincipal() {
        // Session context injected using the resource annotation
        return ejbCtx.getCallerPrincipal();
    }

    @Override
    public long daysSinceLogin() {
        CustomPrincipal principal = (CustomPrincipal) getEJBCallerPrincipal();
        if (principal.getLastLoginTime().equals(principal.getCurrentLoginTime())) {
            return -1;
        } else {
            return DAYS.between(principal.getLastLoginTime(), principal.getCurrentLoginTime());
        }
    }

    @Override
    public boolean secretsAccess() {
        return daysSinceLogin() <= 30;
    }
}
