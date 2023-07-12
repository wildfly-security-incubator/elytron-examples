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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * A {@link PrincipalTransformer} to create a custom principal. An offset {@value LOGIN_DELTA_DAYS LOGIN_DELTA_DAYS} is used to simulate the time since the last
 * user was logged in.
 *
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
public class CustomPreRealmTransformer implements Function<Principal, Principal> {

    private static final int LOGIN_DELTA_DAYS = 10;

    public Principal apply(Principal principal) {
        return new CustomPrincipal(principal, LocalDateTime.now().minusDays(LOGIN_DELTA_DAYS));
    }

}
