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
import java.util.function.Function;

/**
 * A transformer to change the name of the {@link CustomPrincipal} as a final step.
 *
 * @author <a href="mailto:carodrig@redhat.com">Cameron Rodriguez</a>
 */
public class CustomFinalTransformer implements Function<Principal, Principal> {

    @Override
    public Principal apply(Principal principal) {
        return CustomPreRealmTransformer.renameCustomPrincipal(principal, "QuickstartUserPost",
                "QuickstartUserFinal", false);
    }
}
