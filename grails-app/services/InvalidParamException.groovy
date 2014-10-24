/**
 * Exception used to indicate that an invalid parameter was received
 */
class InvalidParamException extends RuntimeException {

	public InvalidParamException() {
		super()
	}

	public InvalidParamException(String message) {
		super(message)
	}

	public InvalidParamException(groovy.lang.GString message) {
		super(message.toString())
	}
	
}
