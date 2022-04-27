import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.client.WildFlyElytronClientDefaultSSLContextProvider;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * Client test method demonstrating how WildFlyElytronClientDefaultSSLContextProvider can be used to register JVM wide default client SSLContext.
 *
 * @author dvilkola@redhat.com
 */
public class Client {

    @Test
    public void test() throws NoSuchAlgorithmException {
        Security.insertProviderAt(new WildFlyElytronClientDefaultSSLContextProvider("src/test/wildfly-config-two-way-tls.xml"), 1);
        ResteasyClient client = new ResteasyClientBuilder().sslContext(SSLContext.getDefault()).hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY).build();
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
