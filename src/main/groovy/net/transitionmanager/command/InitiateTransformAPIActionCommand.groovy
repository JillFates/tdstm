package net.transitionmanager.command
/**
 * Command class that will be used typically for the following request:
 * <pre>
 * 	POST
 * 	/tdstm/api/dataScript/11/transform?sendNotification=true
 * </pre>
 */
class InitiateTransformAPIActionCommand extends UploadFileCommand {
	/**
	 * ID for an instance of {@code Datascript} domain  class.
	 * This instance contains the ETL script content
	 * that is going to be executed against filename param content.
	 */
	Long id
	/**
	 * Defines if Transformation results and the auto import process
	 * should be sent by email after finishing the post step.
	 */
	Boolean sendNotification = false


	static constraints = {
		id nullable: false
		sendNotification nullable: false
	}
}
