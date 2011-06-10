package com.rosowski.grails.plugin;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("com.rosowski.grails.plugin.ConcurrentUpdateASTTransformation")
public @interface ConcurrentUpdate {
}