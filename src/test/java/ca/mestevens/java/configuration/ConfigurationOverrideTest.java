package ca.mestevens.java.configuration;

import ca.mestevens.java.UnitTest;
import ca.mestevens.java.configuration.factory.TypesafeConfigurationFactory;
import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.typesafe.config.ConfigFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.LogbackModule;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.server.DefaultServerFactory;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Map;

@Category(UnitTest.class)
public class ConfigurationOverrideTest {

    private ObjectMapper objectMapper;
    private TypesafeConfigurationFactory typesafeConfiguration;

    @Before
    public void setUp() {
        // Clear the environment variables
        System.clearProperty("logging.level");
        System.clearProperty("ENV");
        this.objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .registerModule(new LogbackModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver());
    }

    @Test
    @SneakyThrows
    public void testFileOverrides() {
        System.setProperty("ENV", "test");
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, null);


        final TypesafeConfiguration configuration = typesafeConfiguration.build();

        //Server
        final DefaultServerFactory defaultServerFactory = (DefaultServerFactory) configuration.getServerFactory();
        final List<ConnectorFactory> connectorFactories = defaultServerFactory.getApplicationConnectors();
        Assert.assertEquals(1, connectorFactories.size());
        Assert.assertTrue(connectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 4433));

        final List<ConnectorFactory> adminConnectorFactories = defaultServerFactory.getAdminConnectors();
        Assert.assertEquals(1, adminConnectorFactories.size());
        Assert.assertTrue(adminConnectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 9191));

        // Check logging
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) configuration.getLoggingFactory();
        final Map<String, JsonNode> loggers = defaultLoggingFactory.getLoggers();
        Assert.assertEquals(5, loggers.size());
        Assert.assertTrue(loggers.keySet().contains("io.dropwizard"));
        Assert.assertEquals("test", loggers.get("io.dropwizard").asText());
    }

    @Test
    @SneakyThrows
    public void testEnvironmentOverrides() {
        System.setProperty("ENV", "test");
        System.setProperty("logging.level", "TRACE");
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, null);

        final TypesafeConfiguration configuration = typesafeConfiguration.build();

        //Server
        final DefaultServerFactory defaultServerFactory = (DefaultServerFactory) configuration.getServerFactory();
        final List<ConnectorFactory> connectorFactories = defaultServerFactory.getApplicationConnectors();
        Assert.assertEquals(1, connectorFactories.size());
        Assert.assertTrue(connectorFactories
                .stream()
                .map(connectorFactory -> (HttpsConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 4433));

        final List<ConnectorFactory> adminConnectorFactories = defaultServerFactory.getAdminConnectors();
        Assert.assertEquals(1, adminConnectorFactories.size());
        Assert.assertTrue(adminConnectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 9191));

        // Check logging
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) configuration.getLoggingFactory();
        final Map<String, JsonNode> loggers = defaultLoggingFactory.getLoggers();
        Assert.assertEquals(5, loggers.size());
        Assert.assertTrue(loggers.keySet().contains("io.dropwizard"));
        Assert.assertEquals("test", loggers.get("io.dropwizard").asText());
        Assert.assertEquals(Level.TRACE, defaultLoggingFactory.getLevel());
    }

    @Test
    @SneakyThrows
    public void testSubConfigOverride() {
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, "subConfig");
        final TypesafeConfiguration configuration = typesafeConfiguration.build();
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) configuration.getLoggingFactory();
        Assert.assertEquals(Level.INFO, defaultLoggingFactory.getLevel());

        //Metrics
        Assert.assertEquals(420, configuration.getMetricsFactory().getFrequency().toSeconds());

    }

    @Test
    @SneakyThrows
    public void testNoFileOverridesWithEnvironment() {
        System.setProperty("logging.level", "TRACE");
        ConfigFactory.invalidateCaches();
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, null);

        final TypesafeConfiguration configuration = typesafeConfiguration.build();

        //Server
        final DefaultServerFactory defaultServerFactory = (DefaultServerFactory) configuration.getServerFactory();
        final List<ConnectorFactory> connectorFactories = defaultServerFactory.getApplicationConnectors();
        Assert.assertEquals(1, connectorFactories.size());
        Assert.assertTrue(connectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 8765));

        final List<ConnectorFactory> adminConnectorFactories = defaultServerFactory.getAdminConnectors();
        Assert.assertEquals(1, adminConnectorFactories.size());
        Assert.assertTrue(adminConnectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory) connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 8081));

        // Check logging
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) configuration.getLoggingFactory();
        final Map<String, JsonNode> loggers = defaultLoggingFactory.getLoggers();
        Assert.assertEquals(5, loggers.size());
        Assert.assertTrue(loggers.keySet().contains("io.dropwizard"));
        Assert.assertEquals("debug", loggers.get("io.dropwizard").asText());
        Assert.assertEquals(Level.TRACE, defaultLoggingFactory.getLevel());
    }
}
