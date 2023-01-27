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

import static java.time.temporal.ChronoUnit.DAYS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.wildfly.security.auth.server.SecurityIdentity;

/**
 * A simple secured HTTP servlet.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
@WebServlet("/secured")
@ServletSecurity(httpMethodConstraints = { @HttpMethodConstraint(value = "GET", rolesAllowed = { "Login" }) })
public class SecuredServlet extends HttpServlet {

    private static final long serialVersionUID = -7255453900077536656L;
    @Inject
    SecurityContext securityContext;
    @Inject
    SecurityIdentity securityIdentity;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter pw = resp.getWriter()) {
            pw.print(securedPage(req));
        }
    }

    private String securedPage(HttpServletRequest req) {
        String newline = System.getProperty("line.separator");
        CustomPrincipal customPrincipalFromType = retrieveCustomPrincipalByType();

        return String.join(newline,
                "<!DOCTYPE html>",
                "<html>",
                "    <head><title>SecuredServlet - doGet()</title></head>",
                "    <body>",
                "        <h1>Custom Principal - Elytron Demo</h1>",
                "        <p>For reference, transform sequence is quickstartUser -> <em>customQuickstartUserPre</em> -> customQuickstartUserPost.</p>",
                "        <p>Injection check - these values should match:</p>",
                "        <ul>",
  String.format("            <li>Identity as available from Jakarta Security (<code>SecurityContext</code>): <strong>%s</strong></li>", securityContext.getCallerPrincipal().getName()),
  String.format("            <li>Identity as available from injection (<code>SecurityIdentity</code>): <strong>%s</strong></li>", securityIdentity.getPrincipal().getName()),
  String.format("            <li>Identity as provided in HTTPServletRequest: <strong>%s</strong>", req.getUserPrincipal()),
                "        </ul>",
                "        <p>Custom Principal check - these values should match:</p>",
                "        <ul>",
  String.format("            <li>Caller principal<code>(SecurityContext.getCallerPrincipal)</code>: Class -> <strong>%s</strong>, Name -> <strong>%s</strong></li>",
                        securityContext.getCallerPrincipal().getClass().getCanonicalName(),
                        securityContext.getCallerPrincipal().getName()),
                customPrincipalFromType != null
                        ? String.format("            <li>Custom principal<code>(SecurityContext.getPrincipalsByType)</code>: Class -> <strong>%s</strong>, Name -> <strong>%s</strong></li>",
                                customPrincipalFromType.getClass().getCanonicalName(),
                                customPrincipalFromType.getName())
                        : String.format("            <li>Custom principal<code>(SecurityContext.getPrincipalsByType)</code>: Class -> <strong>%s</strong>, Name -> <strong>%s</strong></li>",
                                "not found",
                                "null"),
  String.format("            <li>Injection<code>(SecurityIdentity.getPrincipal)</code>: Class -> <strong>%s</strong>, Name -> <strong>%s</strong></li>",
                        securityIdentity.getPrincipal().getClass().getCanonicalName(),
                        securityIdentity.getPrincipal().getName()),
                "        </ul>",
                "        <p>Custom Principal usage - this check will return a result of <em>enabled</em> if the CustomPrincipal was injected properly:</p>",
                "        <ul>",
                customPrincipalFromType != null
                        ? generateSecretsAccessString("SecurityContext", daysSinceLogin(customPrincipalFromType))
                        : "        <li>The custom principal was not loaded from SecurityContext. Secrets access is <strong>disabled</strong>.</li>",
                securityIdentity.getPrincipal().getClass() == CustomPrincipal.class
                        ? generateSecretsAccessString("SecurityIdentity", daysSinceLogin((CustomPrincipal) securityIdentity.getPrincipal()))
                        : "        <li>The custom principal was not loaded from SecurityIdentity. Secrets access is <strong>disabled</strong>.</li>",
                "    </body>",
                "</html>"
        );
    }

    private CustomPrincipal retrieveCustomPrincipalByType() {
        Set<CustomPrincipal> customPrincipals = securityContext.getPrincipalsByType(CustomPrincipal.class);
        return customPrincipals.size() == 1 ? customPrincipals.iterator().next() : null;
    }

    /** @param principalProvider Name of the class or provider that returned the custom principal. */
    private String generateSecretsAccessString(String principalProvider, long days) {
        return String.format("        <li>%s reports last login <strong>%s</strong>. Secrets access is <strong>%s</strong>.</li>",
                principalProvider,
                (days != -1 ? String.format("was %d days ago", days) : "is not applicable"),
                (days < 30 ? "enabled" : "disabled"));
    }

    public long daysSinceLogin(CustomPrincipal customPrincipal) {
        if (customPrincipal != null) {
            if (customPrincipal.getLastLoginTime().equals(customPrincipal.getCurrentLoginTime())) {
                return -1;
            } else {
                return DAYS.between(customPrincipal.getLastLoginTime(), customPrincipal.getCurrentLoginTime());
            }
        } else return -1;
    }
}
