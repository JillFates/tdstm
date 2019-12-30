package net.transitionmanager.reporting

import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.action.Provider
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.imports.ImportBatch
import net.transitionmanager.imports.ImportBatchRecord
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.task.AssetComment
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.ImportBatchTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper
import test.helper.TaskTestHelper

@Rollback
@Integration
class InsightServiceIntegrationSpec extends Specification {
	InsightService insightService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper

	@Shared
	MoveBundleTestHelper moveBundleTestHelper

	@Shared
	ProjectTestHelper projectTestHelper

	@Shared
	ImportBatchTestHelper importBatchTestHelper

	@Shared
	MoveEventTestHelper moveEventTestHelper

	@Shared
	TaskTestHelper taskTestHelper

	@Shared
	Project project

	@Shared
	ProviderTestHelper providerHelper

	void setup() {

		assetEntityTestHelper = new AssetEntityTestHelper()
		moveBundleTestHelper = new test.helper.MoveBundleTestHelper()
		projectTestHelper = new test.helper.ProjectTestHelper()
		project = projectTestHelper.createProject()
		importBatchTestHelper = new ImportBatchTestHelper()
		moveEventTestHelper= new MoveEventTestHelper()
		taskTestHelper = new TaskTestHelper()
		providerHelper = new ProviderTestHelper()



		MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)

		Manufacturer manufacturer1 = new Manufacturer(name: 'manufacturer1').save(flush: true)
		Manufacturer manufacturer2 = new Manufacturer(name: 'manufacturer2').save(flush: true)
		Manufacturer manufacturer3 = new Manufacturer(name: 'manufacturer3').save(flush: true)
		Manufacturer manufacturer4 = new Manufacturer(name: 'manufacturer4').save(flush: true)

		Tag tag1 = new Tag(name: 'tag 1', description: 'This is a description', color: Color.Green, project: project).save(flush: true)
		Tag tag2 = new Tag(name: 'tag 2', description: 'Another description', color: Color.Blue, project: project).save(flush: true)
		Tag tag3 = new Tag(name: 'tag 3', description: 'Yet another description', color: Color.Red, project: project).save(flush: true)
		Tag tag4 = new Tag(name: 'tag 4', description: 'Yet another description', color: Color.Yellow, project: project).save(flush: true)
		Tag tag5 = new Tag(name: 'tag 5', description: 'Yet another description', color: Color.Grey, project: project).save(flush: true)

		AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device4 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device5 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device6 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		AssetEntity device7 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)


		MoveEvent event = moveEventTestHelper.createMoveEvent(project, 'test event 1')
		MoveEvent event2 = moveEventTestHelper.createMoveEvent(project, 'test event 2')

		AssetComment assetComment = taskTestHelper.createTask('some comment', event, device, project)
		AssetComment assetComment2 = taskTestHelper.createTask('some comment', event, device2, project)
		AssetComment assetComment3 = taskTestHelper.createTask('some comment', event, device3, project)
		AssetComment assetComment4 = taskTestHelper.createTask('some comment', event, device4, project)
		AssetComment assetComment5 = taskTestHelper.createTask('some comment', event2, device5, project)
		AssetComment assetComment6 = taskTestHelper.createTask('some comment', event2, device6, project)
		AssetComment assetComment7 = taskTestHelper.createTask('some comment', event2, device7, project)



		new AssetDependency(asset: device, dependent: device2).save(flush: true)
		Integer dependencyBundle = 4
		new AssetDependencyBundle(project: project, asset: device, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
		new AssetDependencyBundle(project: project, asset: device2, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)

		new TagAsset(tag: tag1, asset: device).save(flush: true)
		new TagAsset(tag: tag1, asset: device2).save(flush: true)
		new TagAsset(tag: tag2, asset: device2).save(flush: true)

		//Under 5 dependencies
		AssetEntity app1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app1Sub1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app1Sub2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app1Parent1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)

		app1.manufacturer = manufacturer1
		app1.os = 'Windows 98'
		app1.environment = 'Development'
		app1.save(flush: true)
		app1Sub1.manufacturer = manufacturer1
		app1Sub1.os = 'Windows 98'
		app1Sub1.environment = 'Development'
		app1Sub1.save(flush: true)
		app1Sub2.manufacturer = manufacturer1
		app1Sub2.manufacturer = manufacturer1
		app1Sub2.os = 'Windows XP'
		app1Sub2.environment = 'Production'
		app1Sub2.save(flush: true)
		app1Parent1.manufacturer = manufacturer1
		app1Parent1.os = 'Windows XP'
		app1Parent1.environment = 'Production'
		app1Parent1.save(flush: true)


		new AssetDependency(asset: app1, dependent: app1Sub1).save(flush: true)
		new AssetDependency(asset: app1, dependent: app1Sub2).save(flush: true)
		new AssetDependency(asset: app1Parent1, dependent: app1).save(flush: true)

		assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)

		new TagAsset(tag: tag3, asset: app1).save(flush: true)
		new TagAsset(tag: tag3, asset: app1Sub1).save(flush: true)
		new TagAsset(tag: tag3, asset: app1Sub2).save(flush: true)
		new TagAsset(tag: tag3, asset: app1Parent1).save(flush: true)


		//over 5 depdencies but under 10
		AssetEntity app5 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Sub1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Sub2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Sub3 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Sub4 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Sub5 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Parent1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app5Parent2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)

		app5.manufacturer = manufacturer2
		app5.save(flush: true)
		app5Sub1.manufacturer = manufacturer2
		app5Sub1.save(flush: true)
		app5Sub2.manufacturer = manufacturer2
		app5Sub2.save(flush: true)
		app5Sub3.manufacturer = manufacturer2
		app5Sub3.save(flush: true)
		app5Sub4.manufacturer = manufacturer2
		app5Sub4.save(flush: true)
		app5Sub5.manufacturer = manufacturer2
		app5Sub5.save(flush: true)
		app5Parent1.manufacturer = manufacturer2
		app5Parent1.save(flush: true)
		app5Parent2.manufacturer = manufacturer2
		app5Parent2.save(flush: true)

		new AssetDependency(asset: app5, dependent: app5Sub1).save(flush: true)
		new AssetDependency(asset: app5, dependent: app5Sub2).save(flush: true)
		new AssetDependency(asset: app5, dependent: app5Sub3).save(flush: true)
		new AssetDependency(asset: app5, dependent: app5Sub4).save(flush: true)
		new AssetDependency(asset: app5, dependent: app5Sub5).save(flush: true)
		new AssetDependency(asset: app5Parent1, dependent: app5).save(flush: true)
		new AssetDependency(asset: app5Parent2, dependent: app5).save(flush: true)

		new TagAsset(tag: tag4, asset: app5).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Sub1).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Sub2).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Sub3).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Sub4).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Sub5).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Parent1).save(flush: true)
		new TagAsset(tag: tag4, asset: app5Parent2).save(flush: true)

		AssetEntity app6 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Sub1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Sub2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Sub3 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Sub4 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Sub5 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Parent1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app6Parent2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)

		app6.manufacturer = manufacturer3
		app6.save(flush: true)
		app6Sub1.manufacturer = manufacturer3
		app6Sub1.save(flush: true)
		app6Sub2.manufacturer = manufacturer3
		app6Sub2.save(flush: true)
		app6Sub3.manufacturer = manufacturer3
		app6Sub3.save(flush: true)
		app6Sub4.manufacturer = manufacturer3
		app6Sub4.save(flush: true)
		app6Sub5.manufacturer = manufacturer3
		app6Sub5.save(flush: true)
		app6Parent1.manufacturer = manufacturer3
		app6Parent1.save(flush: true)
		app6Parent2.manufacturer = manufacturer3
		app6Parent2.save(flush: true)

		new AssetDependency(asset: app6, dependent: app6Sub1).save(flush: true)
		new AssetDependency(asset: app6, dependent: app6Sub2).save(flush: true)
		new AssetDependency(asset: app6, dependent: app6Sub3).save(flush: true)
		new AssetDependency(asset: app6, dependent: app6Sub4).save(flush: true)
		new AssetDependency(asset: app6, dependent: app6Sub5).save(flush: true)
		new AssetDependency(asset: app6Parent1, dependent: app6).save(flush: true)
		new AssetDependency(asset: app6Parent2, dependent: app6).save(flush: true)

		new TagAsset(tag: tag4, asset: app6).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Sub1).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Sub2).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Sub3).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Sub4).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Sub5).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Parent1).save(flush: true)
		new TagAsset(tag: tag4, asset: app6Parent2).save(flush: true)


		//over 10 depedencies
		AssetEntity app7 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub3 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub4 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub5 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub6 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub7 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub8 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Sub9 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Parent1 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)
		AssetEntity app7Parent2 = assetEntityTestHelper.createAssetEntity(AssetClass.APPLICATION, project, moveBundle)

		app7.manufacturer = manufacturer4
		app7.save(flush: true)
		app7Sub1.manufacturer = manufacturer4
		app7Sub1.save(flush: true)
		app7Sub2.manufacturer = manufacturer4
		app7Sub2.save(flush: true)
		app7Sub3.manufacturer = manufacturer4
		app7Sub3.save(flush: true)
		app7Sub4.manufacturer = manufacturer4
		app7Sub4.save(flush: true)
		app7Sub5.manufacturer = manufacturer4
		app7Sub5.save(flush: true)
		app7Sub6.manufacturer = manufacturer4
		app7Sub6.save(flush: true)
		app7Sub7.manufacturer = manufacturer4
		app7Sub7.save(flush: true)
		app7Sub8.manufacturer = manufacturer4
		app7Sub8.save(flush: true)
		app7Sub9.manufacturer = manufacturer4
		app7Sub9.save(flush: true)
		app7Parent1.manufacturer = manufacturer4
		app7Parent1.save(flush: true)
		app7Parent2.manufacturer = manufacturer4
		app7Parent2.save(flush: true)

		new AssetDependency(asset: app7, dependent: app7Sub1).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub2).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub3).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub4).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub5).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub6).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub7).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub8).save(flush: true)
		new AssetDependency(asset: app7, dependent: app7Sub9).save(flush: true)
		new AssetDependency(asset: app7Parent1, dependent: app7).save(flush: true)
		new AssetDependency(asset: app7Parent2, dependent: app7).save(flush: true)

		new TagAsset(tag: tag5, asset: app7).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub1).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub2).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub3).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub4).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub5).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub6).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub7).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub8).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Sub9).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Parent1).save(flush: true)
		new TagAsset(tag: tag5, asset: app7Parent2).save(flush: true)


		Provider provider1 = providerHelper.createProvider(project, 'provider1')
		Provider provider2 = providerHelper.createProvider(project, 'provider2')
		ImportBatch batch1 = importBatchTestHelper.createBatch(project, ETLDomain.Application, provider1)
		ImportBatch batch2 = importBatchTestHelper.createBatch(project, ETLDomain.Database, provider1)
		ImportBatch batch3 = importBatchTestHelper.createBatch(project, ETLDomain.Device, provider2)
		ImportBatch batch4 = importBatchTestHelper.createBatch(project, ETLDomain.Dependency, provider2)

		ImportBatchRecord record1 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)
		ImportBatchRecord record2 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)
		ImportBatchRecord record3 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)
		ImportBatchRecord record4 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)
		ImportBatchRecord record5 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)
		ImportBatchRecord record6 = importBatchTestHelper.createImportBatchRecord(batch1, 'COMPLETED', 0)

		ImportBatchRecord record7 = importBatchTestHelper.createImportBatchRecord(batch2, 'COMPLETED', 0)
		ImportBatchRecord record8 = importBatchTestHelper.createImportBatchRecord(batch2, 'COMPLETED', 0)
		ImportBatchRecord record9 = importBatchTestHelper.createImportBatchRecord(batch2, 'PENDING', 0)
		ImportBatchRecord record10 = importBatchTestHelper.createImportBatchRecord(batch2, 'PENDING', 0)

		ImportBatchRecord record11 = importBatchTestHelper.createImportBatchRecord(batch3, 'COMPLETED', 0)
		ImportBatchRecord record12 = importBatchTestHelper.createImportBatchRecord(batch3, 'COMPLETED', 0)
		ImportBatchRecord record13 = importBatchTestHelper.createImportBatchRecord(batch3, 'PENDING', 3)
		ImportBatchRecord record14 = importBatchTestHelper.createImportBatchRecord(batch3, 'PENDING', 4)
		ImportBatchRecord record15 = importBatchTestHelper.createImportBatchRecord(batch3, 'COMPLETED', 0)

		ImportBatchRecord record16 = importBatchTestHelper.createImportBatchRecord(batch4, 'PENDING', 1)
		ImportBatchRecord record17 = importBatchTestHelper.createImportBatchRecord(batch4, 'PENDING', 1)
		ImportBatchRecord record18 = importBatchTestHelper.createImportBatchRecord(batch4, 'PENDING', 1)

		ImportBatchRecord record19 = importBatchTestHelper.createImportBatchRecord(batch4, 'COMPLETED', 0)
		ImportBatchRecord record20 = importBatchTestHelper.createImportBatchRecord(batch4, 'COMPLETED', 0)
		ImportBatchRecord record21 = importBatchTestHelper.createImportBatchRecord(batch4, 'PENDING', 5)
		ImportBatchRecord record22 = importBatchTestHelper.createImportBatchRecord(batch4, 'COMPLETED', 0)
		ImportBatchRecord record23 = importBatchTestHelper.createImportBatchRecord(batch4, 'PENDING', 1)
		ImportBatchRecord record24 = importBatchTestHelper.createImportBatchRecord(batch4, 'COMPLETED', 0)

	}

	void 'Test assetsAndDependenciesByProvider'() {
		when: 'calling assetsAndDependenciesByProvider'

			List<Map> assets = insightService.assetsAndDependenciesByProvider(project, 5)

		then: ''
			assets == [
				[
					Name        : 'provider1',
					Dependencies: 0,
					Assets      : 8
				], [
					Name        : 'provider2',
					Dependencies: 4,
					Assets      : 3
				]
			]
	}

	void 'Test topTags'() {
		when: 'calling toTags'

			List<Map> topTags = insightService.topTags(project, 5)

		then: 'The top tags are returned with the count of assets per tag.'
			topTags[0].Name == 'tag 4'
			topTags[0].Count == 16

			topTags[1].Name == 'tag 5'
			topTags[1].Count == 12

			topTags[2].Name == 'tag 3'
			topTags[2].Count == 4

			topTags[3].Name == 'tag 1'
			topTags[3].Count == 2

			topTags[4].Name == 'tag 2'
			topTags[4].Count == 1
	}

	void 'Test assetsByOsAndEnvironment'() {
		when: 'calling assetsByOsAndEnvironment'

			List<Map> assets = insightService.assetsByOsAndEnvironment(project)

		then: 'you get a list of maps including the count of assets broken down by os and environment'
			assets == [
				[
					Count      : 38,
					OS         : null,
					Environment: null
				], [
					Count      : 2,
					OS         : 'Windows 98',
					Environment: 'Development'
				], [
					Count      : 2,
					OS         : 'Windows XP',
					Environment: 'Production'
				]
			]
	}

	void 'Test devicesByEvent'() {
		when: 'calling devicesByEvent'

			List<Map> devices = insightService.devicesByEvent(project, 5)

		then: 'you get a list of maps with the number of devices per event'
			devices == [
				[
					Name   : 'test event 1',
					Devices: 4]
				, [
					Name   : 'test event 2',
					Devices: 3
				]
			]
	}

	void 'Test AssetsByProviderAndAssetType'() {
		when: 'calling AssetsByProviderAndAssetType'

			List<Map> assets = insightService.AssetsByProviderAndAssetType(project)

		then: 'you get a list of maps including the asset type, provider, total number of rows processed, pending and errors.'
			assets == [
				[
					'Asset Type': 'Application',
					Provider    : 'provider1',
					Total       : 6,
					Processed   : 6,
					Pending     : 0,
					Errors      : 0
				], [
					'Asset Type': 'Database',
					Provider    : 'provider1',
					Total       : 4,
					Processed   : 2,
					Pending     : 2,
					Errors      : 0
				], [
					'Asset Type': 'Dependency',
					Provider    : 'provider2',
					Total       : 9,
					Processed   : 4,
					Pending     : 5,
					Errors      : 5
				], [
					'Asset Type': 'Device',
					Provider    : 'provider2',
					Total       : 5,
					Processed   : 3,
					Pending     : 2,
					Errors      : 2
				]
			]
	}

	void 'Test applicationsGroupedByDependencies'() {
		when: 'calling applicationsGroupedByDependencies'
			List<Map> applicationsGroupedByDependencies = insightService.applicationsGroupedByDependencies(project, 5, 10)


		then: 'the different levels of  the counts of dependencies(parent+dependant) are returned'
			applicationsGroupedByDependencies == [
				[
					level: 'Orphaned',
					count: 3
				],
				[
					level: '1-4 dependencies',
					count: 29
				], [
					level: '5-10 dependencies',
					count: 2
				], [
					level: '11+ dependencies',
					count: 1
				]

			]
	}


	void cleanup() {
		AssetEntity.where { project == project }.deleteAll()
		Manufacturer.findByName('manufacturer1').delete(flush: true)
		Manufacturer.findByName('manufacturer2').delete(flush: true)
		Manufacturer.findByName('manufacturer3').delete(flush: true)
		Manufacturer.findByName('manufacturer4').delete(flush: true)
	}
}
