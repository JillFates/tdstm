import com.tdssrc.grails.FileSystemUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.UploadFileCommand
import net.transitionmanager.command.UploadTextCommand
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.exception.InvalidParamException
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

@Integration
@Rollback
class FileSystemServiceIntegrationSpec extends Specification {

	// IOC
	FileSystemService fileSystemService

	def '1. test validateFilename'() {
		when: 'calling a file with valid naming'
			fileSystemService.validateFilename('abc123')
		then: 'no exception should have been thrown'
			noExceptionThrown()

		when: 'calling with a file separator in the name'
			String filename = 'abc' + File.separator + '123'
			fileSystemService.validateFilename(filename)
		then: 'an exception should be thrown'
			thrown InvalidParamException
	}

	def '2. Creating and delete files'() {
		setup:
			String message = 'Hello World!'
			String suffix = 'abc'
			String extension = 'xyz'

		when: 'calling createTemporaryFile'
			def (String filename, OutputStream os) = fileSystemService.createTemporaryFile(suffix, extension)
		then: 'the filename should start with suffix'
			filename?.startsWith(suffix)
		and: 'the filename has the specified extension'
			filename?.endsWith('.' + extension)
		and: 'there is an output stream'
			os
		and: 'can write to the output stream'
			os << message

		when: 'closing the temporary file'
			os.close()
		then: 'the file should exist'
			fileSystemService.temporaryFileExists(filename)

		when: 'the temporary file is deleted'
			fileSystemService.deleteTemporaryFile(filename)
		then: 'the file should no longer exist'
			! fileSystemService.temporaryFileExists(filename)
	}

	def '3. Creating and deleting files from text upload'() {
		when: "Creating a command object with a txt extension and some random content"
			String extension = "csv"
			String fileContent = 'Hello, World!'
			UploadTextCommand cmd = new UploadTextCommand(extension: extension, content: fileContent)
		then: "The command object pass all validations."
			!cmd.hasErrors()
		when: "Attempting to create a temporary file using this command"
			String filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "No exceptions were thrown"
			noExceptionThrown()
		and: "The method returned a filename"
			filename != null
		and: "The file exists"
			fileSystemService.temporaryFileExists(filename)
		when: "Trying retrieve the actual file"
			InputStream is = fileSystemService.openTemporaryFile(filename)
			String readContent = is.getText()
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The file's content matches the original content."
			fileContent == readContent
		and: "The file's extension matches the one used for writing the file."
			FileSystemUtil.getFileExtension(filename) == extension.toLowerCase()
		when: "Attempting to delete the file"
			boolean deleted = fileSystemService.deleteTemporaryFile(filename)
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The flag is true"
			deleted
		and: "The file no longer exists in the file system"
			!fileSystemService.temporaryFileExists(filename)
	}

	def "4. Testing uploadText with invalid input"() {
		when: "The command object is null"
			UploadTextCommand cmd = null
			String filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "null is returned."
			filename == null
		when: "Trying to write a file with an invalid extension"
			cmd = new UploadTextCommand(extension: "jzon", content: "something")
			filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "the filename is null"
			filename == null
		and: "the command object has validation errors"
			cmd.errors.hasFieldErrors("extension")
		when: "Trying to create a file with no content"
			cmd = new UploadTextCommand(extension: "json")
			filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "The filename is null"
			filename == null
		and: "the field content in the command has errors"
			cmd.errors.hasFieldErrors("content")
		when: "Trying to create a file with no extension"
			cmd = new UploadTextCommand(content: "something")
			filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "The command has errors for the field 'extension'"
			cmd.errors.hasFieldErrors('extension')
	}

	def "5. Test uploadFile with valid inputs"() {
		setup: "Set required objects for the test"
			String extension = "csv"
			String uploadFileName = "mockfile.${extension}"
			String content = "Hello, World!"
			MultipartFile multiPartFile = new MockMultipartFile(uploadFileName, uploadFileName, "text/plain", content.getBytes())
			UploadFileCommand cmd = new UploadFileCommand(file: multiPartFile)
		when: "Uploading the file to the system"
			String filename = fileSystemService.transferFileToFileSystem(cmd)

		then: "The command doesn't have errors"
			!cmd.hasErrors()
		and: "A filename was returned"
			filename != null
		and: "That file actually exists"
			fileSystemService.temporaryFileExists(filename)
		and: "The extension matches the original file's extension"
			extension == FileSystemUtil.getFileExtension(filename)
		and: "The content of the saved file matches the content in the MultiPartFile"
			fileSystemService.openTemporaryFile(filename).getText() == content
		when: "Attempting to delete the file"
			boolean deleted = fileSystemService.deleteTemporaryFile(filename)
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The flag is true"
			deleted
		and: "The file no longer exists in the file system"
			!fileSystemService.temporaryFileExists(filename)
	}

	def "6. Text uploadFile with invalid input"() {
		setup: "Set the required objects for this feature."
			String uploadFileName = "mockfile.xtx"
			String content = "Hello, World!"
			MultipartFile multiPartFile = new MockMultipartFile(uploadFileName, uploadFileName, "text/plain", content.getBytes())
			UploadFileCommand cmd = new UploadFileCommand(file: multiPartFile)
		when: "Trying to upload this file to the system"
			String filename = fileSystemService.transferFileToFileSystem(cmd)
		then: "The command doesn't pass the validation"
			cmd.hasErrors()
		and: "The filename is null"
			filename == null
	}

	def '7. Creating and renaming files'() {
		setup:
			String suffix = 'abc'
			String extension = 'xyz'
			String renameToFilename = fileSystemService.getUniqueFilename('')
		when: 'calling createTemporaryFile'
			def (String filename, OutputStream os) = fileSystemService.createTemporaryFile(suffix, extension)
			os.close()
		then: 'the file can be renamed'
			fileSystemService.renameTemporaryFile(filename, renameToFilename)
		when: 'calling create a second temporaryFile'
			def (String secondFilename, OutputStream secondOS) = fileSystemService.createTemporaryFile(suffix, extension)
			secondOS.close()
			fileSystemService.renameTemporaryFile(secondFilename, renameToFilename)
		then: 'exception should be thrown because renameToFilename already exists'
			thrown InvalidParamException
		when: 'the temporary file is deleted'
			fileSystemService.deleteTemporaryFile(renameToFilename)
			fileSystemService.deleteTemporaryFile(secondFilename)
		then: 'the file should no longer exist'
			! fileSystemService.temporaryFileExists(renameToFilename)
			! fileSystemService.temporaryFileExists(secondFilename)
	}
}
