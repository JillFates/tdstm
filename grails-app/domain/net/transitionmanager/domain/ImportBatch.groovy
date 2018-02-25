package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
/**
 * ImportBatch
 *
 * Used to represent a single batch of data that has been imported throught the ETL process that
 * will be processed and inserted, updated, and/or deleted in a target domain within the application.
 */
class ImportBatch {
	// The project that the batch is associated with
	Project project

	// The status of the batch (Pending, Processing, Completed) (is Stopping a status?)
	ImportBatchStatusEnum status = ImportBatchStatusEnum.PENDING

	// The provider from which the import data was derived
	Provider provider

	// The dataScript that transformed the data from the provider
	DataScript dataScript

	// The domain that the batch is going to manipulate (e.g. Application, Dependency, Manufacturer, etc)
	String domainClassName

	// The person whom processed the import/transformation
	Person createdBy

	// Flag if the batch has been archived
	Integer archived = 0

	// The timezone that String versions of dates were formatted in (default GMT). Dates that are not GMT
	// will be parsed and adjusted to GMT as part of the import process. This maybe processed by the ETL process
	// instead and therefore would be unnecessary (TBD).
	String timezone = TimeUtil.defaultTimeZone

	// For dates that are imported as strings this format will be used to parse the String into a Date. This
	// maybe processed by the ETL process instead and therefore would be unnecessary (TBD).
	String dateFormat = ''

	// The reference to the Quartz job which is assigned when Quartz begins processing the batch
	String progressInfoJob

	// The filename that was originally uploaded to the application
	String originalFilename = ''

	// A String value that will be used to indicate that a property should be nulled/blanked out
	String nullIndicator = ''

	// Flag if existing values should be overwritten with blank/null values
	Integer overwriteWithBlanks = 0

	// Flag if the batch should be automatically processed after the import load has been completed
	Integer autoProcess = 0

	// A Datetime value that if set will warn on posting if the domain object was modified after this datetime. This
	// is primarily used with the TM Asset Export spreadsheet so that changes are not overwritten accidently.
	Date warnOnChangesAfter

	// A JSON List of the field names that are referenced in the detail records
	String fieldNameList = '[]'

	// The results of the various import process steps (in HTML format)
	String importResults

	Date dateCreated
	Date lastUpdated

	static belongsTo = [
			dataScript: DataScript,
			project: Project,
			provider: Provider,
			createdBy: Person
	]

	static constraints = {
		archived nullable: false, range: 0..1
		autoProcess nullable: false, range: 0..1
		createdBy nullable: true
		dateFormat nullable: false, size: 0..32
		domainClassName nullable: false, validator: domainValidator
		fieldNameList nullable: false, size: 2..65635
		lastUpdated nullable: true
		originalFilename nullable: false, size: 0..255
		nullIndicator nullable: false, size: 0..255
		progressInfoJob nullable: true
		project  nullable: false
		provider nullable: true
		status nullable: false
		timezone nullable: false, size: 1..255
		warnOnChangesAfter nullable: true
	}

	static mapping = {
		id column: 'import_batch_id'
		archived sqltype: 'TINYINT(1)'
		autoProcess sqltype: 'TINYINT(1)'
		dateFormat sqltype: 'VARCHAR(32)'
		domainClassName sqltype: 'VARCHAR(255)'
		fieldNameList sqltype: 'TEXT'
		progressInfoJob sqltype: 'VARCHAR(255)'
		overwriteWithBlanks sqltype: 'TINYINT(1)'
		originalFilename sqltype: 'VARCHAR(255)'
		nullIndicator sqltype: 'VARCHAR(255)'
		status sqltype: 'VARCHAR(32)'
		timezone sqltype: 'VARCHAR(255)'
		createdBy column: 'created_by'
	}

	/**
	 * Validate that the provided class name is, in fact, a valid Domain Class.
	 */
	static domainValidator = { String value ->
		String domainPackage = "net.transitionmanager.domain."
		boolean isDomain = true
		try {
			Class domain = Class.forName(domainPackage + value)
			isDomain = GormUtil.isDomainClass(domain)
		} catch (ClassNotFoundException e) {
			isDomain = false
		}
		return isDomain
	}

	/**
	 * Return the list of fields as a list.
	 * @return
	 */
	List fieldNameListAsList() {
		return JsonUtil.parseJsonList(fieldNameList)
	}

	/**
	 * Return a map representation for this Import Batch.
	 * @param minimalInfo: if set to true only the id, status and domainClassName will be returned.
	 * @return
	 */
	Map toMap(boolean minimalInfo = false) {
		Map providerMap = provider ? provider.toMap(true) : null
		Map dataScriptMap = dataScript ? dataScript.toMap(true) : null
		Map dataMap = [
			id: id,
			status: status.name(),
			domainClassName: domainClassName
		]
		if (! minimalInfo) {
			Map additionalFields = [
				project: [
						id: project.id,
						name: project.projectCode
				],
				provider: providerMap,
				dataScript: dataScriptMap,
				createdBy: createdBy? createdBy.toString() : null,
				archived: archived,
				timezone: timezone,
				dateFormat: dateFormat,
				progressInfoJob: progressInfoJob,
				originalFilename: originalFilename,
				nullIndicator: nullIndicator,
				overwriteWithBlanks: overwriteWithBlanks,
				autoProcess: autoProcess,
				warnOnChangesAfter: warnOnChangesAfter,
				fieldNameList: fieldNameListAsList(),
				dateCreated: dateCreated,
				lastUpdated: lastUpdated
			]

			dataMap.putAll(additionalFields)
		}

		return dataMap
	}

}
