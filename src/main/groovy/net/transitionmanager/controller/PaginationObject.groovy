package net.transitionmanager.controller

// import net.transitionmanager.controller.PaginationMethods


/**
 * PaginationObject is used in conjunction with controllers to pass PaginationMethods down to
 * the service layer so that the services can use the same methods as the Controller classes can.
 */
class PaginationObject implements PaginationMethods {
	Map params = [:]

	/**
	 * Public constructor
	 * @param params - a map of parameters
	 */
	PaginationObject(Map params) {
		this.params = params
	}

	/**
	 * Prevent calling the default constructor
	 */
	private PaginationObject() { }
}