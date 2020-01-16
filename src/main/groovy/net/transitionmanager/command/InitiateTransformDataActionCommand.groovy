package net.transitionmanager.command
/**
 * Command class that will be used typically for the following request:
 * <pre>
 * 	POST
 * 	/tdstm/ws/assetImport/initiateTransformData?dataScriptId=6&filename=EtlSourceData_TBXnyXxrwDNnTFqjaQCA9QSMAdVPvRE9.csv&sendNotification=false
 * </pre>
 */
class InitiateTransformDataActionCommand implements CommandObject {

	/**
	 * ID for an instance of {@code Datascript} domain  class.
	 * This instance contains the ETL script content
	 * that is going to be executed against filename param content.
	 */
	Long dataScriptId
	/**
	 * A file name saved previously with in an ETL dataset.
	 */
	String filename
	/**
	 * Defines if Transformation results and the auto import process
	 * should be sent by email after finishing the post step.
	 */
	Boolean sendNotification = false

	static constraints = {
		dataScriptId nullable: false
		filename nullable: false
		sendNotification nullable: false
	}
}
