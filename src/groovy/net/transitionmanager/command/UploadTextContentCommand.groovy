package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import grails.validation.Validateable

@Validateable
class UploadTextContentCommand {

    String content
    String extension

    static constraints = {
        content nullable:true, validator: { text ->
            if (!text) {
                return ['fileSystem.fileContent.empty']
            }

        }
        extension nullable: true, validator: { ext ->
            if (!FileSystemUtil.isValidExtension(ext)) {
                return ["fileSystem.fileExtension.invalid"]
            }
        }

    }
}
