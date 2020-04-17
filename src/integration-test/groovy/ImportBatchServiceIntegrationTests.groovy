import com.tdsops.etl.ETLDomain
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.ImportBatchRecordFieldUpdateCommand
import net.transitionmanager.command.ImportBatchRecordUpdateCommand
import net.transitionmanager.command.importBatch.ListImportBatchesCommand
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.imports.ImportBatch
import net.transitionmanager.imports.ImportBatchRecord
import net.transitionmanager.imports.ImportBatchService
import net.transitionmanager.project.Project
import spock.lang.Specification
import test.helper.ImportBatchTestHelper

@Integration
@Rollback
class ImportBatchServiceIntegrationTests extends Specification {

	ImportBatchTestHelper importBatchTestHelper
	ProjectTestHelper     projectTestHelper

    ImportBatchService importBatchService

    def setup() {

        importBatchTestHelper = new ImportBatchTestHelper()
        projectTestHelper = new ProjectTestHelper()
    }

    def "test update batch record"() {
        setup: 'Create an Import batch and a record'

            String field1 = 'field1'
            String field1Value = 'some value'

            Project project = projectTestHelper.createProject()
            ImportBatch batch = importBatchTestHelper.createBatch(project, ETLDomain.Application)
            ImportBatchRecord record = importBatchTestHelper.createImportBatchRecord(batch)
            importBatchTestHelper.setFields(record, [
                    (field1): 'old value'
            ])


            ImportBatchRecordUpdateCommand cmd = new ImportBatchRecordUpdateCommand()
            ImportBatchRecordFieldUpdateCommand newValues = new ImportBatchRecordFieldUpdateCommand(fieldName: field1, value: field1Value)
            cmd.fieldsInfo = [newValues]
        when: 'Updating the record with some new values'
            importBatchService.updateBatchRecord(project, batch.id, record.id, cmd)
            Map fieldsInfo = record.fieldsInfoAsMap()
        then: 'The record was successfully.'
            fieldsInfo[field1].value == field1Value
        when: 'Overriding the previous value'
            String field1UpdatedValue = 'some other value'
            newValues = new ImportBatchRecordFieldUpdateCommand(fieldName: field1, value: field1UpdatedValue)
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
            batch.save()
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

    void 'test can list ImportBatchRecord records for a project'() {

        setup: 'a created project'
            Project project = projectTestHelper.createProject()

        and: '2 ImportBatch with 1 ImportBatchRecord each one'
            ImportBatch batch1 = importBatchTestHelper.createBatch(project, ETLDomain.Application)
            ImportBatchRecord importBatchRecord1 = importBatchTestHelper.createImportBatchRecord(batch1)
            importBatchTestHelper.setFields(importBatchRecord1, [
                    'appName': 'old value'
            ])

            ImportBatch batch2 = importBatchTestHelper.createBatch(project, ETLDomain.Device)
            ImportBatchRecord importBatchRecord2 = importBatchTestHelper.createImportBatchRecord(batch2)
            importBatchTestHelper.setFields(importBatchRecord2, [
                    'appName': 'old value'
            ])

        when: 'list batches is invoked'
            Collection batches = importBatchService.listBatches(project, new ListImportBatchesCommand(groupGuid: null))

        then: 'collection results contains all batch record created in setup'
            batches.size() == 2
            batches*.id == [batch1.id, batch2.id]

    }


    void 'test can list ImportBatchRecord records for a project and a groupGuid'() {

        setup: 'a created project'
            String uuid = StringUtil.generateGuid()
            Project project = projectTestHelper.createProject()

        and: '1 ImportBatch with 1 ImportBatchRecord'
            ImportBatch batch1 = importBatchTestHelper.createBatch(project, ETLDomain.Application)
            ImportBatchRecord importBatchRecord1 = importBatchTestHelper.createImportBatchRecord(batch1)
            importBatchTestHelper.setFields(importBatchRecord1, [
                    'appName': 'old value'
            ])

        and: '1 ImportBatch using groupGuid with 1 ImportBatchRecord'
            ImportBatch batch2 = importBatchTestHelper.createBatch(project, ETLDomain.Device, uuid)
            ImportBatchRecord importBatchRecord2 = importBatchTestHelper.createImportBatchRecord(batch2)
            importBatchTestHelper.setFields(importBatchRecord2, [
                    'appName': 'old value'
            ])

        when: 'list batches is invoked'
            def batches = importBatchService.listBatches(project, new ListImportBatchesCommand(groupGuid: uuid))

        then: 'collection results contains onlu groupoGuid batch record associated in setup'
            batches.size() == 1
            batches*.id == [batch2.id]
    }
}
