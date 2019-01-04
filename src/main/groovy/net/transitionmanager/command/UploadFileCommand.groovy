package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import grails.validation.Validateable
import net.transitionmanager.i18n.Message
import org.springframework.web.multipart.MultipartFile

/**
 * Command Object for handling the upload and writing of a given file
 * to the filesystem.
 */
@Validateable
class UploadFileCommand implements FileCommand{

    MultipartFile file

    static constraints = {

        /**
         * Accept empty files, but the extension needs to be valid.
         */
        file nullable: false, validator: { file, cmd ->
            if (!FileSystemUtil.validateExtension(file.getOriginalFilename(), cmd.getValidFileExtension())) {
                return Message.FileSystemInvalidFileExtension
            }
        }
    }
}
