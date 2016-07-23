package ca.mestevens.java.bundle.integration;

import ca.mestevens.java.IntegrationTest;
import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.dropwizard.TestApplication;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

@Category(IntegrationTest.class)
public class BundleIntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<TypesafeConfiguration> dropwizardAppRule
            = new DropwizardAppRule<>(TestApplication.class, "test.conf");

    private static Client client;

    @BeforeClass
    public static void setUp() {
        client = new JerseyClientBuilder(dropwizardAppRule.getEnvironment()).build("Test Client");
    }

    @Test
    public void bundleSetUpSuccessfully() {

        final int port = dropwizardAppRule.getLocalPort();
        Assert.assertEquals(8765, port);

        final Response response = client
                .target(String.format("http://localhost:%d/test", port))
                .request()
                .get();

        Assert.assertEquals(200, response.getStatus());
    }

}
