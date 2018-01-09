import com.tdsops.common.security.spring.HasPermission
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.FileUploadCommand
import net.transitionmanager.command.UploadTextContentCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.FileSystemService
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder


@Slf4j
@Secured("isAuthenticated()")
class WsFileSystemController implements ControllerMethods{

    FileSystemService fileSystemService

    final static String FILE_DOESNT_EXIST_MSG = "fileSystem.fileNotExists"
    final static String FILE_DELETED_MSG = "fileSystem.fileDeleted"

    Locale locale = LocaleContextHolder.locale

    /**
     * Endpoint for creating a temporary file in the system from a text input and file extension.
     * @param extension
     * @param content
     * @return temporary file's name
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadText(UploadTextContentCommand uploadTextContentCommand) {
        if (uploadTextContentCommand.hasErrors()) {
            render (errorsInValidation(uploadTextContentCommand.errors) as JSON)
        }
        String filename = fileSystemService.writeTemporaryFileFromRawInput(null, uploadTextContentCommand)
        renderSuccessJson([filename: filename])
    }

    /**
     * Endpoint for uploading a file to the server.
     * @param fileUploadCommand
     * @return
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadFile(FileUploadCommand fileUploadCommand) {
        if (fileUploadCommand.hasErrors()) {
            render (errorsInValidation(fileUploadCommand.errors) as JSON)
        }
        String filename = fileSystemService.copyToTemporaryFile(fileUploadCommand)
        renderSuccessJson([filename: filename])
    }

    /**
     * Endpoint for deleting a temporary file from the server.
     * @return
     */
    @HasPermission(Permission.UserGeneralAccess)
    def deleteFile(){
        boolean result = fileSystemService.deleteTemporaryFile(request.JSON.filename)
        if (result) {
            renderSuccessJson(messageSource.getMessage(FILE_DELETED_MSG, null, locale))
        } else {
            renderErrorJson(messageSource.getMessage(FILE_DOESNT_EXIST_MSG, null, locale))
        }
    }


}
