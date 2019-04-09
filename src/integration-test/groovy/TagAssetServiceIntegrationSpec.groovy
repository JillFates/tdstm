import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.tag.TagEvent
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.tag.TagAssetService
import net.transitionmanager.tag.TagEventService
import net.transitionmanager.tag.TagService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveEventTestHelper

@Integration
@Rollback
class TagAssetServiceIntegrationSpec extends Specification{
	TagService      tagService
	TagAssetService tagAssetService
	TagEventService tagEventService

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

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true)


		event = moveEventTestHelper.createMoveEvent(project)
		event2 = moveEventTestHelper.createMoveEvent(project)

		tagEvent1 = new TagEvent(tag: tag1, event: event).save(flush: true)
		tagEvent2 = new TagEvent(tag: tag1, event: event2).save(flush: true)
		tagEvent3 = new TagEvent(tag: tag2, event: event2).save(flush: true)

		tagService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		tagAssetService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void 'Test list'() {
		when: 'Getting a list of tagAssets by asset'
			List<TagAsset> tagAssets = tagAssetService.list(project, device2.id)

		then: 'a list of tagAssets are returned for the asset'
			tagAssets.size() == 2
			tagAssets[0].tag.id == tag1.id
			tagAssets[1].tag.id == tag2.id
	}

	void 'Test list with asset from another project'() {
		when: 'trying to get a list of tagAssets from an asset, that belongs to another project'
			tagAssetService.list(project, device3.id)

		then: 'An exception is thrown'
			thrown EmptyResultException
	}

	void 'Test getTags'() {
		when: 'getting tags based on an asset'
			List<TagAsset> tagAssets = tagAssetService.getTags(project, device2.id)

		then: 'a list of tags is returned'
			tagAssets.size() == 2
			tagAssets[0].id == tag1.id
			tagAssets[1].id == tag2.id
	}

	void 'Test getTags with asset from another project'() {
		when: 'trying to get a list of tags, from an asset, that belongs to another project'
			tagAssetService.getTags(project, device3.id)

		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test add tags'() {
		when: 'adding a tag(s) to an asset'
			List<TagAsset> tagAssets = tagAssetService.applyTags(project, [tag2.id], device.id)

		then: 'a list of tagAssets is returned'
			tagAssets.size() == 1
			tagAssets[0].tag.id == tag2.id
	}

	void 'Test add tag from another project'() {
		when: 'trying to add tags from another project to an asset'
			tagAssetService.applyTags(project, [tag3.id], device.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test add tag to an asset from another project'() {
		when: 'trying to add tags to an asset from another project'
			tagAssetService.applyTags(project, [tag1.id], device3.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test remove tags'() {
		when: 'removing a tagAsset, from an asset, and checking the list of tagAssets'
			tagAssetService.removeTags(project, [tagAsset3.id])
			List<TagAsset> tagAssets = tagAssetService.list(project, device2.id)
		then: 'The list of tagAssets will not contain the delete tagAsset'
			tagAssets.size() == 1
			tagAssets[0].tag.id == tag1.id
	}

	void 'Test remove tags from another project'() {
		when: 'trying to remove a tagAsset from an asset that belongs to another project'
			tagAssetService.removeTags(project, [tagAsset4.id])
		then: 'an exception it thrown'
			thrown EmptyResultException
	}

	void 'Test asset cascade delete'() {
		when: 'deleting an asset'
			device.delete(flush: true)
			tagAssetService.getTags(project, device.id)
		then: 'all related tagAssets are deleted'
			thrown EmptyResultException
	}

	void 'Test tag cascade delete'() {
		when: 'deleting a tag'
			tag1.delete(flush: true)
			List<TagAsset> tagAssets = tagAssetService.list(project, device.id)
			List<TagAsset> tagAssets2 = tagAssetService.list(project, device2.id)
		then: 'all related tagAssets are deleted'
			tagAssets.size() == 0
			tagAssets2.size() == 1
	}

	void 'Test tagMerge'() {
		when: 'merging one tag into another'
			long tagId = tag1.id
			List<TagAsset> tagAssets = tagAssetService.merge(project, tag2.id, tag1.id)
			Tag tag = Tag.get(tagId)
			List<Tag> eventTags = tagEventService.getTags(project, event.id)
			List<Tag> event2Tags = tagEventService.getTags(project, event2.id)

		then: 'the tag is deleted and all the tagAssets are updated with that tag'
			!tag
			tagAssets[0].tag.id == tag2.id
			tagAssets[0].asset.id == device.id
			eventTags.size() == 1
			event2Tags.size() == 1
	}

	void 'Test tagMerge primary tag from another project'() {
		when: 'trying to merge a tag from another project into a tag'
			tagAssetService.merge(project, tag3.id, tag1.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test tagMerge secondary tag from another project'() {
		when: 'trying to merge a tag from another project into a tag'
			tagAssetService.merge(project, tag1.id, tag3.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}
}