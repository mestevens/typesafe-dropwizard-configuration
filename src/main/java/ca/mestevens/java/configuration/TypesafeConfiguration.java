package ca.mestevens.java.configuration;

import com.typesafe.config.Config;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

public class TypesafeConfiguration extends Configuration {

    @Getter
    @Setter
    private Config config;

}
