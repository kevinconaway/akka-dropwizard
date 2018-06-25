# Dropwizard Metrics for Akka Actors

This project instruments an Akka ActorSystem to provide Dropwizard metrics for Actors.  Currently, the following
metrics are captured:

* A [`Gauge`](https://metrics.dropwizard.io/4.0.0/manual/core.html#gauges) for the size of an Actors mailbox
* A [`Timer`](https://metrics.dropwizard.io/4.0.0/manual/core.html#timers) measuring how long a message sits in an Actors mailbox

# Usage

This project is composed of two modules:

* _akka-dropwizard-agent_, a java agent which performs the ActorSystem instrumentation
* _akka-dropwizard-metrics_ which contains the code to configure the agent.

First, include the _akka-dropwizard-metrics_ module in your project:

        <dependency>
            <groupId>com.github.kevinconaway</groupId>
            <artifactId>akka-dropwizard-metrics</artifactId>
            <version>1.0</version>
        </dependency>

There is a singleton configuration class, `AkkaDropwizard` that must be configured *before* the `ActorSystem` is created

```java
MetricRegistry registry = ...
AkkaDropwizard.configure(registry);
```

By default, the metrics will be stored under a root prefix called _actor-metrics_.  You can customize this by providing
an instance of `AkkaDropwizardSettings` to `AkkaDropwizard`

```java
AkkaDropwizardSettings settings = ...
MetricRegistry registry = ...
AkkaDropwizard.configure(registry, settings);
```

The instrumentation is performed by a java agent that you need to run with your application.  Add the following 
argument to your VM startup properties:

    -javaagent:/path/to/akka-dropwizard-agent-1.0.jar

# Compatibility matrix

Below are the versions Akka and Dropwizard that this library uses in each version

| This Project  | Akka Version          | Dropwizard Version    |
| ------------  | ------------          | -------------------   |
| 1.0           | 2.5.x (Scala 2.12)    |   3.2.x               |