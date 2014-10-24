
class InvalidRequestException extends RuntimeException {

	public InvalidRequestException() {
		super()
	}

	public InvalidRequestException(String message) {
		super(message)
	}

	public InvalidRequestException(groovy.lang.GString message) {
		super(message.toString())
	}
}