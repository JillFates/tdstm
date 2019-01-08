package net.transitionmanager.bulk.change

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.domain.TagEvent
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TagAssetService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveEventTestHelper

@Integration
@Rollback
class BulkChangeTagIntegrationSpec extends Specification{
	TagAssetService tagAssetService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	MoveEventTestHelper moveEventTestHelper = new MoveEventTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project

	@Shared
	Project otherProject

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetEntity device3

	@Shared
	Map context

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagAsset tagAsset1

	@Shared
	TagAsset tagAsset2

	@Shared
	TagAsset tagAsset3

	@Shared
	TagAsset tagAsset4

	@Shared
	MoveEvent event

	@Shared
	MoveEvent event2

	@Shared
	TagEvent tagEvent1

	@Shared
	TagEvent tagEvent2

	@Shared
	TagEvent tagEvent3

	@Shared
	Date now

	void setup() {
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()

		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true, failOnError: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true, failOnError: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true, failOnError: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true, failOnError: true)


		event = moveEventTestHelper.createMoveEvent(project)
		event2 = moveEventTestHelper.createMoveEvent(project)

		tagEvent1 = new TagEvent(tag: tag1, event: event).save(flush: true, failOnError: true)
		tagEvent2 = new TagEvent(tag: tag1, event: event2).save(flush: true, failOnError: true)
		tagEvent3 = new TagEvent(tag: tag2, event: event2).save(flush: true, failOnError: true)

		tagAssetService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void 'test validateBulkValues'() {
		when: 'validating tags from the current project, against the current project'
			BulkChangeTag.validateBulkValues(project, [tag1.id, tag2.id])
		then: 'All tags are valid, and no exceptions are thrown'
			noExceptionThrown()
	}

	void 'test validateBulkValues invalid tags'() {
		when: 'validating tags not all from the current project, against the current project'
			BulkChangeTag.validateBulkValues(project, [tag1.id, tag2.id, tag3.id])
		then: 'an EmptyResultException is thrown'
			thrown InvalidParamException
	}

	void 'test bulkAdd'() {
		setup: 'Given new tags, not yet assigned to Assets'
			Tag tag4 = new Tag(name: 'Cyan tag', description: 'Descriptive is it not', color: Color.Cyan, project: project).save(flush: true, failOnError: true)
			Tag tag5 = new Tag(name: 'Orange tag', description: 'This is not a description', color: Color.Orange, project: project).save(flush: true, failOnError: true)
		when: 'bulk adding the new tags to a list of assetIds'
			BulkChangeTag.add(Application.class, [tag4.id, tag5.id], 'tagAssets', [device.id, device2.id])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the assets are associated with the new tags.'
			tagAssets.size() == 3
			tagAssets[0].tag.color == Color.Green
			tagAssets[0].asset.id == device.id
			tagAssets[1].tag.color == Color.Cyan
			tagAssets[1].asset.id == device.id
			tagAssets[2].tag.color == Color.Orange
			tagAssets[2].asset.id == device.id

			tagAssets2.size() == 4
			tagAssets2[0].tag.color == Color.Green
			tagAssets2[0].asset.id == device2.id
			tagAssets2[1].tag.color == Color.Blue
			tagAssets2[1].asset.id == device2.id
			tagAssets2[2].tag.color == Color.Cyan
			tagAssets2[2].asset.id == device2.id
			tagAssets2[3].tag.color == Color.Orange
			tagAssets2[3].asset.id == device2.id
	}

	void 'test bulkAdd filter query'() {
		setup: 'Given new tags, not yet assigned to Assets, and an AssetFilterQuery'
			Tag tag4 = new Tag(name: 'Cyan tag', description: 'Descriptive is it not', color: Color.Cyan, project: project).save(flush: true, failOnError: true)
			Tag tag5 = new Tag(name: 'Orange tag', description: 'This is not a description', color: Color.Orange, project: project).save(flush: true, failOnError: true)

			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
					SELECT AE.id
					FROM AssetEntity AE
					WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
				"""
		when: 'bulk adding the new tags to assents based on the assentFilterQuery'
			BulkChangeTag.add(Application.class, [tag4.id, tag5.id], 'tagAssets', [], [query: query, params: params])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the assets are associated with the new tags.'
			tagAssets.size() == 3
			tagAssets[0].tag.color == Color.Green
			tagAssets[0].asset.id == device.id
			tagAssets[1].tag.color == Color.Cyan
			tagAssets[1].asset.id == device.id
			tagAssets[2].tag.color == Color.Orange
			tagAssets[2].asset.id == device.id

			tagAssets2.size() == 4
			tagAssets2[0].tag.color == Color.Green
			tagAssets2[0].asset.id == device2.id
			tagAssets2[1].tag.color == Color.Blue
			tagAssets2[1].asset.id == device2.id
			tagAssets2[2].tag.color == Color.Cyan
			tagAssets2[2].asset.id == device2.id
			tagAssets2[3].tag.color == Color.Orange
			tagAssets2[3].asset.id == device2.id
	}

	void 'test bulkRemove'() {
		when: 'bulk removing tags from a list of assets'
			BulkChangeTag.remove(Application.class, [tag1.id], 'tagAssets', [device.id, device2.id])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the tags are removed from those assets'
			tagAssets.size() == 0
			tagAssets2.size() == 1
			tagAssets2[0].tag.color == Color.Blue
			tagAssets2[0].asset.id == device2.id
	}

	void 'test bulkRemove filter query'() {
		setup: 'given an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
					SELECT AE.id
					FROM AssetEntity AE
					WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
				"""
		when: 'bulk removing tags based on the assetFilterQuery'
			BulkChangeTag.remove(Application.class, [tag1.id], 'tagAssets', [], [query: query, params: params])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the tags are removed from the assets specifed by the query'
			tagAssets.size() == 0
			tagAssets2.size() == 1
			tagAssets2[0].tag.color == Color.Blue
			tagAssets2[0].asset.id == device2.id
	}

