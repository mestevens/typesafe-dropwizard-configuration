package ca.mestevens.java.configuration.bundle;

import ca.mestevens.java.configuration.factory.TypesafeConfigurationFactoryFactory;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;

public class TypesafeConfigurationBundle<T extends Configuration> implements ConfiguredBundle<T> {

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



    private void setConfigurationFactoryFactory(final Bootstrap bootstrap) {
        bootstrap.setConfigurationFactoryFactory(new TypesafeConfigurationFactoryFactory<>(this.dropwizardConfigName));
    }

}
