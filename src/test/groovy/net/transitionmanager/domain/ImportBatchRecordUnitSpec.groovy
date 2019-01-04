package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdsops.etl.ETLDomain
import net.transitionmanager.dataImport.SearchQueryHelper
import com.tdssrc.grails.JsonUtil

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Shared
import spock.lang.Unroll

@Title('Tests the ImportBatchRecord Domain class')
class ImportBatchRecordUnitSpec extends Specification {

	@Shared ImportBatchRecord ibr

	void setup() {
		Map fieldsInfoMap = [
			assetName: [
				value: 'abc123',
				init: null,
			],
			description: [
				value: null,
				init: 'The awesome ABC-123 app'
			]
		]
		List fieldNameList = fieldsInfoMap.keySet().toList()
		ImportBatch batch = new ImportBatch(
			fieldNameList: JsonUtil.toJson(fieldNameList),
			domainClassName: ETLDomain.Device
		)

		ibr = new ImportBatchRecord(
				importBatch: batch,
				status: ImportBatchStatusEnum.PENDING,
				operation: ImportOperationEnum.INSERT,
				domainPrimaryId: null,
				sourceRowId: 1,
				errorCount: 0,
				warn: 0,
				duplicateReferences: 0,
				errorList: [],
				fieldsInfo: JsonUtil.toJson(fieldsInfoMap)
			)
	}

	def 'Test errorListAsList method'() {
		when: 'errorList is empty'
			ImportBatchRecord r = new ImportBatchRecord(errorList:'')
		then: 'an empty list should be returned'
			[] == r.errorListAsList()

		when: 'errorList contains JSON list of errors'
			r.errorList = '["abc", "123" ]'
			List list = r.errorListAsList()
		then: 'a list of values should be returned'
			2 == list.size()
		and: 'the contents are as expected'
			'abc' == list[0]
			'123' == list[1]
	}

	def 'Test toMap with minimalInfo == true'() {

		when: 'calling toMap with minimalInfo == false'
			Map result = ibr.toMap(true)
		then: 'should have values expected'
			with (result) {
				operation == ImportOperationEnum.INSERT.name()
				currentValues.assetName == 'abc123'
				domainPrimaryId == null
				init.description == 'The awesome ABC-123 app'
				sourceRowId == 1
				errorCount == 0
				warn == 0
				status.code == ImportBatchStatusEnum.PENDING.name()
				status.label == ImportBatchStatusEnum.PENDING.toString()
			}
	}

	def 'Test toMap with minimalInfo == true and no asset is found'() {

		when: 'SearchQueryHelper.findEntityByMetaData()has been mocked to return null'
			GroovyMock(SearchQueryHelper, global: true)
			SearchQueryHelper.findEntityByMetaData(_, _, _) >> { return null}
		and: 'the operation is set to UPDATE'
			ibr.operation = ImportOperationEnum.UPDATE
		and: 'calling toMap with minimalInfo == false'
			Map result = ibr.toMap(false)
		then: 'should have values expected'
			with (result) {
				domainPrimaryId == null
				sourceRowId == 1
				errorCount == 0
				warn == 0
				status.code == ImportBatchStatusEnum.PENDING.name()
				status.label == ImportBatchStatusEnum.PENDING.toString()
			}
		and: 'should have an errorList'
			[] == result.errorList
		and: 'fieldsInfo should be populated as expected'
			result.containsKey('fieldsInfo')
			(result.fieldsInfo instanceof Map)
			result.fieldsInfo.assetName.value == 'abc123'
			result.fieldsInfo.description.init == 'The awesome ABC-123 app'
		and: 'the operation should have been reset to INSERT'
			result.operation == ImportOperationEnum.INSERT.name()
	}

	def 'Test toMap with minimalInfo == true and with an asset that has errors'() {

		when: 'SearchQueryHelper.findEntityByMetaData()has been mocked to return null'
			GroovyMock(SearchQueryHelper, global: true)
			SearchQueryHelper.findEntityByMetaData(_, _, _) >> { return new AssetEntity(assetName:'xray01', description:'Nothing Special') }
		and: 'the operation is set to INSERT'
			ibr.operation = ImportOperationEnum.INSERT
		and: 'the batch record has errors'
			ibr.errorList = '["crap - something broke"]'
		and: 'calling toMap with minimalInfo == false'
			Map result = ibr.toMap(false)
		then: 'should have values expected'
			with (result) {
				domainPrimaryId == null
				sourceRowId == 1
				errorCount == 0
				warn == 0
				status.code == ImportBatchStatusEnum.PENDING.name()
				status.label == ImportBatchStatusEnum.PENDING.toString()
			}
		and: 'should have an errorList with an error'
			['crap - something broke'] == result.errorList
		and: 'fieldsInfo should be populated as expected'
			result.containsKey('fieldsInfo')
			(result.fieldsInfo instanceof Map)
			result.fieldsInfo.assetName.value == 'abc123'
			result.fieldsInfo.description.init == 'The awesome ABC-123 app'
		and: 'the operation should have been changed to UPDATE'
			result.operation == ImportOperationEnum.UPDATE.name()
		and: 'existingRecord should be populated'
			'xray01' == result.existingRecord.assetName
			'Nothing Special' == result.existingRecord.description
	}
}
