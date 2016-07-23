package ca.mestevens.java.configuration.bundle;

import ca.mestevens.java.configuration.factory.TypesafeConfigurationFactoryFactory;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TypesafeConfigurationBundle implements Bundle {

    final String dropwizardConfigName;

    public TypesafeConfigurationBundle() {
        this(null);
    }

    public TypesafeConfigurationBundle(final String dropwizardConfigName) {
        this.dropwizardConfigName = dropwizardConfigName;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        this.setConfigurationFactoryFactory(bootstrap);
    }

    @Override
    public void run(final Environment environment) {

    }

    private void setConfigurationFactoryFactory(final Bootstrap bootstrap) {
        bootstrap.setConfigurationFactoryFactory(new TypesafeConfigurationFactoryFactory<>(this.dropwizardConfigName));
    }

}
