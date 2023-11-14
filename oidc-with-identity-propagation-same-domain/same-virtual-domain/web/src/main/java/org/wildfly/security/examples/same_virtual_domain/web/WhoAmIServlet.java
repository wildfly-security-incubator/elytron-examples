/*
 * JBoss, Home of Professional Open Source
 * Copyright 2023, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.examples.same_virtual_domain.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.wildfly.security.examples.same_virtual_domain.ejb.EntryBean;

/**
 * A simple secured servlet that will show information about the current authenticated identity and also information about the
 * representation of the identity as it calls an EJB.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@SuppressWarnings("serial")
@WebServlet("/secured")
public class WhoAmIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EntryBean bean = lookup(EntryBean.class, "java:global/same-virtual-domain/same-virtual-domain-ejb/EntryBean");
        final PrintWriter writer = resp.getWriter();

        writer.println("<html><head><title>same-virtual-domain</title></head><body>");
        writer.println("<h1>Successfully logged into Secured Servlet with OIDC</h1>");
        writer.println("<h2>Identity as visible to servlet.</h2>");
        writer.println(String.format("<p>Principal  : %s</p>", req.getUserPrincipal().getName()));
        writer.println(String.format("<p>Authentication Type : %s</p>", req.getAuthType()));

        writer.println(String.format("<p>Caller Has Role '%s'=%b</p>", "User", req.isUserInRole("User")));
        writer.println(String.format("<p>Caller Has Role '%s'=%b</p>", "Admin", req.isUserInRole("Admin")));

        writer.println("<h2>Identity as visible to EntryBean.</h2>");


        writer.println(String.format("<p>Principal  : %s</p>", bean.getCallerPrincipal().getName()));
        writer.println(String.format("<p>Caller Has Role '%s'=%b</p>", "User", req.isUserInRole("User")));
        writer.println(String.format("<p>Caller Has Role '%s'=%b</p>", "Admin", bean.userHasRole("Admin")));

        writer.println("<h2>Identity as visible to ManagementBean.</h2>");
        writer.println(String.format("<p>%s</p>", bean.invokeWhoAmIBean()));

        writer.println("</body></html>");
        writer.close();
    }

    public static <T> T lookup(Class<T> clazz, String jndiName) {
        Object bean = lookup(jndiName);
        return clazz.cast(bean);
    }

    private static Object lookup(String jndiName) {
        Context context = null;
        try {
            context = new InitialContext();
            return context.lookup(jndiName);
        } catch (NamingException ex) {
            throw new IllegalStateException("Lookup failed", ex);
        } finally {
            try {
                context.close();
            } catch (NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
