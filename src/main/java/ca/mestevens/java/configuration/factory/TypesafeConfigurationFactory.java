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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class TypesafeConfigurationFactory<T extends TypesafeConfiguration> implements ConfigurationFactory<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> aClass;
    private final String dropwizardConfigName;
    private final Config systemProperties;

    public TypesafeConfigurationFactory(final ObjectMapper objectMapper,
                                        final Class<T> aClass,
                                        final String dropwizardConfigName) {
        this.objectMapper = objectMapper.copy()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.aClass = aClass;
        this.dropwizardConfigName = dropwizardConfigName;
        this.systemProperties = ConfigFactory.systemProperties();
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
        Config config = ConfigFactory.empty();

        // 1. Look for application.conf in resources and application
        config = withResource(config, "reference");
        config = withResource(config, "application");

        // 2. Look for a matching .env.* files in the resources
        final String resourceKey = systemProperties.hasPath("env") ? systemProperties.getString("env") + ".application" : "application";
        // If we don't pass an environment, then we know that we've already loaded the main application conf
        if (systemProperties.hasPath("env")) {
            config = withResource(config, resourceKey);
        }

        // 3. Look for application.local.conf in the working directory
        config = withOptionalFile(config, getExecutionDirectory() + "/application.local.conf");

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
        typesafeConfiguration.setConfig(resolvedSubConfig);
        return typesafeConfiguration;
    }

    private String getExecutionDirectory() {
        return systemProperties.getString("user.dir");
    }

    private static Config withOptionalFile(Config config, String path) {
        final File configFilePath = new File(path);

        if (configFilePath.exists()) {
            return ConfigFactory.parseFile(configFilePath).withFallback(config);
        }
        return config;
    }

    private static Config withResource(Config config, String resource) {
        final Config resourceConfig = ConfigFactory.parseResourcesAnySyntax(resource);
        return resourceConfig.withFallback(config);
    }


}
