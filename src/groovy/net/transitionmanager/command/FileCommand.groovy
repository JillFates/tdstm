package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import grails.validation.Validateable


trait FileCommand {

    /**
     * A List of accepted file extensions for validating
     * this command object.
     * @return
     */
    List<String> getValidFileExtension() {
        return FileSystemUtil.ALLOWED_FILE_EXTENSIONS_FOR_ETL_UPLOADS
    }
}
