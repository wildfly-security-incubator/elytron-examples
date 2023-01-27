/*
 * Copyright 2023 Red Hat, Inc.
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.BasicAuthenticationCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.CredentialValidationResult.Status;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A simple {@link HttpAuthenticationMechanism} for testing.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
@ApplicationScoped
public class CustomAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String BASIC_REQUEST_HEADER = "Authorization";
    private static final String BASIC_RESPONSE_HEADER = "WWW-Authenticate";
    private static final String BASIC_RESPONSE = "Basic realm=\"principalRealm\"";

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response,
                                                HttpMessageContext httpMessageContext) throws AuthenticationException {

        String basicHeader = request.getHeader(BASIC_REQUEST_HEADER);

        if(basicHeader != null) {
            BasicAuthenticationCredential credential = new BasicAuthenticationCredential(basicHeader.substring(6));
            CredentialValidationResult cvr = identityStoreHandler.validate(credential);

            if (cvr.getStatus() == Status.VALID) {
                return httpMessageContext.notifyContainerAboutLogin(
                        new CustomTransformer().apply(cvr.getCallerPrincipal()),
                        cvr.getCallerGroups()
                );
            } else {
                return challenge(response, httpMessageContext);
            }
        }

        return challenge(response, httpMessageContext);
    }

    private static AuthenticationStatus challenge(HttpServletResponse response, HttpMessageContext httpMessageContext) {
        response.addHeader(BASIC_RESPONSE_HEADER, BASIC_RESPONSE);

        return httpMessageContext.responseUnauthorized();
    }
}

