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

import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDateTime;

import org.wildfly.security.auth.principal.NamePrincipal;

/**
 * Custom implementation of a {@link Principal}, storing the timestamp when a user attempts to login,
 * and of the last time they logged in.
 *
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
public class CustomPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = -7725026682598731089L;
    private final LocalDateTime currentLoginTime;
    private final LocalDateTime lastLoginTime;
    private final Principal wrappedPrincipal;

    public CustomPrincipal(Principal principal, LocalDateTime lastLoginTime) {
        this.wrappedPrincipal = principal;
        this.currentLoginTime = LocalDateTime.now();
        this.lastLoginTime = lastLoginTime != null ? lastLoginTime : this.currentLoginTime;
    }

    // Used for rewriting names of existing principals
    CustomPrincipal(String name, LocalDateTime lastLoginTime, LocalDateTime currentLoginTime) {
        this.wrappedPrincipal = new NamePrincipal(name);
        this.currentLoginTime = currentLoginTime;
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCurrentLoginTime() {
        return this.currentLoginTime;
    }

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }
    @Override
    public String getName() {
        return wrappedPrincipal.getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof CustomPrincipal && this.equals((CustomPrincipal)object);
    }

    // Only compares names
    public boolean equals(final CustomPrincipal principal) {
        return principal.getName().equals(this.getName());
    }
}
