package ca.mestevens.java.configuration;

import com.typesafe.config.Config;
import io.dropwizard.core.Configuration;

public class TypesafeConfiguration extends Configuration {

    private Config config;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config newConfig) {
        this.config = newConfig;
    }
}
