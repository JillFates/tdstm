package com.tdsops.common.security.spring;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the permission(s) required to access the annotated method or methods in the annotated class.
 *
 * @author <a href="mailto:burt@agileorbit.com">Burt Beckwith</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
@GroovyASTTransformationClass("com.tdsops.common.security.spring.HasPermissionASTTransformation")
public @interface HasPermission {

	/**
	 * One or more required permission names (e.g. "AssetEdit", "TaskBatchDelete", etc.).
	 *
	 * @return the names
	 */
	String[] value();
}
