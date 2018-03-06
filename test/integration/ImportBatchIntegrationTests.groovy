import com.tdsops.etl.ETLDomain
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Project
import net.transitionmanager.service.ImportBatchService
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

class ImportBatchIntegrationTests extends Specification {

	ImportBatchTestHelper importBatchTestHelper
	ProjectTestHelper projectTestHelper

	ImportBatchService importBatchService

	def setup() {

		importBatchTestHelper = new ImportBatchTestHelper()
		projectTestHelper = new ProjectTestHelper()
	}

	def "test update batch record"() {
		setup: 'Create an Import batch and a record'
			Project project = projectTestHelper.createProject()
			ImportBatch batch = importBatchTestHelper.createBatch(project, ETLDomain.Application)
			ImportBatchRecord record = importBatchTestHelper.createImportBatchRecord(batch)
			String field1 = 'field1'
			String field1Value = 'some value'
			ImportBatchRecordUpdateCommand cmd = new ImportBatchRecordUpdateCommand()
			Map newValues = [:]
			newValues[field1] = field1Value
			cmd.fieldsInfo = new JSONObject(newValues)
		when: 'Updating the record with some new values'
			importBatchService.updateBatchRecord(project, batch.id, record.id, cmd)
			Map fieldsInfo = record.fieldsInfoAsMap()
		then: 'The record was successfully.'
			fieldsInfo[field1] == field1Value
		when: 'Overriding the previous value'
			String field1UpdatedValue = 'some other value'
			newValues[field1] = field1UpdatedValue
			cmd.fieldsInfo = new JSONObject(newValues)
			importBatchService.updateBatchRecord(project, batch.id, record.id, cmd)
			fieldsInfo = record.fieldsInfoAsMap()
		then: 'The record was successfully updated again.'

			fieldsInfo[field1] == field1UpdatedValue
	}
}
