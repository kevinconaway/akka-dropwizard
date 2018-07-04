package com.github.kevinconaway.akka.metrics.dynamic;

import net.bytebuddy.dynamic.ClassFileLocator;

import java.util.ArrayList;
import java.util.List;

public class AgentClassFileLocator {

    /**
     * Create a class file locator for use in finding classes referenced by the agent.
     *
     * @return Compound class file locator
     */
    public static ClassFileLocator create() {
        List<ClassFileLocator> locators = new ArrayList<>();

        locators.add(ClassFileLocator.ForClassLoader.ofClassPath());

        if (isSpringBootPresent()) {
            locators.add(getSpringBootClassFileLocator());
        }

        return new ClassFileLocator.Compound(locators);
    }

    private static boolean isSpringBootPresent() {
        try {
            Class.forName("org.springframework.boot.loader.JarLauncher");
            return true;
        } catch (ClassNotFoundException ignore) {
            // Ignore this
        }

        return false;
    }

    /**
     * We have to be careful not to hard reference any spring boot classes in case they aren't present.
     */
    private static ClassFileLocator getSpringBootClassFileLocator() {
        try {
            Class<?> clazz = Class.forName("com.github.kevinconaway.akka.metrics.dynamic.SpringBootClassFileLocatorSupplier");
            ClassFileLocatorSupplier supplier = (ClassFileLocatorSupplier) clazz.newInstance();
            return supplier.get();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Unable to create spring boot class file locator", ex);
        }
    }

}
