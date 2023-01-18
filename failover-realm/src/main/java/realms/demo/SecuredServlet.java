 /*
  * JBoss, Home of Professional Open Source
  *
  * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package realms.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/secure")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "Admin" }))
public class SecuredServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        Principal principal = null;
        String authType = null;

        principal = req.getUserPrincipal();

        authType = req.getAuthType();

        writer.println("<html><head><title>servlet-security</title></head><body>");
        writer.println("<h1>" + "Successfully called Secured Servlet " + "</h1>");
        writer.println("<p>" + "Principal  : " + principal.getName() + "</p>");
        writer.println("<p>" + "Authentication Type : " + authType + "</p>");
        writer.println("</body></html>");
        writer.close();
    }

}
