/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.examples;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Landing page after RP-initiated logout ({@code post-logout-redirect-uri}).
 */
@WebServlet("/logged-out")
public class PostLogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println("<html>");
            writer.println("  <head><title>Logged out</title></head>");
            writer.println("  <body>");
            writer.println("    <h1>You are logged out</h1>");
            writer.println("    <p>Keycloak ended your SSO session and redirected the browser here ");
            writer.println("       (<code>post-logout-redirect-uri</code>).</p>");
            writer.println("    <p><a href=\"" + req.getContextPath() + "/secured\">Log in again</a></p>");
            writer.println("    <p><a href=\"" + req.getContextPath() + "/\">Home</a></p>");
            writer.println("  </body>");
            writer.println("</html>");
        }
    }

}
