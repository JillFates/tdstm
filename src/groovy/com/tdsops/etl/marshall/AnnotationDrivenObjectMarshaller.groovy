package com.tdsops.etl.marshall

import org.codehaus.groovy.grails.web.converters.Converter
import org.codehaus.groovy.grails.web.converters.marshaller.ClosureObjectMarshaller
import org.springframework.beans.BeanUtils

import java.beans.PropertyDescriptor
import java.lang.reflect.Field

/**
 * <p>AnnotationDrivenObjectMarshaller is used to define a new ClosureObjectMarshaller.
 * It enables to exclude fields and properties from marshalling in a JSON transformation.</p>
 * <p>In order to define which class can use this marshaller it needs to be annotated by the @ConfigureMarshalling class.</p>
 * <pre>
 *  @ConfigureMarshalling
 *  class ETLProcessorResult {
 *
 * </pre>
 *
 * <p>Then every field annotated by @DoNotMarshall will be removed from the JSON serialization.</p>
 * <pre>
 * @DoNotMarshall
 * ETLProcessor processor
 * </pre>
 * <p>The last step is to register the new custom marshaller</p>
 * <pre>
 *   JSON.registerObjectMarshaller(new AnnotationDrivenObjectMarshaller<JSON>())
 * </pre>
 * <p>As an example used in tdstm, we can check its use based on ETLPpocessor and ETLProcessorResult classes.</p><br>
 * <pre>
 *  ETLProcessor processor = new ETLProcessor(......)
 *  etlProcessor.evaluate(etlScript)
 *  JSONObject marshalled = JSON.parse(new JSON(etlProcessor.result).toString())
 *  marshalled.with {
 *      domains.size() == 1
 *      .....
 *  }
 * </pre>
 *
 * Reference used:
 * http://blog.proxerd.pl/article/excluding-fields-and-properties-from-marshalling-using-annotations-in-grails
 */
class AnnotationDrivenObjectMarshaller<T extends Converter> extends ClosureObjectMarshaller<T> {

	static final String CLASS_PROPERTY_NAME = 'class'
	static final String META_CLASS_PROPERTY_NAME = 'metaClass'

	/**
	 * Closure responsible for detect which field are excluded in a marshalling step.<br>
	 *
	 */
	private final static Closure marshallingClosure = { marshalled ->
		Class marshalledClass = marshalled.getClass()
		Field[] fields = marshalledClass.declaredFields

		PropertyDescriptor[] properties = BeanUtils.getPropertyDescriptors(marshalledClass)

		def propertiesToBeMarshalled = properties.findAll { PropertyDescriptor property ->
			List accessorAndField = [property.readMethod, fields.find { it.name == property.name }]
			boolean isAnnotationPresent = accessorAndField.any { it?.isAnnotationPresent(DoNotMarshall) }

			property.name != CLASS_PROPERTY_NAME &&
				property.name != META_CLASS_PROPERTY_NAME &&
				!isAnnotationPresent
		}
		return propertiesToBeMarshalled.inject([:]) { Map result, PropertyDescriptor property ->
			result[property.name] = property.readMethod.invoke(marshalled, null as Object[])
			result
		}
	}

	/**
	 * Constructor register a copy of AnnotationDrivenObjectMarshaller#marshallingClosure
	 */
	AnnotationDrivenObjectMarshaller() {
		super(GroovyObject, marshallingClosure.clone())
	}

	/**
	 * Defines which class has support for this AnnotationDrivenObjectMarshaller custom marshaller
	 * @param object
	 * @return
	 */
	@Override
	boolean supports(Object object) {
		super.supports(object) && object.class.isAnnotationPresent(ConfigureMarshalling)
	}
}
