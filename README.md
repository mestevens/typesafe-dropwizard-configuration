# typesafe-dropwizard-configuration

## Description

A dropwizard configuration factory (and factory factory) so that you can use typesafe config files instead of yml files to configure dropwizard.

## Dependency Information
```
<dependency>
    <groupId>ca.mestevens.java</groupId>
    <artifactId>typesafe-dropwizard-configuration</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage
The example shown below will be assuming that you are using [Dropwizard](dropwizard.github.io/dropwizard) framework.

In order to use the configuration factory, there are two steps that need to be done.

In your dropwizard application class, in the `initialize` method, add the following bundle:

```
bootstrap.addBundle(new TypesafeConfigurationBundle);
```

There are two ways to accomplish the second step of the configuration.

### Extend your Dropwizard Configuration
Have your dropwizard configuration file extend `TypesafeConfiguration` instead of `Configuration`

### Change the Dropwizard Application
Change the type param of your dropwizard application to be `TypesafeConfiguration` instead of whatever your current configuration class is. Note, this method is only recommended if you don't want to add anything to your configuration class and want to use the typesafe config for everything.

After setting everything up you'll need to have a typesafe config file with settings in it. For a very basic example, look at the following Dropwizard application.yml file

```
logging:
  level: INFO

server:
  applicationConnectors:
  - type: http
    port: 8198
    
metrics:
  frequency: 5m

```

Adapting this to a typesafe config would look like the following:

```
logging {
  level = INFO
}

server {
  applicationConnectors = [{
    type = http
    port = 8198
  }]
}

metrics {
  frequency = 5m
}
```

You can then also do a bunch of cool typesafe config stuff as written [here](https://github.com/typesafehub/config).

## File Location
There are some cool things you get for free by using this configuration. First, you don't have to pass a config into your start-up command if you have an `application.conf` file in your resources folder. In addition, the file you pass in can either be in your resources or in another location, and the configuration will pick it up for you.

For example if I start my server with the following command `java -jar server-name.jar server` it will use the `application.conf` in my resource directory. If I use the following `java -jar server-name.jar server environment.conf` it will first attempt to use a file named `environment.conf` in the jar resources, but if it can't find that will attempt to load a file called `environment.conf` in my current directory. This is useful if you need to override settings without rebuilding the jar.

## Custom Properties
You can access custom properties (or any properties for that matter) from your config by calling the `getConfig()` method on the `TypesafeConfiguration` class. You then interact with the config as you would a normal typesafe config.

As an example suppose I use the conf file from above and add a new one:
```
include "application"

custom {
    value = 1234    
}

```

If I have an instance of my Dropwizard Configuration all I have to do to get that property is:

```
configuration.getConfig().getInt("custom.value");
```

## Sub-configuration
If you want your dropwizard application to read it's configuration settings from a sub-config object inside your .conf file, you can provide the sub-config in the set-up step

```
boostrap.addBundle(new TypesafeConfigurationBundle("subConfig"));
```
