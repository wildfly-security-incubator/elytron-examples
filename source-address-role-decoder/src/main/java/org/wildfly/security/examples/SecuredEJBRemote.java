package org.wildfly.security.examples;

/**
 * The interface used to access the SecuredEJB.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface SecuredEJBRemote {

    /**
     * @return A String containing the name of the current principal.
     */
    String getPrincipalName();

    boolean administrativeMethod();

}
