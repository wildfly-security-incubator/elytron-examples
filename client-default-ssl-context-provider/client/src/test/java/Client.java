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

import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.net.ssl.SSLContext;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.client.WildFlyElytronClientDefaultSSLContextProvider;

import jakarta.ws.rs.core.Response;

/**
 * Client test method demonstrating how WildFlyElytronClientDefaultSSLContextProvider can be used to register JVM wide default client SSLContext.
 *
 * @author dvilkola@redhat.com
 */
public class Client {

    @Test
    public void test() throws NoSuchAlgorithmException {
        Security.insertProviderAt(new WildFlyElytronClientDefaultSSLContextProvider("src/test/wildfly-config-two-way-tls.xml"), 1);
        ResteasyClient client = new ResteasyClientBuilderImpl().sslContext(SSLContext.getDefault()).hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY).build();
        Response response = client
                .target("https://127.0.0.1:8443/client-default-ssl-context-provider/rest/hello")
                .request().get();
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("Hello there!", response.readEntity(String.class));
        Assert.assertEquals(WildFlyElytronClientDefaultSSLContextProvider.ELYTRON_CLIENT_DEFAULT_SSL_CONTEXT_PROVIDER_NAME, SSLContext.getDefault().getProvider().getName());
        System.out.println("WildFly Elytron client has provided default SSLContext to trust the server");
    }
}
