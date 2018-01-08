import com.tdsops.common.security.spring.HasPermission
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.FileUploadCommand
import net.transitionmanager.command.UploadTextContentCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.FileSystemService
import org.springframework.context.i18n.LocaleContextHolder


@Slf4j
@Secured("isAuthenticated()")
class WsFileSystemController implements ControllerMethods{

    FileSystemService fileSystemService

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

    @HasPermission(Permission.UserGeneralAccess)
    def uploadFile(FileUploadCommand fileUploadCommand) {
        if (fileUploadCommand.hasErrors()) {
            render (errorsInValidation(fileUploadCommand.errors) as JSON)
        }
        String filename = fileSystemService.copyToTemporaryFile(fileUploadCommand)
        renderSuccessJson([filename: filename])
    }



}
