/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.examples;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.security.auth.server.event.SecurityAuthenticationFailedEvent;
import org.wildfly.security.auth.server.event.SecurityAuthenticationSuccessfulEvent;
import org.wildfly.security.auth.server.event.SecurityEvent;

/**
 * Example of custom-security-event-listener
 *
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class MySecurityEventListener implements Consumer<SecurityEvent> {

    private String myAttribute;

    // receiving configuration from subsystem
    public void initialize(Map<String, String> configuration) {
        myAttribute = configuration.get("myAttribute");
        System.out.println("MySecurityEventListener initialized with myAttribute = " + myAttribute);
    }

    @Override
    public void accept(SecurityEvent securityEvent) {
        if (securityEvent instanceof SecurityAuthenticationSuccessfulEvent) {
            System.err.printf("Authenticated user \"%s\"\n", securityEvent.getSecurityIdentity().getPrincipal());
        } else if (securityEvent instanceof SecurityAuthenticationFailedEvent) {
            System.err.printf("Failed authentication as user \"%s\"\n", ((SecurityAuthenticationFailedEvent)securityEvent).getPrincipal());
        }
    }
}
