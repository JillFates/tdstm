package net.transitionmanager.reporting

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper

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
	Project project

	void setup() {

		assetEntityTestHelper = new AssetEntityTestHelper()
		moveBundleTestHelper = new test.helper.MoveBundleTestHelper()
		projectTestHelper = new test.helper.ProjectTestHelper()
		project = projectTestHelper.createProject()

		//Tag.withTransaction {

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
		app1.save(flush: true)
		app1Sub1.manufacturer = manufacturer1
		app1Sub1.save(flush: true)
		app1Sub2.manufacturer = manufacturer1
		app1Sub2.save(flush: true)
		app1Parent1.manufacturer = manufacturer1
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
		//}
	}

	void 'Test assetsByVendor'() {
		when: 'Calling assetsByVendor'

			List<Map> assetsByVendor = insightService.assetsByVendor(project, 5)

		then: 'The counts for the number of assets by vendor are returned in descending order'
			assetsByVendor == [
				[
					name : 'manufacturer4',
					count: 12
				], [
					name : 'manufacturer3',
					count: 8
				], [
					name : 'manufacturer2',
					count: 8
				], [
					name : 'manufacturer1',
					count: 4
				]
			]
	}


	void 'Test dependenciesByVendor'() {
		when: 'calling dependenciesByVendor'

			List<Map> dependenciesByVendor = insightService.dependenciesByVendor(project, 5)

		then: 'The counts for the number of dependencies by vendor are returned in descending order'
			dependenciesByVendor == [
				[
					name : 'manufacturer4',
					count: 11
				], [
					name : 'manufacturer3',
					count: 7
				], [
					name : 'manufacturer2',
					count: 7
				], [
					name : 'manufacturer1',
					count: 3
				]
			]
	}

	void 'Test topTags'() {
		when: 'calling toTags'

			List<Map> topTags = insightService.topTags(project, 5)

		then: 'The top tags are returned with the count of assets per tag.'
			topTags[0].name == 'tag 4'
			topTags[0].count == 16

			topTags[1].name == 'tag 5'
			topTags[1].count == 12

			topTags[2].name == 'tag 3'
			topTags[2].count == 4

			topTags[3].name == 'tag 1'
			topTags[3].count == 2

			topTags[4].name == 'tag 2'
			topTags[4].count == 1
	}

	void 'Test applicationsGroupedByDependencies'() {
		when: 'calling applicationsGroupedByDependencies'
			List<Map> applicationsGroupedByDependencies = insightService.applicationsGroupedByDependencies(project, 5, 10)


		then: 'the differenct levels of  the counts of dependencies(parent+dependant) are returned'
			applicationsGroupedByDependencies == [
				[
					level: '>5',
					count: 32
				], [
					level: '5>10',
					count: 2
				], [
					level: '10+',
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
