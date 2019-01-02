package com.tdsops.etl

trait ETLCommand {

	public static final String COMMENTS = 'comments'

	/**
	 * Calculates values for every args received in a WhenFound and WhenNotFound command.
	 * @param args array with values received in a WhenFound and WhenNotFound command.
	 * @return an array with all the args transformed by ETLValueHelper class.
	 * @see ETLValueHelper#valueOf(java.lang.Object)
	 */
	def calculateValue(def args) {
		if (args.size() == 1) {
			//TODO: DMC. Review with John. TM-11182 Application Description field not loading during asset posting when using a variable
			if (processor.binding.isValidETLVariableName(args[0]?.toString())) {
				throw ETLProcessorException.missingPropertyException(args[0])
			}
			return ETLValueHelper.valueOf(args[0])
		} else {
			return args.collect { ETLValueHelper.valueOf(it) }
		}
	}

	/**
	 * <p>Determines if a fieldName specified in an ETL command belongs to a 'comments'.</p>
	 * <pre>
	 * 		load 'comments' with 'Added by ETL Script' key 'My common Key'
	 * </pre>
	 * @param fieldName a fieldName used in an ETL script
	 * @return true if field name is equals {@code Element#COMMENTS}
	 */
	boolean isCommentsCommand(String fieldName) {
		return COMMENTS == fieldName
	}
}
