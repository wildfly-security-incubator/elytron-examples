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

import java.security.Principal;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * Simple secured EJB using EJB security annotations
 *
 * @author Sherif Makary
 *
 */
/**
 *
 * Annotate this EJB for authorization. Allow only those in the "guest" role. For EJB authorization, you must also specify the
 * security domain. This example uses the "other" security domain which is provided by default in the standalone.xml file.
 *
 */
@Stateless
@Remote(SecuredEJBRemote.class)
@RolesAllowed({ "Guest" })
@SecurityDomain("other")
public class SecuredEJB implements SecuredEJBRemote {

    // Inject the Session Context
    @Resource
    private SessionContext ctx;

    /**
     * Secured EJB method using security annotations
     */
    public String getSecurityInfo() {
        // Session context injected using the resource annotation
        Principal principal = ctx.getCallerPrincipal();
        return principal.toString();
    }

    @RolesAllowed("Admin")
    public boolean administrativeMethod() {
        return true;
    }
}