	void 'test bulkClear no tagIds/clear'() {
		when: 'running a bulk remove on a list of assets, not specifying the tags'
			BulkChangeTag.clear(Application.class, [], 'tagAssets', [device.id, device2.id])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'The assets are cleared of all associations with tags.'
			tagAssets.size() == 0
			tagAssets2.size() == 0
	}

	void 'test bulkClear no tagIds/clear filter query'() {
		setup: 'given an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
					SELECT AE.id
					FROM AssetEntity AE
					WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
				"""
		when: 'running a bulk remove on a query filter of assets, not specifying the tags'
			BulkChangeTag.clear(Application.class, [], 'tagAssets', [], [query: query, params: params])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'The assets are cleared of all associations with tags.'
			tagAssets.size() == 0
			tagAssets2.size() == 0
	}

	void 'test bulkReplace'() {

		setup: 'given new tags, not yet assigned to assets'
			Tag tag4 = new Tag(name: 'Cyan tag', description: 'Descriptive is it not', color: Color.Cyan, project: project).save(flush: true, failOnError: true)
			Tag tag5 = new Tag(name: 'Orange tag', description: 'This is not a description', color: Color.Orange, project: project).save(flush: true, failOnError: true)
		when: 'bulk replacing tags on a list of devices, with the new tags'
			BulkChangeTag.replace(Application.class, [tag4.id, tag5.id], 'tagAssets', [device.id, device2.id])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the assets, have the new tags, but not the old ones'
			tagAssets.size() == 2
			tagAssets[0].tag.color == Color.Cyan
			tagAssets[0].asset.id == device.id
			tagAssets[1].tag.color == Color.Orange
			tagAssets[1].asset.id == device.id

			tagAssets2.size() == 2
			tagAssets2[0].tag.color == Color.Cyan
			tagAssets2[0].asset.id == device2.id
			tagAssets2[1].tag.color == Color.Orange
			tagAssets2[1].asset.id == device2.id
	}

	void 'test bulkReplace filter query'() {

		setup: 'given new tags, not yet assigned to assets, and an AssetFilterQuery'
			Tag tag4 = new Tag(name: 'Cyan tag', description: 'Descriptive is it not', color: Color.Cyan, project: project).save(flush: true, failOnError: true)
			Tag tag5 = new Tag(name: 'Orange tag', description: 'This is not a description', color: Color.Orange, project: project).save(flush: true, failOnError: true)
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
					SELECT AE.id
					FROM AssetEntity AE
					WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
				"""
		when: 'bulk replacing tags for assets from the AssetFilterQuery, with the new tags'
			BulkChangeTag.replace(Application.class, [tag4.id, tag5.id], 'tagAssets', [], [query: query, params: params])
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'the assets, have the new tags, but not the old ones'
			tagAssets.size() == 2
			tagAssets[0].tag.color == Color.Cyan
			tagAssets[0].asset.id == device.id
			tagAssets[1].tag.color == Color.Orange
			tagAssets[1].asset.id == device.id

			tagAssets2.size() == 2
			tagAssets2[0].tag.color == Color.Cyan
			tagAssets2[0].asset.id == device2.id
			tagAssets2[1].tag.color == Color.Orange
			tagAssets2[1].asset.id == device2.id
	}

	void 'test coerceBulkValue'() {
		when: 'coercing a string value that contains a list of numbers, that are valid tag ids'
			def value = BulkChangeTag.coerceBulkValue(project, "[${tag1.id},${tag2.id}]")

		then: 'a list of longs is returned'
			value == [tag1.id, tag2.id]
	}

	void 'test coerceBulkValue a list of numbers with an invalid tag id.'() {
		when: 'coercing and '
			BulkChangeTag.coerceBulkValue(project, "[1,${tag2.id}]")

		then: 'an EmptyResultException is returned'
			thrown InvalidParamException
	}

	void 'test coerceBulkValue input false'() {
		when: 'coercing a string value of false'
			BulkChangeTag.coerceBulkValue(project, 'false')

		then: 'InvalidParamException is returned'
			thrown InvalidParamException
	}

	void 'test coerceBulkValue input object'() {
		when: 'coercing a string value of a map'
			BulkChangeTag.coerceBulkValue(project, '{"data": [1,2]}')

		then: 'InvalidParamException is returned'
			thrown InvalidParamException
	}
}
