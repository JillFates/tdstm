package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import net.transitionmanager.dataImport.SearchQueryHelper
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges


@TestFor(AssetEntity)
@TestMixin([DomainClassUnitTestMixin])
class ETLFetchSpec extends ETLBaseSpec {

	ETLProcessor processor
	ETLFieldsValidator validator
	Long projectId = 54321
	Project project

	def setup() {
		validator = createDomainClassFieldsValidator()
		project = Mock()
		project.getId() >> projectId

		processor = new ETLProcessor(
			project,
			Mock(DataSetFacade),
			Mock(DebugConsole),
			validator)
	}

	@Unroll
	void 'test can create a fetch command using domain #domainETL and property #propertyName'() {

		setup:
			processor.domain domainETL
			FetchFacade fetch = new FetchFacade(processor, propertyName)

		expect:
			fetch.propertyName == value

		where:
			domainETL        | propertyName   || value
			ETLDomain.Device | 'id'           || 'id'
			ETLDomain.Device | 'Id'           || 'id'
			ETLDomain.Device | 'Manufacturer' || 'manufacturer'
			ETLDomain.Device | 'manufacturer' || 'manufacturer'
			ETLDomain.Device | 'Model'        || 'model'
			ETLDomain.Device | 'model'        || 'model'
			ETLDomain.Rack   | 'room'         || 'room'
	}

	void 'test can throw an ETLProcessorException trying to fetch with an invalid domain field'() {

		given:
			processor.domain ETLDomain.Device

		when: 'It tries to create a fetch using an incorrect property reference'
			new FetchFacade(processor, 'App Owner')

		then: 'It throws an Exception because fetch command was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'The domain Device does not have field name: App Owner'

		when: 'It tries to create a fetch using an incorrect property reference'
			new FetchFacade(processor, 'ipAddress')

		then: 'It throws an Exception because fetch command was incorrectly configured'
			e = thrown ETLProcessorException
			e.message == 'ipAddress is not a domain reference for Device'
	}

	@Unroll
	void 'test can configure fetch with domain #domainETL and fields #fields'() {
		setup:
			processor.domain domainETL
			FetchFacade fetch = new FetchFacade(processor, propertyName)
			fetch.fields fields

		expect:
			fetch.fieldNames.size() == fields.size()

		where:
			domainETL        | propertyName   || fields
			ETLDomain.Device | 'id'           || ['assetTag', 'Device Type']
			ETLDomain.Device | 'Id'           || ['IP Address', 'Maint Expiration', 'Priority']
			ETLDomain.Device | 'Manufacturer' || ['Rail Type']
			ETLDomain.Device | 'manufacturer' || ['Rate Of Change', 'Scale', 'Serial #']
			ETLDomain.Device | 'Model'        || ['Source Blade Position']
			ETLDomain.Device | 'model'        || ['Truck']
			ETLDomain.Rack   | 'room'         || ['source', 'location', 'tag']
			ETLDomain.Room   | 'id'           || ['roomName', 'location', 'address']
	}

	@ConfineMetaClassChanges([SearchQueryHelper])
	void 'test can fetch results by find command retrieving a list of fields'() {

		given: 'a domain defined and a loaded element'
			processor.domain ETLDomain.Device
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)
			// load 'manufacturer' with 'PM10'
			processor.load('manufacturer').with('PM10')

		and: 'a find element'
			ETLFindElement find = new ETLFindElement(processor, ETLDomain.Model, 1)
			find.by 'manufacturer' with 'PM10' into 'manufacturer'
			processor.pushIntoStack find
			processor.currentFindElement = find

		and:
			mockFor(SearchQueryHelper)
			SearchQueryHelper.metaClass.static.findEntityByMetaData = { String fieldName, Map fieldsInfo, Map context, Object entityInstance ->
				assert context.project.getId() == projectId
				assert context.domainClass == Model
				assert fieldName == 'model'
				assert fieldsInfo['manufacturer'].value == 'PM10'

				Model model = new Model(modelName: 'PM10', usize: 1)
				return new AssetEntity(assetClass: AssetClass.DEVICE, assetName: 'A1 PDU1 A', priority: 2, model: model)
			}

		when: 'a fetch command that is is configured with fields'
			Map myVar = new FetchFacade(processor, 'Model').set 'myVar'

