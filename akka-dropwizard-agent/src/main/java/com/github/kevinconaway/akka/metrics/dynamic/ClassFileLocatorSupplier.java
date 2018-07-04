package com.github.kevinconaway.akka.metrics.dynamic;

import net.bytebuddy.dynamic.ClassFileLocator;

import java.util.function.Supplier;

public interface ClassFileLocatorSupplier extends Supplier<ClassFileLocator> {

}
