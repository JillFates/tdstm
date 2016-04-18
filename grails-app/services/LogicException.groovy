/**
 * Exception used to indicate that a logic error in the code has occurred. This should be used
 * when something unexpected happens but can not be properly correlated with other exception types.
 */
class LogicException extends RuntimeException {

	public LogicException() {
		super()
	}

	public LogicException(String message) {
		super(message)
	}

	public LogicException(groovy.lang.GString message) {
		super(message.toString())
	}
	
}
