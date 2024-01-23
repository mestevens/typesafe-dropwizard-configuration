package ca.mestevens.java.bundle.integration;

import ca.mestevens.java.IntegrationTest;
import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.dropwizard.TestApplication;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

@Category(IntegrationTest.class)
public class BundleIntegrationTest {

    public static final DropwizardTestSupport<TypesafeConfiguration> dropwizardTestSupport
            = new DropwizardTestSupport<TypesafeConfiguration>(TestApplication.class, "test.conf");

    private static Client client;

    @BeforeClass
    public static void setUp() {
        client = new JerseyClientBuilder(dropwizardTestSupport.getEnvironment()).build("Test Client");
    }

    @Test
    public void bundleSetUpSuccessfully() {

        final int port = dropwizardTestSupport.getLocalPort();
        Assert.assertEquals(8765, port);

        final Response response = client
                .target(String.format("http://localhost:%d/test", port))
                .request()
                .get();

        Assert.assertEquals(200, response.getStatus());
    }

}
