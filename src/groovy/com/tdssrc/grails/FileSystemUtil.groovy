package com.tdssrc.grails

import groovy.util.logging.Slf4j
import org.springframework.web.multipart.MultipartFile

import java.util.regex.Matcher

@Slf4j
class FileSystemUtil {

    private static final String[] ALLOWED_EXTENSIONS = ["csv", "txt", "json", "xls", "xlsx", "xml"]

    /**
     * Determine if the given extension should be allowed.
     *
     * @param extension
     * @return
     */
    static boolean isValidExtension(String extension) {
        boolean isValid = false
        extension = formatExtension(extension)
        if (extension) {
            isValid = extension in ALLOWED_EXTENSIONS
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
     * Return the file extension for a given file.
     *
     * @param file
     * @return
     */
    static String getFileExtension(MultipartFile file) {
        String extension = null
        if (file) {
            String filename = file.getOriginalFilename()
            Matcher matcher = (filename =~ /.*\.(.*)$/)
            if (matcher.matches()) {
                extension = (matcher[0][1]).toLowerCase()
            }
        }
        return extension
    }

    /**
     * Determine if the given file has a accepted extension.
     *
     * @param file
     * @return
     */
    static boolean validateExtension (MultipartFile file) {
        String extension = getFileExtension(file)
        return isValidExtension(extension)
    }

}
