package ca.mestevens.java.configuration.factory;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;

import javax.validation.Validator;

public class TypesafeConfigurationFactoryFactory<T extends TypesafeConfiguration> implements ConfigurationFactoryFactory<T> {

    private final String dropwizardConfigName;

    public TypesafeConfigurationFactoryFactory() {
        this.dropwizardConfigName = null;
    }

    public TypesafeConfigurationFactoryFactory(final String dropwizardConfigName) {
        this.dropwizardConfigName = dropwizardConfigName;
    }

    @Override
    public ConfigurationFactory<T> create(final Class<T> aClass,
                                          final Validator validator,
                                          final ObjectMapper objectMapper,
                                          final String s) {
        return new TypesafeConfigurationFactory(objectMapper, aClass, this.dropwizardConfigName);
    }

}
