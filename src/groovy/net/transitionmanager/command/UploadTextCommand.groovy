package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import grails.validation.Validateable
import net.transitionmanager.i18n.Message

/**
 * Command Object for uploading a file by providing its name
 * and content in the request.
 */
@Validateable
class UploadTextCommand implements FileCommand{

    String content
    String extension

    static constraints = {
        content nullable: false, blank: true

        extension nullable: true, validator: { ext, cmd ->
            if (!FileSystemUtil.isValidExtension(ext, cmd.getValidFileExtension())) {
                return  Message.FileSystemInvalidFileExtension
            }
        }
    }
}
