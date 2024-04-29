/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.wildfly.security.http.oidc.OidcSecurityContext;
import org.wildfly.security.http.oidc.RefreshableOidcSecurityContext;

/**
 * A simple secured HTTP servlet.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@WebServlet("/secured")
public class SecuredServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OidcSecurityContext oidcSecurityContext = getOidcSecurityContext(req);

        List<String> scopeClaims = new ArrayList<String>();
        if (oidcSecurityContext != null) {
            String scope = ((RefreshableOidcSecurityContext) oidcSecurityContext).getOidcClientConfiguration().getScope();
            displayScopeClaims(scope, scopeClaims, oidcSecurityContext);
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.println("<html>");
            writer.println("  <head><title>Secured Servlet</title></head>");
            writer.println("  <body>");
            writer.println("    <h1>Secured Servlet</h1>");
            writer.println("    <p>");
            writer.print(" Current Principal '");
            Principal user = req.getUserPrincipal();
            writer.print(user != null ? user.getName() : "NO AUTHENTICATED USER");
            writer.println("'");
            writer.println("    </p>");
            if(scopeClaims.isEmpty()) {
                writer.println("<p>");
                writer.println("No additional claims were recovered using the scope values.\n" );
                writer.println("    </p>");            }
            else{
                writer.println("<p>");
                writer.println("Claims received using additional scope values: \n" );
                writer.println("    </p>");
                for (String value : scopeClaims) {
                    writer.println("<p>");
                    writer.println(value);
                    writer.println("    </p>");
                }
            }
            writer.println("  </body>");
            writer.println("</html>");
        }
    }

    private OidcSecurityContext getOidcSecurityContext(HttpServletRequest req) {
        return (OidcSecurityContext) req.getAttribute(OidcSecurityContext.class.getName());
    }

    private void displayScopeClaims(String scope, List<String> message, OidcSecurityContext oidcSecurityContext) {
        if (scope == null){
            return;
        }
        if (scope.contains("profile")){
            message.add("By configuring the \"profile\" scope, the \"given_name\" and \"family_name\" claims are present in the access token and have values : " + oidcSecurityContext.getToken().getClaimValueAsString("given_name") + " and "
                    + oidcSecurityContext.getToken().getClaimValueAsString("family_name"));
        }
        if (scope.contains("email")){
            message.add("By configuring the \"email\" scope, the \"email_verified\" claim is present in the access token and has value : " + oidcSecurityContext.getToken().getClaimValueAsString("email_verified") + "\n");
        }
        if (scope.contains("roles")){
            message.add("By configuring the \"roles\" scope, the \"realm_access\" claim is present in the access token and has value : " + oidcSecurityContext.getToken().getClaimValueAsString("realm_access") + "\n");
        }
        if (scope.contains("acr")){ //authentication context class reference scope
            message.add("By configuring the \"acr\" scope, the \"acr\" claim is present in the access token and has value : " + oidcSecurityContext.getToken().getClaimValueAsString("acr") + "\n");
        }
        if (scope.contains("microprofile-jwt")){
            message.add("By configuring the \"microprofile-jwt\" scope, the \"groups\" claim is present in the access token and has value : " + oidcSecurityContext.getToken().getClaimValueAsString("groups") + "\n");
        }
        if (scope.contains("web-origins")){
            message.add("By configuring the \"web-origins\" scope, the \"allowed-origins\" claim is present in the access token and has value : " + oidcSecurityContext.getToken().getClaimValueAsString("allowed-origins") + "\n");
        }
    }
}
