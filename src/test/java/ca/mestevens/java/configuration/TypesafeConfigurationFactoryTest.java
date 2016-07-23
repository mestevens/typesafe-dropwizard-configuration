package ca.mestevens.java.configuration;

import ca.mestevens.java.UnitTest;
import ca.mestevens.java.configuration.factory.TypesafeConfigurationFactory;
import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.LogbackModule;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.server.DefaultServerFactory;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Category(UnitTest.class)
public class TypesafeConfigurationFactoryTest {

    private final String PATH = "path";
    private ObjectMapper objectMapper;
    private TypesafeConfigurationFactory typesafeConfiguration;
    private ConfigurationSourceProvider configurationSourceProvider;

    @Before
    @SneakyThrows
    public void setUp() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .registerModule(new LogbackModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver());
        this.configurationSourceProvider = Mockito.mock(ConfigurationSourceProvider.class);
        Mockito.when(configurationSourceProvider.open(PATH))
                .thenReturn(this.getClass().getResourceAsStream("/test.conf"));
    }

    @After
    public void tearDown() {
        this.typesafeConfiguration = null;
        this.objectMapper = null;
    }

    @Test
    @SneakyThrows
    public void noSubpathConfig() {
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, null);
        final TypesafeConfiguration configuration = typesafeConfiguration.build(configurationSourceProvider, PATH);

        //Server
        final DefaultServerFactory defaultServerFactory = (DefaultServerFactory)configuration.getServerFactory();
        final List<ConnectorFactory> connectorFactories = defaultServerFactory.getApplicationConnectors();
        Assert.assertEquals(1, connectorFactories.size());
        Assert.assertTrue(connectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory)connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 8765));

        //Logging
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory)configuration.getLoggingFactory();
        final Map<String, JsonNode> loggers = defaultLoggingFactory.getLoggers();
        Assert.assertEquals(5, loggers.size());
        Assert.assertTrue(loggers.keySet().contains("io.dropwizard"));
        Assert.assertEquals("debug", loggers.get("io.dropwizard").asText());
        Assert.assertTrue(loggers.keySet().contains("org.eclipse.jetty"));
        Assert.assertEquals("warn", loggers.get("org.eclipse.jetty").asText());
        Assert.assertTrue(loggers.keySet().contains("com.amazonaws"));
        Assert.assertEquals("warn", loggers.get("com.amazonaws").asText());
        Assert.assertTrue(loggers.keySet().contains("io.netty"));
        Assert.assertEquals("warn", loggers.get("io.netty").asText());
        Assert.assertTrue(loggers.keySet().contains("org.apache.http"));
        Assert.assertEquals("warn", loggers.get("org.apache.http").asText());
        Assert.assertEquals(Level.INFO, defaultLoggingFactory.getLevel());
        Assert.assertEquals(1, defaultLoggingFactory.getAppenders().size());
        Assert.assertTrue(defaultLoggingFactory.getAppenders()
                .stream()
                .map(appender -> (ConsoleAppenderFactory)appender)
                .anyMatch(appender -> (appender.getTimeZone().equals(TimeZone.getTimeZone("utc")))
                        && (appender.getLogFormat().equals("%d{yyyy-MM-dd'T'HH:mm:ssXXX, UTC} %-5level [%thread] %logger{36} - %msg%n"))));

        //Metrics
        Assert.assertEquals(300, configuration.getMetricsFactory().getFrequency().toSeconds());
    }

    @Test
    @SneakyThrows
    public void subPathConfig() {
        this.typesafeConfiguration = new TypesafeConfigurationFactory<>(objectMapper, TypesafeConfiguration.class, "subConfig");
        final TypesafeConfiguration configuration = typesafeConfiguration.build(configurationSourceProvider, PATH);

        //Server
        final DefaultServerFactory defaultServerFactory = (DefaultServerFactory)configuration.getServerFactory();
        final List<ConnectorFactory> connectorFactories = defaultServerFactory.getApplicationConnectors();
        Assert.assertEquals(1, connectorFactories.size());
        Assert.assertTrue(connectorFactories
                .stream()
                .map(connectorFactory -> (HttpConnectorFactory)connectorFactory)
                .anyMatch(httpConnectorFactory -> httpConnectorFactory.getPort() == 8198));

        //Logging
        final DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory)configuration.getLoggingFactory();
        Assert.assertEquals(Level.DEBUG, defaultLoggingFactory.getLevel());
    }

}
