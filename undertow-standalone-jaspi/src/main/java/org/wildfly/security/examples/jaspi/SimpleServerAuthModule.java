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

package org.wildfly.security.examples.jaspi;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A simple {@link ServerAuthModule} implementation.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class SimpleServerAuthModule implements ServerAuthModule {

    private static final String USERNAME_HEADER = "X-USERNAME";
    private static final String PASSWORD_HEADER = "X-PASSWORD";
    private static final String MESSAGE_HEADER = "X-MESSAGE";

    private CallbackHandler callbackHandler;

    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options)
            throws AuthException {
        System.out.println("SimpleServerAuthModule initialise");
        this.callbackHandler = handle("handler", handler);
    }

    public Class<?>[] getSupportedMessageTypes() {
        System.out.println("SimpleServerAuthModule getSupportedMessageTypes");
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        System.out.println("validateRequest");
        handle("messageInfo", messageInfo);
        handle("clientSubject", clientSubject);
        handle("serviceSubject", serviceSubject);

        HttpServletRequest request =  (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        final String username = request.getHeader(USERNAME_HEADER);
        final String password = request.getHeader(PASSWORD_HEADER);

        if (username == null || username.length() == 0 || password == null || password.length() == 0) {
            response.addHeader(MESSAGE_HEADER, "Please resubmit the request with a username specified using the X-USERNAME and a password specified using the X-PASSWORD header.");
            response.setStatus(401);
            return AuthStatus.SEND_FAILURE;
        }

        try {
            PasswordValidationCallback pvc = new PasswordValidationCallback(clientSubject, username, password.toCharArray());
            callbackHandler.handle(new Callback[] { pvc, new GroupPrincipalCallback(clientSubject, new String[] {"Users"})});
            if (pvc.getResult()) {
                return AuthStatus.SUCCESS;
            }

            response.addHeader(MESSAGE_HEADER, "Validation of the supplied username and password failed, GO AWAY!!");
            response.setStatus(401);
            return AuthStatus.SEND_FAILURE;
        } catch (IOException | UnsupportedCallbackException e) {
            e.printStackTrace();
            return AuthStatus.SEND_FAILURE;
        }
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        System.out.println("cleanSubject");
    }

    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        System.out.println("secureResponse");
        return AuthStatus.SEND_SUCCESS;
    }

    private static <T> T handle(final String name, final T value) {
        System.out.println(String.format("'%s' is %s", name, value == null ? "null" : "not null"));

        return value;
    }

}
