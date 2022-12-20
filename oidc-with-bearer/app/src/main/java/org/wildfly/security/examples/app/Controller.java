/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.examples.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.wildfly.security.http.oidc.OidcSecurityContext;

public class Controller {

    private static final String AUTHZ_HEADER = "Authorization";
    private static final String ENDPOINT_URL = "http://localhost:8090/service/";

    public boolean isLoggedIn(HttpServletRequest req) {
        return getOidcSecurityContext(req) != null;
    }

    public String getMessage(HttpServletRequest req) {
        String action = getAction(req);
        if (action.equals("")) return "";
        OidcSecurityContext oidcSecurityContext = getOidcSecurityContext(req);
        String target = System.getenv("SERVICE_URL");
        Invocation.Builder invocationBuilder = ClientBuilder.newClient().target(target == null ? ENDPOINT_URL : target).path(action).request();
        Response response;
        if (oidcSecurityContext != null) {
            String authzHeaderValue = "Bearer " + oidcSecurityContext.getTokenString();
            response = invocationBuilder.header(AUTHZ_HEADER, authzHeaderValue).get();
        } else {
            response = invocationBuilder.get();
        }
        String message;
        if (response.getStatus() == 200) {
            message = response.readEntity(String.class);
        } else {
            message = "<span class='error'>" + response.getStatus() + " " + response.getStatusInfo() + "</span>";
        }
        response.close();
        return message;
    }

    private OidcSecurityContext getOidcSecurityContext(HttpServletRequest req) {
        return (OidcSecurityContext) req.getAttribute(OidcSecurityContext.class.getName());
    }

    private String getAction(HttpServletRequest req) {
        if (req.getParameter("action") == null) return "";
        return req.getParameter("action");
    }
}
