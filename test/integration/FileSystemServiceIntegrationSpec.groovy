import com.tdssrc.grails.FileSystemUtil
import net.transitionmanager.command.FileUploadCommand
import net.transitionmanager.command.UploadTextContentCommand
import net.transitionmanager.service.InvalidParamException
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.See
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidRequestException

class FileSystemServiceIntegrationSpec extends Specification {

	// IOC
	FileSystemService fileSystemService

	def '1. test validateFilename'() {
		when: 'calling a file with valid naming'
			fileSystemService.validateFilename('abc123')
		then: 'no exception should have been thrown'
			true == true

		when: 'calling with a file separator in the name'
			String filename = 'abc' + File.separator + '123'
			fileSystemService.validateFilename(filename)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
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

	def '3. Creating and deleting files from raw input'() {
		when: "Creating a command object with a txt extension and some random content"
			String extension = "TXT"
			UploadTextContentCommand cmd = new UploadTextContentCommand(extension: extension, content: 'Hello, World!')
		then: "The command object pass all validations."
			!cmd.hasErrors()
		when: "Attempting to create a temporary file using this command"
			String filename = fileSystemService.writeTemporaryFileFromRawInput('', cmd)
		then: "No exceptions were thrown"
			noExceptionThrown()
		and: "The method returned a filename"
			filename != null
		and: "The file exists"
			fileSystemService.temporaryFileExists(filename)
		when: "Trying retrieve the actual file"
			File file = fileSystemService.getTemporaryFile(filename, true)
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The file was returned"
			file != null
		and: "The file's extension matches the one used for writing the file."
			FileSystemUtil.getFileExtension(file.getName()) == extension.toLowerCase()
		when: "Attempting to delete the file"
			boolean deleted = fileSystemService.deleteTemporaryFile(filename)
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The flag is true"
			deleted
		and: "The file no longer exists in the file system"
			!fileSystemService.temporaryFileExists(filename)
	}

	def "4. Trying to create a temporary file with invalid input"() {
		when: "The command object is null"
			UploadTextContentCommand cmd = null
			String filename = fileSystemService.writeTemporaryFileFromRawInput(null, cmd)
		then: "An InvalidParamException is thrown"
			thrown(InvalidParamException)
		when: "Trying to write a file with an invalid extension"
			cmd = new UploadTextContentCommand(extension: "jzon", content: "something")
			filename = fileSystemService.writeTemporaryFileFromRawInput(null, cmd)
		then: "An InvalidParamException is thrown"
			thrown(InvalidParamException)
		when: "Trying to create a file with no content"
			cmd = new UploadTextContentCommand(extension: "json")
			filename = fileSystemService.writeTemporaryFileFromRawInput(null, cmd)
		then: "An InvalidParamException is thrown"
			thrown(InvalidParamException)
		when: "Trying to create a file with no extension"
			cmd = new UploadTextContentCommand(content: "something")
			filename = fileSystemService.writeTemporaryFileFromRawInput(null, cmd)
		then: "An InvalidParamException is thrown"
			thrown(InvalidParamException)
	}

	def "5. Create a temporary file from an uploaded file."() {
		setup: "Create a temporary file for this test"
			String extension = "txt"
			UploadTextContentCommand cmd = new UploadTextContentCommand(extension: extension, content: "something important.")
			String filename = fileSystemService.writeTemporaryFileFromRawInput('', cmd)
			File file = fileSystemService.getTemporaryFile(filename, true)
			MockMultipartFile multipartFile = new MockMultipartFile(filename, filename, "text/plain", new FileInputStream(file))
		when: "Uploading a new file using an existing file"
			FileUploadCommand cmd2 = new FileUploadCommand(file: multipartFile)
			String filename2 = fileSystemService.copyToTemporaryFile(cmd2)
		then: "No exception thrown"
			noExceptionThrown()
		and: "The filename for the new file isn't null"
			filename2 != null
		and: "The file was actually created"
			fileSystemService.temporaryFileExists(filename2)
		and: "The file extensions are the same"
			FileSystemUtil.getFileExtension(filename2) == extension
	}

	def "6. Try to create a temporary file from an uploaded one"() {
		when: "Trying to create a file from no input"
			String filename = fileSystemService.copyToTemporaryFile(null)
		then: "InvalidParamException is thrown"
			thrown(InvalidParamException)
		when: "Trying to create a file with an empty command"
			filename = fileSystemService.copyToTemporaryFile(new FileUploadCommand())
		then: "InvalidParamException is thrown"
			thrown(InvalidParamException)
		when: "Creating a file with an invalid extension"
			String extension = "bogus"
			def (String filename2, OutputStream os) = fileSystemService.createTemporaryFile('', extension)
		then: "No exceptions thrown"
			noExceptionThrown()
		and: "The file has the expected extension"
			FileSystemUtil.getFileExtension(filename2) == extension
		when: "Trying to use that file and copy it to a temporary file"
			File file = fileSystemService.getTemporaryFile(filename2, true)
			MockMultipartFile multipartFile = new MockMultipartFile(filename2, filename2, "text/plain", new FileInputStream(file))
			FileUploadCommand cmd = new FileUploadCommand(file: multipartFile)
			filename = fileSystemService.copyToTemporaryFile(cmd)
		then: "InvalidParamException is thrown"
			thrown(InvalidParamException)
	}
}
