/*
 * Copyright 2021 Red Hat, Inc.
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
package manual.test.rest;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("ping")
@Stateless
public class Ping {

    @Resource
    private SessionContext sessionContext;

    @Resource
    private EJBContext ejbContext;

    @GET
    @RolesAllowed("Admin")
    public String ping() {
        return sessionContext == null ? "No session context! " :  "Hello " + sessionContext.getCallerPrincipal().getName() + "!";
    }

}
