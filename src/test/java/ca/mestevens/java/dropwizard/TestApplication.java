package ca.mestevens.java.dropwizard;

import ca.mestevens.java.configuration.TypesafeConfiguration;
import ca.mestevens.java.configuration.bundle.TypesafeConfigurationBundle;
import ca.mestevens.java.dropwizard.rest.SimpleResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TestApplication extends Application<TypesafeConfiguration> {

    @Override
    public void initialize(final Bootstrap<TypesafeConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new TypesafeConfigurationBundle());
    }

    @Override
    public void run(final TypesafeConfiguration typesafeConfiguration,
                    final Environment environment) throws Exception {
        environment.jersey().register(new SimpleResource());
    }

}