		then:
			with(myVar, Map) {
				assetName == 'A1 PDU1 A'
				priority == 2

				with(model) {
					modelName == 'PM10'
					usize == 1
				}
			}
	}

	@ConfineMetaClassChanges([SearchQueryHelper])
	void 'test can fetch results by ID'() {

		given: 'a domain already defined in an ETL processor instance'
			processor.domain ETLDomain.Device
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)

		and: 'an extracted and loaded id value'
			Long deviceId = 123456l
			processor.load('Id').with(deviceId)

		and: 'a mocked instance of SearchQueryHelper'
			mockFor(SearchQueryHelper)
			SearchQueryHelper.metaClass.static.findEntityByMetaData = { String fieldName, Map fieldsInfo, Map context, Object entityInstance ->
				assert context.project.getId() == projectId
				assert context.domainClass == AssetEntity
				assert fieldName == 'id'
				assert fieldsInfo['id'].originalValue == deviceId
				assert fieldsInfo['id'].value == deviceId

				return new AssetEntity(id: deviceId, assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', priority: 2)
			}

		when: 'a fetch command for the id property that it is not configured with fields is set it in a local var'
			Map myVar = new FetchFacade(processor, 'id').set 'myVar'

		then: 'results contains field values previous defined in Fetch command'
			with(myVar, Map) {
				id == deviceId
			}
	}

	@ConfineMetaClassChanges([SearchQueryHelper])
	void 'test can fetch results by ID defining fields'() {

		given: 'a domain already defined in an ETL processor instance'
			processor.domain ETLDomain.Device
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)

		and: 'an extracted and loaded id value'
			Long deviceId = 123456l
			processor.load('Id').with(deviceId)

		and: 'a mocked instance of SearchQueryHelper'
			mockFor(SearchQueryHelper)
			SearchQueryHelper.metaClass.static.findEntityByMetaData = { String fieldName, Map fieldsInfo, Map context, Object entityInstance ->
				assert context.project.getId() == projectId
				assert context.domainClass == AssetEntity
				assert fieldName == 'id'
				assert fieldsInfo['id'].originalValue == deviceId
				assert fieldsInfo['id'].value == deviceId

				return new AssetEntity(id: deviceId, assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', priority: 2)
			}

		when: 'a fetch command for the id property taht it is configured with fields is set it in a local var'
			Map myVar = new FetchFacade(processor, 'id').fields 'assetName', 'priority' set 'myVar'

		then: 'results contains field values previous defined in Fetch command'
			with(myVar, Map) {
				assetName == 'ACMEVMPROD01'
				priority == 2
			}
	}

	@ConfineMetaClassChanges([SearchQueryHelper])
	void 'test can fetch results and use from binding context'() {

		given: 'a domain already defined in an ETL processor instance'
			processor.domain ETLDomain.Device
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)

		and: 'an extracted and loaded id value'
			Long deviceId = 123456l
			processor.load('Id').with(deviceId)

		and:
			mockFor(SearchQueryHelper)
			SearchQueryHelper.metaClass.static.findEntityByMetaData = { String fieldName, Map fieldsInfo, Map context, Object entityInstance ->
				assert context.project.getId() == projectId
				assert context.domainClass == AssetEntity
				assert fieldName == 'id'
				assert fieldsInfo['id'].value == deviceId

				return new AssetEntity(id: deviceId, assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', priority: 2)
			}

		and: 'a fetch command'
			FetchFacade fetch = new FetchFacade(processor, 'id')

		when: 'It is configured with fields'
			Map myVar = fetch.fields 'assetName', 'priority' set 'myVar'

		then:
			with(myVar, Map) {
				assetName == 'ACMEVMPROD01'
				priority == 2
			}
	}

	@ConfineMetaClassChanges([SearchQueryHelper])
	void 'test can fetch results by Alternate Key'() {

		given: 'a domain already defined in an ETL processor instance'
			processor.domain ETLDomain.Device
			processor.iterateIndex = new IterateIndex(0)
			processor.currentRow = new Row([], processor)

		and: 'an extracted and loaded Manufacturer'
			Manufacturer manufacturer = new Manufacturer(id: 123456, name: 'Avocent Biteme', description: 'Avocent Biteme', corporateName: 'HP')
			// command: "load 'Manufacturer' with 'Avocent Biteme' "
			processor.load('Manufacturer').with('Avocent Biteme')

		and:
			mockFor(SearchQueryHelper)
			SearchQueryHelper.metaClass.static.findEntityByMetaData = { String fieldName, Map fieldsInfo, Map context, Object entityInstance ->
				assert context.project.getId() == projectId
				assert context.domainClass == AssetEntity
				assert fieldName == 'manufacturer'
				assert fieldsInfo['manufacturer'].value == 'Avocent Biteme'

				return manufacturer
			}

		when: 'fetch command is executed'
			// command: " fetch 'Manufacturer' set 'myVar' "
			Map myVar = new FetchFacade(processor, 'Manufacturer').set 'myVar'

		then:
			with(myVar, Map) {
				id == manufacturer.id
			}
	}
}
