package com.tdsops.etl.marshall


import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * Defines a class used by AnnotationDrivenObjectMarshaller
 * @see AnnotationDrivenObjectMarshaller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ConfigureMarshalling {
}
