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
package org.wildfly.elytron.resteasy.client.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * Simple REST endpoint that will be used to demonstrate RESTEasy client integration with Elytron client.
 *
 * @author dvilkola@redhat.com
 */
@Path("/")
public class HelloUser {
    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/hello")
    public String hello() {
        return "Hello " + securityContext.getUserPrincipal().getName();
    }
}
