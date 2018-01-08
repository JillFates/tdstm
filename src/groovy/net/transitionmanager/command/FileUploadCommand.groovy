package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

@Validateable
class FileUploadCommand {

    MultipartFile file

    static constraints = {

        file nullable: false, validator: { file ->
            if (file == null || file.empty) {
                return "fileSystem.fileContent.empty"
            } else {
                if (!FileSystemUtil.validateExtension(file)) {
                    return "fileSystem.fileExtension.invalid"
                }
            }
        }
    }
}
