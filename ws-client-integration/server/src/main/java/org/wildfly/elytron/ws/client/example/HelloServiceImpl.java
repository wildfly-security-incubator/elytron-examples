package org.wildfly.elytron.ws.client.example;

import org.wildfly.security.auth.server.SecurityDomain;


import javax.jws.WebService;

/**
 * The implementation of the HelloService JAX-WS Web Service.
 */
@WebService(serviceName = "HelloService", portName = "HelloService", name = "HelloService")
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello() {
        String username = "unknown";
        SecurityDomain secDom = SecurityDomain.getCurrent();
        if (secDom != null) {
            username = secDom.getCurrentSecurityIdentity().getPrincipal().getName();
        }
        return "Hello " + username + "!";
    }

}
