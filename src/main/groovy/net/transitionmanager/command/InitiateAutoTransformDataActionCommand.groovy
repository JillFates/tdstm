package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import net.transitionmanager.i18n.Message
import org.springframework.web.multipart.MultipartFile

/**
 * Command class that will be used typically for the following request:
 * <pre>
 * 	POST
 * 	/tdstm/ws/assetImport/autoBatchProcessing?dataScriptId=6&sendNotification=false
 * </pre>
 */
class InitiateAutoTransformDataActionCommand extends UploadFileCommand {
	/**
	 * ID for an instance of {@code Datascript} domain  class.
	 * This instance contains the ETL script content
	 * that is going to be executed against filename param content.
	 */
	Long dataScriptId
	/**
	 * Defines if Transformation results and the auto import process
	 * should be sent by email after finishing the post step.
	 */
	Boolean sendNotification = false


	static constraints = {
		dataScriptId nullable: false
		sendNotification nullable: false
	}
}
