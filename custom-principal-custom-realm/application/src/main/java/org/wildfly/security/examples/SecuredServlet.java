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
@ServletSecurity(httpMethodConstraints = { @HttpMethodConstraint(value = "GET", rolesAllowed = { "Users" }) })
public class SecuredServlet extends HttpServlet {

    private static final long serialVersionUID = -7255453900077536656L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter pw = resp.getWriter()) {
            pw.print(securedPage(req));
        }
    }

    private String securedPage(HttpServletRequest req) {
        String newline = System.getProperty("line.separator");
        CustomPrincipal customPrincipal = (CustomPrincipal) req.getUserPrincipal();

        return String.join(newline,
                "<!DOCTYPE html>",
                "<html>",
                "    <head><title>SecuredServlet - doGet()</title></head>",
                "    <body>",
                "        <h1>Custom Principal - Elytron Demo</h1>",
                "        <ul>",
  String.format("            <li>Principal name from <code>HttpServletRequest.getUserPrincipal()</code>: <strong>%s</strong>", req.getUserPrincipal()),
                "        </ul>",
                "        <p>Invoking a method from our custom principal class:</p>",
                "        <ul>",
  String.format("            <li>Calling <code>CustomPrincipal.getLastLoginTime()</code>: <strong>%s</strong></li>", customPrincipal.getLastLoginTime()),
                "        </ul>",
                "    </body>",
                "</html>"
        );
    }
}
