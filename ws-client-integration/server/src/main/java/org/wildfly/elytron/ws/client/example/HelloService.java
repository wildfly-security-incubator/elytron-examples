package org.wildfly.elytron.ws.client.example;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.io.IOException;

/**
 * @author dvilkola@redhat.com
 */

@WebService
public interface HelloService {

    /**
     * Say hello as a response
     *
     * @return A simple hello world message
     */
    @WebMethod
    String sayHello();
}

