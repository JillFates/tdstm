package net.transitionmanager.command

import net.transitionmanager.service.FileSystemService

trait FileCommand {

    static FileSystemService fileSystemService

    /**
     * A List of accepted file extensions for validating
     * this command object.
     * @return
     */
    List<String> getValidFileExtension() {
        return fileSystemService.getAllowedExtensions()
    }
}
