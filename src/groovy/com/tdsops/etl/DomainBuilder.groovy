package com.tdsops.etl

/**
 * Class used to manage domain chain methods command.
 * <pre>
 *  read labels
 *  iterate {
 *  	domain Application method 'arg'
 *  }
 * </pre>
 * <p>An instance or domain builder is in charged to manipulate all next methods
 * executed in an ETL Script.</p>
 * <p>Subclasses can extends this behaviour based on the domain definition</p>
 *
 * @see DependencyBuilder
 */
class DomainBuilder {

	ETLDomain domain
	ETLProcessor processor

	DomainBuilder(ETLDomain domain, ETLProcessor processor) {
		this.domain = domain
		this.processor = processor
	}

	/**
	 * <p>This "method missing" method is used by domain command to manage all the chain methods used in an ETL Script</p>
	 *
	 * @param methodName
	 * @param args
	 */
	def methodMissing(String methodName, Object args) {
		if (methodName == 'with') {
			throw ETLProcessorException.invalidDomainForDomainDependencyWithCommand()
		} else {
			throw ETLProcessorException.unrecognizedArguments()
		}
	}

	/**
	 * <p>Factory method to create domain command instances.</p>
	 * It is in charge to define the correct instance of {@code DomainBuilder} hierarchy
	 * to be created in a domain command
	 * @param domain
	 * @param processor an instance of {@code ETLProcessor}
	 * @return an instance of {@code DomainBuilder} hierarchy.
	 */
	static DomainBuilder create(ETLDomain domain, ETLProcessor processor) {

		if (domain == ETLDomain.Dependency) {
			return new DependencyBuilder(domain, processor)
		} else {
			return new DomainBuilder(domain, processor)
		}
	}

}
