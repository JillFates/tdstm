package com.tdsops.etl.marshall

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Defines a property name or field taht will be excluded in the marshalling used by AnnotationDrivenObjectMarshaller
 *
 * @see AnnotationDrivenObjectMarshaller
 * @see ConfigureMarshalling
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@interface DoNotMarshall {
}
