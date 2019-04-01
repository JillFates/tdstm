package net.transitionmanager.imports

import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.action.Provider
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project

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
	//
	ETLDomain domainClassName

	// The person whom processed the import/transformation
	Person createdBy

	// Flag if the batch has been archived (0-false/1-true, default 0)
	Integer archived = 0

	// Flag if the batch should be automatically processed after the import load has been completed (0-false/1-true, default 0)
	Integer autoProcess = 0

	// For dates that are imported as strings this format will be used to parse the String into a Date. This
	// maybe processed by the ETL process instead and therefore would be unnecessary (TBD).
	String dateFormat = ''

	// A JSON List of the field names that are referenced in the detail records
	String fieldNameList = '[]'

	// A JSON onject with a list of the field names and its field labels values
	String fieldLabelMap = '{}'

	// The results of the various import process steps (in HTML format)
	String importResults = ''

	// The reference to the job that which is assigned when Quartz begins processing the batch
	String progressInfoJob

	// Will contain the percentage complete for the process posting progress (0..100)
	Integer processProgress

	// Will contain the last time that the processProgress was last updated
	Date processLastUpdated

	// Will contain the time when the batch was queued to be processed
	Date queuedAt

	// Will contain the username that queued the batch
	String queuedBy

	// Is used to trigger the halting of a batch being processed
	Integer processStopFlag

	// The filename that was originally uploaded to the application
	String originalFilename = ''

	// A String value that will be used to indicate that a property should be nulled/blanked out.
	// In the legacy import 'NULL' is used to clear out values.
	String nullIndicator = ''

	// Flag if existing values should be overwritten with blank/null values (0-false/1-true)
	Integer overwriteWithBlanks = 0

	// The timezone that String versions of dates were formatted in (default GMT). Dates that are not GMT
	// will be parsed and adjusted to GMT as part of the import process. This maybe processed by the ETL process
	// instead and therefore would be unnecessary (TBD).
	String timezone = TimeUtil.defaultTimeZone

	// A Datetime value that if set will warn on posting if the domain object was modified after this datetime. This
	// is primarily used with the TM Asset Export spreadsheet so that changes are not overwritten accidently.
	Date warnOnChangesAfter

	Date dateCreated
	Date lastUpdated

	static belongsTo = [
		createdBy: Person,
		dataScript: DataScript,
		project: Project,
		provider: Provider
	]

	static constraints = {
		archived range: 0..1
		autoProcess range: 0..1
		createdBy nullable: true
		dateFormat nullable: true, blank:true, size: 0..32
		dataScript nullable: true
		fieldNameList size: 2..65635
		fieldLabelMap size: 2..65635
		lastUpdated nullable: true
		originalFilename blank:true, size: 0..255
		nullIndicator nullable:true, blank:true, size: 0..255
		processProgress nullable: true, range: 0..100
		processLastUpdated nullable: true
		queuedAt nullable: true
		queuedBy nullable: true, size: 0..50
		processStopFlag nullable: true, range: 0..1
		progressInfoJob nullable: true
		provider nullable: true
		timezone size: 1..255
		warnOnChangesAfter nullable: true
	}

	static mapping = {
		id column: 'import_batch_id'
		archived sqltype: 'TINYINT(1)'
		autoProcess sqltype: 'TINYINT(1)'
		dateFormat sqltype: 'VARCHAR(32)'
		domainClassName sqltype: 'VARCHAR(32)', enumType: 'String'
		fieldNameList sqltype: 'TEXT'	// JSON
		fieldLabelMap sqltype: 'TEXT'
		overwriteWithBlanks sqltype: 'TINYINT(1)'
		originalFilename sqltype: 'VARCHAR(255)'
		processProgress sqltype: 'TINYINT(3)'
		processLastUpdated sqltype: 'DATETIME'
		queuedAt sqltype: 'DATETIME'
		queuedBy sqltype: 'VARCHAR(50)'
		processStopFlag sqltype: 'TINYINT(1)'
		progressInfoJob sqltype: 'VARCHAR(255)'
		nullIndicator sqltype: 'VARCHAR(255)'
		status sqltype: 'VARCHAR(32)', enumType: "String"
		timezone sqltype: 'VARCHAR(255)'
		createdBy column: 'created_by'
	}

	/**
	 * Return the list of fields as a list.
	 * @return
	 */
	List fieldNameListAsList() {
		return JsonUtil.parseJsonList(fieldNameList)
	}

	/**
	 * Return the list of fields as a list.
	 * @return
	 */
	Map fieldLabelMapAsJsonMap() {
		return JsonUtil.parseJson(fieldLabelMap)
	}

	/**
	 * Return a map representation for this Import Batch.
	 * @param minimalInfo: if set to true only the id, status and domainClassName will be returned.
	 * @return
	 */
	Map toMap(boolean minimalInfo = false) {
		Map providerMap = provider ? provider.toMap(true) : null
		Map dataScriptMap = dataScript ? dataScript.toMap(DataScript.MINIMAL_INFO) : null
		Map dataMap = [
			id: id,
			status: [
				code: status.name(),
				label: status.toString()
			],
			domainClassName: domainClassName.name()
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
				fieldLabelMap: fieldLabelMapAsJsonMap(),
				queuedAt: queuedAt,
				queuedBy: queuedBy,
				dateCreated: dateCreated,
				lastUpdated: lastUpdated
			]

			dataMap.putAll(additionalFields)
		}

		return dataMap
	}

	// TODO : JPM 2/2018 : When using this setter the assignments were NOT working correctly
	//
	// Setter functions to deal with the JSON properties
	//
	// void setFieldNameList(Object value) {
	// 	this.fieldNameList = JsonUtil.toJson(value)
	// }

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

}
