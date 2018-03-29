package com.tdsops.etl

/**
 * Wrapper over a data set field value
 * to be used in an find ETL command.
 * It is used in ETL scripts. For example:
 * <pre>
 *      init appVendor with DOMAIN.appVendor
 *      ......
 *      find Application by assetName, appVendor with DOMAIN.assetName, vmWare into id
 *      ....
 *      set myLocalVariable with DOMAIN.environment
 * </pre>
 */
class DomainField {

	/**
	 * This is the value to wrapped in an instance of DomainField class
	 */
	private Object value

	DomainField(Object value) {
		this.value = value
	}

	/**
	 * Using meta-programming, this method forwards
	 * any invoked over an instance of DomainField class
	 * to the wrapped value.
	 * For example, the following ETL script:
	 * <pre>
	 *   if(DOMAIN.id > 10000){
	 *      ....
	 *   }
	 *
	 *   if(DOMAIN.appVendor.startsWith('Microsoft')) {
	 *       .....
	 *   }
	 * </pre>
	 * @param name the method to be invoked
	 * @param args an array of objects used in the invoke method
	 * @return the result of invoke method name over DomainField#value instance using args as a parameter.
	 *
	 */
	def invokeMethod(String name, args){
		return value.invokeMethod(name, args)
	}
}
