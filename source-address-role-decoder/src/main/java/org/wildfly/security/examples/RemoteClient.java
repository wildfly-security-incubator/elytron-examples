package org.wildfly.security.examples;

import java.util.Hashtable;

import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;

public class RemoteClient {

    public static void main(String[] args) throws Exception {

        final Hashtable<String, String> jndiProperties = new Hashtable<>();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "remote+http://localhost:8080");
        final Context context = new InitialContext(jndiProperties);

        SecuredEJBRemote reference = (SecuredEJBRemote) context.lookup("ejb:/source-address-role-decoder/SecuredEJB!"
                + SecuredEJBRemote.class.getName());

        System.out.println("\n\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n");
        System.out.println("Successfully called secured bean, caller principal " + reference.getPrincipalName());
        boolean hasAdminPermission = false;
        try {
            hasAdminPermission = reference.administrativeMethod();
        } catch (EJBAccessException e) {
        }
        System.out.println("\nPrincipal has admin permission: " + hasAdminPermission);
        System.out.println("\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n\n");
    }

}
