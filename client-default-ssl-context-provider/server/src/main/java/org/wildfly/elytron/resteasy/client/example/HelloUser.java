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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Simple REST endpoint that you can access after accepting server's certificate and providing client's certificate.
 *
 * @author dvilkola@redhat.com
 */
@Path("/")
public class HelloUser {

    @GET
    @Path("/hello")
    public String hello() {
        return "Hello there!";
    }
}
