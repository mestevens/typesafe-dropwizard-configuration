package ca.mestevens.java.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class TypesafeConfigurationFactory<T extends TypesafeConfiguration> implements ConfigurationFactory<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> aClass;

    public TypesafeConfigurationFactory(final ObjectMapper objectMapper,
                                        final Class<T> aClass) {
        this.objectMapper = objectMapper.copy()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.aClass = aClass;
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
        final Config config = ConfigFactory.load();
        return this.createTypesafeConfiguration(config);
    }

    private T createTypesafeConfiguration(final Config config) throws IOException {
        final T typesafeConfiguration = this.objectMapper.readValue(this.objectMapper.writeValueAsString(config.root().unwrapped()), aClass);
        typesafeConfiguration.setConfig(config);
        return typesafeConfiguration;
    }

}
