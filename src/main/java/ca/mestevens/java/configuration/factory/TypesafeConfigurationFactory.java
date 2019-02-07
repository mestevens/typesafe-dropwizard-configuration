package ca.mestevens.java.configuration.factory;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
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
    private Config config;
    private final Config systemProperties = ConfigFactory.systemProperties();
    private final Config systemEnvironment = ConfigFactory.systemEnvironment();

    public TypesafeConfigurationFactory(final ObjectMapper objectMapper,
                                        final Class<T> aClass,
                                        final String dropwizardConfigName) {
        this.objectMapper = objectMapper.copy()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.aClass = aClass;
        this.dropwizardConfigName = dropwizardConfigName;
        this.config = ConfigFactory.empty();
        this.config = config.withFallback(systemProperties);
        this.config = config.withFallback(systemEnvironment);
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

        // 1. Pull from the system properties and environment variables


        // 3. Look for application.local.conf in the working directory
        this.withOptionalFile(getExecutionDirectory() + "/application.local.conf");

        // 4. Look for a matching .env.* files in the resources
        final String env = systemProperties.getString("env");
        final String resourceKey = env.isEmpty() ? "application" : env + ".application";
        this.withResource(resourceKey);


        // 5. Look for application.conf in resources
        // If we don't pass an environment, then we know that we've already loaded the main application conf
        if (!env.isEmpty()) {
            this.withResource("application");
        }
//        final Config config = ConfigFactory.load();
        return this.createTypesafeConfiguration(config);
    }

    private T createTypesafeConfiguration(final Config cfg2) throws IOException {
        final Config subConfig = (this.dropwizardConfigName != null) ?
                cfg2.getConfig(this.dropwizardConfigName) :
                cfg2;
        final Config resolvedSubConfig = subConfig.resolve();
        final T typesafeConfiguration = this.objectMapper.readValue(
                this.objectMapper.writeValueAsString(resolvedSubConfig.root().unwrapped()), aClass);
        typesafeConfiguration.setConfig(resolvedSubConfig);
        return typesafeConfiguration;
    }

    private void withOptionalFile(String path) {
        final File configFilePath = new File(path);

        if (configFilePath.exists()) {
            this.config = this.config.withFallback(ConfigFactory.parseFile(configFilePath));
        }
    }

    private void withResource(String resource) {
        final Config resourceConfig = ConfigFactory.parseResourcesAnySyntax(resource);
        this.config = this.config.withFallback(resourceConfig);
    }

    private String getExecutionDirectory() {
        return systemProperties.getString("user.dir");
    }

}
