import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.JsonUtil
import net.transitionmanager.domain.ImportBatch
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.Project

class ImportBatchTestHelper {

	/**
	 * Create an Import Batch with default values.
	 *
	 * @param project
	 * @param domainClass
	 * @return the import batch once saved.
	 */
	ImportBatch createBatch(Project project, ETLDomain domainClass) {
		ImportBatch importBatch = new ImportBatch(
			project: project,
			domainClassName: domainClass
		)
		importBatch.save(flush: true)
		return importBatch
	}

	/**
	 * Create a simple Import Batch Record for the given Import Batch
	 * @param importBatch
	 * @return
	 */
	ImportBatchRecord createImportBatchRecord(ImportBatch importBatch) {
		ImportBatchRecord record = new ImportBatchRecord(
			importBatch: importBatch,
			operation: ImportOperationEnum.INSERT,
			errorList: '[]',
		   fieldsInfo: '{}'
		)
		record.save(flush: true)
		return record
	}

	/**
	 * set Fields for an existing importBatch when testing
	 * @param importBatchRecord
	 * @param fields
	 */
	void setFields(ImportBatchRecord importBatchRecord, Map <String, ?> fields) {
		ImportBatch importBatch =  importBatchRecord.importBatch
		String arrayValues = fields.keySet().collect {
			'"' + it + '"'
		}.join(',')

		importBatch.fieldNameList = "[$arrayValues]"
		importBatch.save(flush: true)


		Map fieldsInfo = fields.collectEntries { k, v ->
			[
					  (k): [
								 value: v
					  ]
			]
		}

		importBatchRecord.fieldsInfo = JsonUtil.convertMapToJsonString(fieldsInfo)
		importBatchRecord.save(flush: true)
	}
}
