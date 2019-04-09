package net.transitionmanager.common

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.FileCommand
import net.transitionmanager.command.UploadFileCommand
import net.transitionmanager.command.UploadTextCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.i18n.Message
import net.transitionmanager.security.Permission
import org.springframework.context.i18n.LocaleContextHolder

@Slf4j
@Secured("isAuthenticated()")
class WsFileSystemController implements ControllerMethods{

    FileSystemService fileSystemService

    /**
     * Endpoint for creating a temporary file in the system from a text input and file extension.
     * @param extension
     * @param content
     * @return temporary file's name
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadText(UploadTextCommand uploadTextCommand) {
        doFileUpload(uploadTextCommand)
    }

	/**
	 * Endpoint for creating a temporary file in the system from a text input and file extension
	 * from the ETLDesigner to the server.
	 * @param extension
	 * @param content
	 * @return temporary file's name
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def uploadTextETLDesigner(UploadTextCommand uploadTextCommand) {
		doFileUploadETLDesigner(uploadTextCommand)
	}

	/**
	 * Endpoint for creating a temporary file in the system from a text input and file extension
	 * from the Asset Import (ETL) to the server.
	 * @param extension
	 * @param content
	 * @return temporary file's name
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def uploadTextETLAssetImport(UploadTextCommand uploadTextCommand) {
		doFileUploadETLAssetImport(uploadTextCommand)
	}

    /**
     * Endpoint for uploading a file to the server.
     * @param fileUploadCommand
     * @return
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadFile(UploadFileCommand fileUploadCommand) {
	    doFileUpload(fileUploadCommand)
    }

    /**
     * Endpoint for uploading a file from the ETLDesigner to the server.
     * @param fileUploadCommand
     * @return
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadFileETLDesigner(UploadFileCommand fileUploadCommand) {
	    doFileUploadETLDesigner(fileUploadCommand)
    }

	/**
	 * Endpoint for uploading a file from the Asset Import (ETL) to the server.
	 * @param fileUploadCommand
	 * @return
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def uploadFileETLAssetImport(UploadFileCommand fileUploadCommand) {
		doFileUploadETLAssetImport(fileUploadCommand)
	}

	/**
	 * Do the actual uploading of a file to the file system for the ETLDesigner.
	 *
	 * @param fileCommand
	 * @return
	 */
	private def doFileUploadETLDesigner(FileCommand fileCommand) {
		doFileUpload(fileCommand,  FileSystemService.ETL_SAMPLE_DATA_PREFIX)
	}

	/**
	 * Do the actual uploading of a file to the file system for the Asset Import (ETL).
	 *
	 * @param fileCommand
	 * @return
	 */
	private def doFileUploadETLAssetImport(FileCommand fileCommand) {
		doFileUpload(fileCommand, FileSystemService.ETL_SOURCE_DATA_PREFIX) // TODO: This should be in a Constant
	}

    /**
     * Do the actual uploading of a file to the file system.
     *
     * @param fileCommand
     * @param prefix for the uploaded file name
     * @return
     */
    private def doFileUpload(FileCommand fileCommand, String prefix = '') {
        String fileName = fileSystemService.transferFileToFileSystem(fileCommand, prefix)
        if (fileCommand.hasErrors()) {
            renderErrorJson(errorsInValidation(fileCommand.errors))
        } else {
            renderSuccessJson([filename: fileName])
        }
    }

    /**
     * Endpoint for deleting temporary files from the server.
     * @return
     */
    @HasPermission(Permission.UserGeneralAccess)
    def deleteFile(){
        Locale locale = LocaleContextHolder.locale
        List<String> fileNames = request.JSON.filename.split(',')
        List<String> skippedFiles = []
        for (fileName in fileNames) {
            fileName = fileName.trim()
            if (!fileSystemService.deleteTemporaryFile(fileName)) {
               skippedFiles << fileName
            }
        }
        if (skippedFiles) {
            Object[] msgArgs = [skippedFiles.join(', ')]
            renderErrorJson(messageSource.getMessage(Message.FileSystemFilesCannotBeDeleted, msgArgs, locale))
        } else {
            renderSuccessJson(messageSource.getMessage(Message.FileSystemFilesDeleted, null, locale))
        }
    }


}
