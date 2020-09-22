import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * Client test method demonstrating RESTEasy client integration with Elytron client.
 *
 * @author dvilkola@redhat.com
 */
public class Client {

    @Test
    public void test() {
        ResteasyClient client = new ResteasyClientBuilder().hostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        Response response = client.target("https://127.0.0.1:8443/resteasy-client-integration-example/rest/hello").request().get();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.readEntity(String.class), "Hello jane");
        System.out.println("\n\nRESTEasy client have successfully used SSLContext and credentials from Elytron client to authenticate.\n\n");
    }
}
