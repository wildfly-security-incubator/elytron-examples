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

package org.wildfly.security.examples.ejb_same_domain.ejb;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.Resource;

import org.jboss.ejb3.annotation.SecurityDomain;
import jakarta.ejb.SessionContext;

@Stateful
@Remote(WhoAmI.class)
@SecurityDomain("same-virtual-domain.ear")
public class WhoAmIBean implements WhoAmI {

    @Resource
    private SessionContext sessionContext;

    @Override
    public String whoAmI() {
        String callerPrincipal = sessionContext.getCallerPrincipal().getName();
        boolean isCallerUser = sessionContext.isCallerInRole("User");
        boolean isCallerAdmin = sessionContext.isCallerInRole("Admin");
        return "Principal : " + callerPrincipal + "<br/><br/>Caller Has Role 'User'=" + String.valueOf(isCallerUser) + "<br/><br/>Caller Has Role 'Admin'=" + String.valueOf(isCallerAdmin);
    }
}
