package net.transitionmanager.command

import net.transitionmanager.service.FileSystemService

trait FileCommand {

    /**
     * A List of accepted file extensions for validating
     * this command object.
     * @return
     */
    List<String> getValidFileExtension() {
        return FileSystemService.ALLOWED_FILE_EXTENSIONS_FOR_ETL_UPLOADS
    }
}
