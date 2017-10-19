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

		when: 'deleting the temporary'
			fileSystemService.deleteTemporaryFile(filename)
		then: 'the file should no longer exist'
			! fileSystemService.temporaryFileExists(filename)
	}

}
