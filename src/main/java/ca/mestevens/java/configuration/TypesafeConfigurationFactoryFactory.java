package ca.mestevens.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;

import javax.validation.Validator;

public class TypesafeConfigurationFactoryFactory<T extends TypesafeConfiguration> implements ConfigurationFactoryFactory<T> {

    @Override
    public ConfigurationFactory<T> create(Class<T> aClass, Validator validator, ObjectMapper objectMapper, String s) {
        return new TypesafeConfigurationFactory(objectMapper, aClass);
    }

}
