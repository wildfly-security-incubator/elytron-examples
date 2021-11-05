/*
 * Copyright 2021 Red Hat, Inc.
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

package org.wildfly.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.bind.annotation.JsonbTransient;

import org.wildfly.common.iteration.CodePointIterator;

/**
 * A raw representation of an identity for use with JSONB.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class RawHotpIdentity {

    private String name;

    private String hotpSecret;
    private long lastHotpCount;

    private List<String> groups;

    public RawHotpIdentity() {
    }

    RawHotpIdentity(String name, String hotpSecret, long lastHotpCount, List<String> groups) {
        this.name = name;
        this.hotpSecret = hotpSecret;
        this.lastHotpCount = lastHotpCount;
        this.groups = new ArrayList<>(groups);
    }

    RawHotpIdentity(String name, String hotpSecret) {
        this(name, hotpSecret, 0, Collections.singletonList("Users"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastHotpCount() {
        return lastHotpCount;
    }

    public void setLastHotpCount(long hotpCount) {
        this.lastHotpCount = hotpCount;
    }

    @JsonbTransient
    public byte[] getRawHotpSecret() {
        return CodePointIterator.ofString(hotpSecret).base32Decode().drain();
    }

    public String getHotpSecret() {
        return hotpSecret;
    }

    public void setHotpSecret(String hotpSecret) {
        this.hotpSecret = hotpSecret;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }


}
