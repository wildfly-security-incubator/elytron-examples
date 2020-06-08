package org.wildfly.security.examples;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBContext;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

@Stateless
@Remote(SecuredEJBRemote.class)
@RolesAllowed({ "employee" })
@SecurityDomain("other")
public class SecuredEJB implements SecuredEJBRemote {

    @Resource
    private EJBContext context;

    public String getPrincipalName() {
        return context.getCallerPrincipal().getName();
    }

    @RolesAllowed("admin")
    public boolean administrativeMethod() {
        return true;
    }
}
