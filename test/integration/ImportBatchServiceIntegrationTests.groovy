import com.tdsops.etl.ETLDomain
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ImportBatchService
import spock.lang.Specification

class ImportBatchServiceIntegrationTests extends Specification {

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
			Map newValues = [fieldName: field1, value: field1Value]
			cmd.fieldsInfo = [newValues]
		when: 'Updating the record with some new values'
			importBatchService.updateBatchRecord(project, batch.id, record.id, cmd)
			Map fieldsInfo = record.fieldsInfoAsMap()
		then: 'The record was successfully.'
			fieldsInfo[field1].value == field1Value
		when: 'Overriding the previous value'
			String field1UpdatedValue = 'some other value'
			newValues = [fieldName: field1, value: field1UpdatedValue]
			cmd.fieldsInfo = [newValues]
			importBatchService.updateBatchRecord(project, batch.id, record.id, cmd)
			fieldsInfo = record.fieldsInfoAsMap()
		then: 'The record was successfully updated again.'

			fieldsInfo[field1].value == field1UpdatedValue
	}

	def "test get info of batch"() {
		setup: 'Create a project and an import batch'
			Project project = projectTestHelper.createProject()
			ImportBatch batch = importBatchTestHelper.createBatch(project, ETLDomain.Application)
			String info = 'progress'
			String progressKey = 'progress'
			String lastUpdatedKey = 'lastUpdated'
		when: 'retrieving the progress info'
			Map progressInfo = importBatchService.getImportBatchInfo(project, batch.id, info)
		then: 'the progress info is not null'
			progressInfo != null
		and: 'the progress info has the expected keys'
			progressInfo.containsKey(progressKey)
			progressInfo.containsKey(lastUpdatedKey)
		and: 'both values are null'
			progressInfo[progressKey] == null
			progressInfo[lastUpdatedKey] == null
		when: 'updating the progress info'
			Date lastUpdated = TimeUtil.nowGMT()
			Integer progress = 50
			batch.processLastUpdated = lastUpdated
			batch.processProgress = progress
			batch.save(failOnError: true)
			progressInfo = importBatchService.getImportBatchInfo(project, batch.id, info)
		then: 'the progress info was updated with the new values'
			progressInfo[progressKey] == progress
			progressInfo[lastUpdatedKey] == lastUpdated
		when: "requesting info of a batch that doesn't exists"
			importBatchService.getImportBatchInfo(project, -1, info)
		then: 'an EmptyResultException is thrown'
			thrown(EmptyResultException)
		when: 'requesting info of a batch with a different project'
			Project project2 = projectTestHelper.createProject()
			importBatchService.getImportBatchInfo(project2, batch.id, info)
		then: 'an EmptyResultException is thrown'
			thrown(EmptyResultException)
	}
}
