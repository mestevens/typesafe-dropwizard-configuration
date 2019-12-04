package ca.mestevens.java.configuration.factory;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class TypesafeConfigurationFactory<T extends TypesafeConfiguration> implements ConfigurationFactory<T> {

    private static final String ENV_KEY = "ENV";
    private static final Logger logger = LoggerFactory.getLogger(TypesafeConfigurationFactory.class);
    private final ObjectMapper objectMapper;
    private final Class<T> aClass;
    private final String dropwizardConfigName;

    public TypesafeConfigurationFactory(final ObjectMapper objectMapper,
                                        final Class<T> aClass,
                                        final String dropwizardConfigName) {
        this.objectMapper = objectMapper.copy()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.aClass = aClass;
        this.dropwizardConfigName = dropwizardConfigName;
    }

    @Override
    public T build(final ConfigurationSourceProvider configurationSourceProvider, final String path) throws IOException, ConfigurationException {
        try (final InputStream inputStream = configurationSourceProvider.open(requireNonNull(path))) {
            final Config config = ConfigFactory.parseString(IOUtils.toString(inputStream));
            return this.createTypesafeConfiguration(config);
        } catch (final FileNotFoundException ex) {
            final Config config = ConfigFactory.load(path);
            return this.createTypesafeConfiguration(config);
        }

    }

    @Override
    public T build() throws IOException, ConfigurationException {

        // Start the loading process
        // Borrowed from: https://www.stubbornjava.com/posts/environment-aware-configuration-with-typesafe-config
        // Invalidate any existing caches to ensure we pickup the latest values from the System and Environment
        ConfigFactory.invalidateCaches();
        final Config systemProperties = ConfigFactory.systemProperties();
        Config config = ConfigFactory.empty();

        // 1. Look for application.conf in resources and application
        config = withResource(config, "reference");
        config = withResource(config, "application");

        // 2. Look for a matching .env.* files in the resources
        // If we don't pass an environment, then we know that we've already loaded the main application conf
        final String env = getEnvironmentOverride(systemProperties);
        if (env != null) {
            config = withResource(config, env + ".application");
        }
        // 3. Look for application.local.conf in the working directory
        config = withOptionalFile(config, getExecutionDirectory(systemProperties) + "/application.local.conf");

        // 4. Pull from the system properties and environment variables
        config = systemProperties.withFallback(config);
        config = ConfigFactory.systemEnvironment().withFallback(config);

        return this.createTypesafeConfiguration(config);
    }

    private T createTypesafeConfiguration(final Config config) throws IOException {
        final Config subConfig = (this.dropwizardConfigName != null) ?
                config.getConfig(this.dropwizardConfigName) :
                config;
        final Config resolvedSubConfig = subConfig.resolve();
        final T typesafeConfiguration = this.objectMapper.readValue(
                this.objectMapper.writeValueAsString(resolvedSubConfig.root().unwrapped()), aClass);
        logger.trace("Final config: {}", resolvedSubConfig.root().render());
        typesafeConfiguration.setConfig(resolvedSubConfig);
        return typesafeConfiguration;
    }

    private String getExecutionDirectory(Config systemProperties) {
        return systemProperties.getString("user.dir");
    }

    private static Config withOptionalFile(Config config, String path) {
        logger.debug("Attempting to load optional file at: {}", path);
        final File configFilePath = new File(path);

        if (configFilePath.exists()) {
            logger.debug("Found config file, loading");
            final Config cfg = ConfigFactory.parseFile(configFilePath);
            logger.trace(cfg.root().render());
            return cfg.withFallback(config);
        }
        return config;
    }

    private static Config withResource(Config config, String resource) {
        logger.debug("Appending {} to config", resource);
        final Config resourceConfig = ConfigFactory.parseResourcesAnySyntax(resource);
        logger.trace(resourceConfig.root().render());
        return resourceConfig.withFallback(config);
    }

    private String getEnvironmentOverride(Config systemProperties) {
        // Look to see if $ENV or -DENV is set, biasing towards the environment variable
        if (System.getenv(ENV_KEY) != null)
            return System.getenv(ENV_KEY);

        if (systemProperties.hasPath(ENV_KEY) && !systemProperties.getString(ENV_KEY).equals(""))
            return systemProperties.getString(ENV_KEY);

        return null;
    }


}
