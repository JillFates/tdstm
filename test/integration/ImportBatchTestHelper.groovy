import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.ImportOperationEnum
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
			domainClassName: domainClass,
		   // fieldNameList: '["field1"]'
		)
		importBatch.save(flush: true, failOnError: true)
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
		   fieldsInfo: '[]'
		   // fieldsInfo: '{"field1":{"value":"old value"}}'
		)
		record.save(flush: true, failOnError: true)
		return record
	}
}
