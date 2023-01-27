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

import java.util.regex.Pattern;

import org.wildfly.extension.elytron.capabilities.PrincipalTransformer;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.util.RegexNameRewriter;

public class CustomNameRewriter {

    /**
     * Acts like {@link RegexNameRewriter}, but operates on both {@link CustomPrincipal} and other types of
     * {@link java.security.Principal Principal} with a name set.
     *
     * @return a {@link PrincipalTransformer}. It will return {@link CustomPrincipal} if the input was the same type,
     * otherwise {@link NamePrincipal} if there was a valid name for the Principal, and if not then the original principal.
     */
    static PrincipalTransformer asPrincipalTransformer(String pattern, String replacement, boolean replaceAll) {
        return PrincipalTransformer.from(
            principal -> {
                if (principal == null) {
                    return null;
                } else if (principal.getName() != null){
                    RegexNameRewriter regexRewriter = new RegexNameRewriter(Pattern.compile(pattern), replacement, replaceAll);
                    String rewritten = regexRewriter.rewriteName(principal.getName());

                    if (principal instanceof CustomPrincipal) {
                        CustomPrincipal cPrincipal = (CustomPrincipal) principal;
                        return rewritten == null ? null : new CustomPrincipal(rewritten,
                                cPrincipal.getLastLoginTime(), cPrincipal.getCurrentLoginTime());
                    } else { // CustomPrincipal not being used
                        return rewritten == null ? null : new NamePrincipal(rewritten);
                    }
                } else return principal;
            }
        );
    }
}
