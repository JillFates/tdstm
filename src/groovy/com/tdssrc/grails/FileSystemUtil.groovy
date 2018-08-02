package com.tdssrc.grails

import groovy.util.logging.Slf4j

import java.util.regex.Matcher

@Slf4j
class FileSystemUtil {

    /**
     * Format a file extension and validate it's among the accepted extensions.
     *
     * @param extension
     * @param acceptedExtension
     * @return
     */
    static boolean isValidExtension(String extension, List<String> acceptedExtension) {
        boolean isValid = false
        extension = formatExtension(extension)
        if (extension) {
            isValid = extension in acceptedExtension
        }
        return isValid
    }

    /**
     * Format a string to make it usable as a file extension by stripping out
     * any leading/trailing spaces and removing a possible leading dot.
     *
     * @param extension
     * @return
     */
    static String formatExtension(String extension) {
        if (extension) {
            extension = extension.trim().toLowerCase()
            if (extension.startsWith(".")) {
                extension = extension.substring(1)
            }
        }
        return extension
    }

    /**
     * Return the file extension for a given file in lower case.
     *
     * @param file
     * @return
     */
    static String getFileExtension(String filename) {
        String extension = null
        if (filename) {
            Matcher matcher = (filename =~ /.*\.(.*)$/)
            if (matcher.matches()) {
                extension = (matcher[0][1]).toLowerCase()
            }
        }
        return extension
    }

    /**
     * Given a filename, this function extracts and formats its extension and
     * then validates that it's among the accepted extensions.
     *
     * @param file
     * @param accepted extensions
     * @return
     */
    static boolean validateExtension (String filename, List<String> acceptedExtension) {
        String extension = getFileExtension(filename)
        return isValidExtension(extension, acceptedExtension)
    }

    /**
     * Update the File last time modified only if it exists
     * @param file
     * @return the same file object
     */
    static File touch(File file) {
        if (file && file.exists()) {
            file.setLastModified(System.currentTimeMillis())
        }

        return file
    }

}
