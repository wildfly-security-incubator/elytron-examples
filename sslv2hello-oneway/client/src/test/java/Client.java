/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.security.Security;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * RESTEasy Client testing successful connection using SSLv2Hello
 *
 * @author Sonia Zaldana Calles
 */
public class Client {

    public static String disabledAlgorithms;

    @BeforeClass
    public static void init() {
        disabledAlgorithms = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (disabledAlgorithms != null && (disabledAlgorithms.contains("TLSv1") || disabledAlgorithms.contains("TLSv1.1"))) {
            // reset the disabled algorithms to make sure that the protocols required in this test are available
            Security.setProperty("jdk.tls.disabledAlgorithms", "");
        }
    }

    @AfterClass
    public static void tearDown() {
        if (disabledAlgorithms != null) {
            Security.setProperty("jdk.tls.disabledAlgorithms", disabledAlgorithms);
        }
    }

    @Test
    public void test() {
        ResteasyClient client = new ResteasyClientBuilder().hostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        Response response = client.target("https://127.0.0.1:8443/server-sslv2hello/HelloWorld").request().get();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200);
    }
}
