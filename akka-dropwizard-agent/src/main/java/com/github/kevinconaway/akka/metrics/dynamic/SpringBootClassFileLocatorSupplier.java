package com.github.kevinconaway.akka.metrics.dynamic;

import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.ClassFileLocator.Compound;
import net.bytebuddy.dynamic.ClassFileLocator.ForJarFile;
import org.springframework.boot.loader.jar.JarFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Creates a {@link ClassFileLocator} composed of all jar files in the spring boot BOOT-INF/lib directory
 * on the classpath
 */
public class SpringBootClassFileLocatorSupplier implements ClassFileLocatorSupplier {

    @Override
    public ClassFileLocator get() {
        try {
            Class<?> clazz = Class.forName("org.springframework.boot.loader.JarLauncher");
            ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            URI location = codeSource.getLocation().toURI();
            String path = location.getSchemeSpecificPart();

            File file = new File(path);
            JarFile jarFile = new JarFile(file);

            List<ClassFileLocator> locators = new ArrayList<>();
            for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements(); ) {
                JarEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                if (entry.getName().startsWith("BOOT-INF/lib/")) {
                    locators.add(
                        new ForJarFile(jarFile.getNestedJarFile(entry))
                    );
                }
            }

            return new Compound(locators);
        } catch (ClassNotFoundException | URISyntaxException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
