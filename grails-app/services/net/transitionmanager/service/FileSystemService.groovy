package net.transitionmanager.service

import com.tdssrc.grails.FileSystemUtil
import grails.transaction.Transactional
import net.transitionmanager.command.FileUploadCommand
import net.transitionmanager.command.UploadTextContentCommand
import org.springframework.beans.factory.InitializingBean
import org.apache.commons.lang3.RandomStringUtils
import groovy.util.logging.Slf4j
import org.springframework.web.multipart.MultipartFile

import javax.management.RuntimeErrorException

/**
 * FileSystemService provides a number of methods to use to interact with the application server file system.
 */
@Transactional(readOnly = true)
@Slf4j
class FileSystemService  implements InitializingBean {

    // The maximum number of tries to get a unique filename so that the getUniqueFilename doens't get into infinite loop
    static final int maxUniqueTries=100

    // The directory that temporary files will be created
    static String temporaryDirectory

    CoreService coreService
    SecurityService securityService

    public void afterPropertiesSet() throws Exception {
        // Load the temporary directory name and make sure that it has the
        String appTempDirectory = coreService.getAppTempDirectory()

        if (! appTempDirectory.endsWith(File.separator)) {
            temporaryDirectory = appTempDirectory + File.separator
        } else {
            temporaryDirectory = appTempDirectory
        }
    }

    /**
     * Used to create a temporary file where the name will consist of a prefix + random + extension
     * @param prefix
     * @param extension
     * @return Calling the method returns both the name of the file as well as the FileOutputStream
     *      String filename
     *      OutputStream output stream
     */
    List createTemporaryFile(String prefix='', String extension='tmp') {
        String filename = getUniqueFilename(temporaryDirectory, prefix, extension)
        OutputStream os = new File(temporaryDirectory + filename).newOutputStream()
        log.info 'Created temporary file {}{}', temporaryDirectory, filename
        return [filename, os]
    }

    /**
     * Used to get a temporary filename where the name will consist of fully qualified path + prefix + random + extension
     * @param prefix
     * @param extension
     * @return the full qualified path and filename
     */
    String getTemporaryFilename(String prefix='', String extension='tmp') {
        String filename = getUniqueFilename(temporaryDirectory, prefix, extension)
        return temporaryDirectory + filename
    }

    /**
     * Used to get the full path to a file in the application temporary directory
     * @param filename
     * @return the fully qualified path and filename of the temporary filename passed or null if not found
     */
    String getTemporaryFullFilename(String filename) {
        temporaryFileExists(filename) ? temporaryDirectory + filename : null
    }

    /**
     * Used to open a previously created temporary file
     * @param filename
     * @return an InputStream to read the file contents if it exists otherwise NULL
     */
    InputStream openTemporaryFile(String filename) {
        InputStream is
        validateFilename(filename)
        if (temporaryFileExists(filename)) {
            is = new File(temporaryDirectory + filename).newInputStream()
        }
        return is
    }

    /**
     *
     * @param filename
     * @param throwException
     * @return
     */
    File getTemporaryFile(String filename, boolean throwException = false) {
        File file = null
        validateFilename(filename)
        if (temporaryFileExists(filename)) {
            file = new File(getTemporaryFullFilename(filename))
        } else if (throwException) {
            throw new RuntimeException("Requested temporary file $filename doesn't exist.")
        }
        return file
    }

    /**
     * Used to determine if a file exists in the temporary directory
     * @param filename
     * @return true if the file exists otherwise false
     */
    boolean temporaryFileExists(String filename) {
        return new File(temporaryDirectory + filename).exists()
    }

    /**
     * Used to get unique filename in the temporary directory
     * @param prefix
     * @param extension
     * @return
     */
    String getUniqueFilename(String directory, String prefix='', String extension='tmp') {
        String filename
        int tries = maxUniqueTries
        if (prefix == null) {
            prefix = ''
        }
        while(true) {
            filename = prefix + RandomStringUtils.randomAlphanumeric(32) + '.' + extension
            if (! temporaryFileExists(filename) ) {
                break
            }
            if (! --tries) {
                log.error 'Failed to generate a unique filename in {} directory', temporaryDirectory
                throw new RuntimeErrorException('getUniqueFilename unable to determine unique filename')
            }
        }
        return filename
    }

    /**
     * Create a temporary file in the system named random + extension
     * a write to it the input given.
     * @param prefix
     * @param extension
     * @param rawInput
     * @return file name for the temporary file.
     */
    String writeTemporaryFileFromRawInput(String prefix, UploadTextContentCommand uploadTextContentCommand) {
        if (uploadTextContentCommand) {
            if (!uploadTextContentCommand.validate()) {
                // This should have been caught in the controller, but it doesn't hurt to double-check
                throw new InvalidParamException("Attempted to create a temporary file using invalid input.")
            }
            String extension = FileSystemUtil.formatExtension(uploadTextContentCommand.extension)
            def (String filename, OutputStream os) = createTemporaryFile(prefix, extension)
            os << uploadTextContentCommand.content
            os.close()
            return filename
        } else {
            throw new InvalidParamException("writeTemporaryFileFromRawInput called with null uploadTextContentCommand.")
        }
    }

    /**
     * Transfer an uploaded file to the temporary directory using the same extension
     * and a randomized filename.
     * @param file
     * @return temp file's name.
     */
    String copyToTemporaryFile(FileUploadCommand fileUploadCommand) {
        String temporaryFilename = null
        if (fileUploadCommand) {
            MultipartFile file = fileUploadCommand.file
            String extension = FileSystemUtil.getFileExtension(file.getOriginalFilename())
            OutputStream os
            try {
                (temporaryFilename, os) = createTemporaryFile('', extension)
                file.transferTo(new File(getTemporaryFullFilename(temporaryFilename)))
            } catch (Exception e) {
                log.error(e.getMessage())
                deleteTemporaryFile(temporaryFilename)
                temporaryFilename = null
                throw new InvalidParamException(e.getMessage())
            }
        }
        return temporaryFilename
    }

    /**
     * Used to delete a temporary file
     * @param filename - the name of the file without any path information
     * @return a flag that the file was actually deleted
     */
    boolean deleteTemporaryFile(String filename) {
        validateFilename(filename)
        boolean success = false
        File file = new File(temporaryDirectory + filename)
        if (file.exists()) {
            success = file.delete()
        }

        log.info 'Deletion of temporary file {}{} {}', temporaryDirectory, filename, (success ? 'succeeded' : 'failed')

        return success
    }

    /**
     * Used by the service to determine that the filename is legitimate in that it does not include any directory
     * references. If it does it will log a security violation and throw an exception.
     * @param filename
     * @throws InvalidRequestException
     */
    private void validateFilename(String filename) {
        if (filename.contains(File.separator)) {
            securityService.reportViolation("attempted to access file with path separator ($filename)")
            throw new InvalidRequestException('Filename contains path separator')
        }
    }
}
