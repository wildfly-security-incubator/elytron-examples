import org.jboss.as.quickstarts.wshelloworld.HelloWorldService;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {

    QName serviceName = new QName("http://www.jboss.org/eap/quickstarts/wshelloworld/HelloWorld", "HelloWorldService");

    @Test
    public void testHTTPS() {
        // it is necessary to set null or local copy of wsdl file if we are loading the WSDL from the same TLS secured server, see https://stackoverflow.com/a/11032471
        Service service = Service.create(null, serviceName);
        HelloWorldService helloWorldService = service.getPort(HelloWorldService.class);
        BindingProvider bindingProvider = (BindingProvider) helloWorldService;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://localhost:8443/helloworld-ws/HelloWorldService");

        CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
        cxfClientConfigurer.setConfigProperties(bindingProvider, null, null);

        Assert.assertEquals("bob", bindingProvider.getRequestContext().get(BindingProvider.USERNAME_PROPERTY));
        Assert.assertEquals("bobs_password", bindingProvider.getRequestContext().get(BindingProvider.PASSWORD_PROPERTY));
        Assert.assertEquals(helloWorldService.sayHello(), "Hello World!");
        Assert.assertEquals(helloWorldService.sayHelloToName("jane"), "Hello jane!");
    }

    @Test
    public void testHTTP() throws MalformedURLException {
        Service service = Service.create(new URL("http://localhost:8080/helloworld-ws/HelloWorldService?wsdl"), serviceName);
        HelloWorldService helloWorldService = service.getPort(HelloWorldService.class);
        BindingProvider bindingProvider = (BindingProvider) helloWorldService;

        CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
        cxfClientConfigurer.setConfigProperties(bindingProvider, null, null);

        Assert.assertEquals("bob", bindingProvider.getRequestContext().get(BindingProvider.USERNAME_PROPERTY));
        Assert.assertEquals("bobs_password", bindingProvider.getRequestContext().get(BindingProvider.PASSWORD_PROPERTY));
        Assert.assertEquals(helloWorldService.sayHello(), "Hello World!");
        Assert.assertEquals(helloWorldService.sayHelloToName("jane"), "Hello jane!");
    }
}
