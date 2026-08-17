import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.wildfly.elytron.ws.client.example.HelloService;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dvilkola@redhat.com
 */
public class Client {

    @Test
    public void test() {
        QName serviceName = new QName("HelloService");
        Service service = Service.create(serviceName);
        HelloService helloService = service.getPort(HelloService.class);
        BindingProvider bindingProvider = (BindingProvider) helloService;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://localhost:8443/ws-client-integration/HelloService");
        CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
        cxfClientConfigurer.setConfigProperties(bindingProvider, null, null);
        Assert.assertNotNull(helloService);
        Assert.assertEquals("Hello jane!", helloService.sayHello());
        System.out.println("\n\nYou have successfully used SSLContext and credentials from Elytron client to authenticate as Jane to HelloService JAX-WS Web Service.\n\n");
    }
}
