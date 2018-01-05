import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Slf4j
@Secured("isAuthenticated()")
class WsFileSystemController implements ControllerMethods{

    FileSystemService fileSystemService

    Locale locale = LocaleContextHolder.locale

    MessageSource messageSource

    static final String INVALID_EXTENSION_MSG = "fileSystem.invalidExtension"
    static final String EMPTY_FILE_UPLOADED_MSG = "fileSystem.emptyFileUploaded"

    static final String[] ALLOWED_EXTENSIONS = ["csv", "txt", "json", "xls", "xlsx", "xml"]

    /**
     * Endpoint for creating a temporary file in the system from a text input and file extension.
     * @param extension
     * @param content
     * @return temporary file's name
     */
    @HasPermission(Permission.UserGeneralAccess)
    def uploadText() {

        String extension = request.JSON.extension
        String content = request.JSON.content

        // Check if the file content is not empty.
        if (!content) {
            String errorMsg = messageSource.getMessage(EMPTY_FILE_UPLOADED_MSG, null , locale)
            throw new InvalidParamException(errorMsg)
        }

        // Check if the given extension is valid.
        if (!isValidExtension(extension)) {
            Object[] msgParams = [extension, getALLOWED_EXTENSIONS().join(", ")]
            String errorMsg = messageSource.getMessage(INVALID_EXTENSION_MSG, msgParams , locale)
            throw new InvalidParamException(errorMsg)
        }

        // Write the content to a temporary file and retrieve its name.
        String fileName = fileSystemService.writeTemporaryFileFromRawInput("", extension, content)

        renderSuccessJson([filename: fileName])


    }

    /**
     * Determine if the given extension should be allowed.
     *
     * @param extension
     * @return
     */
    private boolean isValidExtension(String extension) {
        return extension in ALLOWED_EXTENSIONS
    }
}
