/**
 * Exception used to indicate that some source code has invalid syntax
 */
class InvalidSyntaxException extends RuntimeException {

	public InvalidSyntaxException() {
		super()
	}

	public InvalidSyntaxException(String message) {
		super(message)
	}
	
	public InvalidSyntaxException(groovy.lang.GString message) {
		super(message.toString())
	}
	
}
