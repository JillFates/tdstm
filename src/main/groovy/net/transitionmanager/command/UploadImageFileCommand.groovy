package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.i18n.Message
import org.springframework.web.multipart.MultipartFile

/**
 * Command Object for handling the upload and writing of a given file
 * to the filesystem.
 */

class UploadImageFileCommand extends UploadFileCommand {

    /**
     * A List of accepted file extensions for validating
     * this command object.
     * @return
     */
    List<String> getValidFileExtension() {
        return FileSystemService.ALLOWED_FILE_EXTENSIONS_FOR_IMAGE_UPLOADS
    }
}
