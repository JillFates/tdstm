import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TagAssetService
import net.transitionmanager.service.TagService
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class TagAssetServiceIntegrationSpec extends IntegrationSpec {
	TagService      tagService
	TagAssetService tagAssetService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Project otherProject = projectTestHelper.createProject()

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
	Date now

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Black, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true, failOnError: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true, failOnError: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true, failOnError: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true, failOnError: true)

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

	void 'Test custom MySQL dialect'() {
		when: 'Executing a HQL query with group_concat and Json_object'
			List<Map> results = AssetEntity.executeQuery("""
				SELECT new map(
					a.id as id, 
					a.assetType as type, 
					a.assetClass as assetClass, 
					CONCAT(
						'[',
						if(
							ta.id,
							group_concat(
								json_object('name', t.name, 'description', t.description, 'color', t.color)
							),
							''
						), 
						']'
					) as tags) 
				from AssetEntity a 
				left outer join a.tagAssets ta
				left outer join ta.tag t
				where ta.id is not null
				group by a.id
			""")
		then: 'results from a joined table are returned as a JSON string in one column'
			results.size() == 3

			results[0].id == device.id
			results[0].assetClass == AssetClass.DEVICE
			results[0].type == 'Server'
			results[0].tags == '[{"name": "grouping assets", "color": "Black", "description": "This is a description"}]'

			results[1].id == device2.id
			results[1].assetClass == AssetClass.DEVICE
			results[1].type == 'Server'
			results[1].tags == '[{"name": "grouping assets", "color": "Black", "description": "This is a description"},{"name": "some assets", "color": "Blue", "description": "Another description"}]'

			results[2].id == device3.id
			results[2].assetClass == AssetClass.DEVICE
			results[2].type == 'Server'
			results[2].tags == '[{"name": "other", "color": "Red", "description": "Yet another description"}]'
	}

	void 'Test get'() {
		when: 'getting a TagAsset by id'
			TagAsset tagAsset = tagAssetService.get(tagAsset1.id)

		then: 'The TagAsset is returned'
			tagAsset.tag == tag1
	}

	void 'Test get of AssetTag for an id that does not exist'() {
		when: 'getting an tagAssets by id, for an asset that does not exist'
			TagAsset tagAsset = tagAssetService.get(0)

		then: 'no assets are returned'
			!tagAsset
	}

	void 'Test list'() {
		when: 'Getting a list of tagAssets by asset'
			List<TagAsset> tagAssets = tagAssetService.list(device2)

		then: 'a list of tagAssets are returned for the asset'
			tagAssets.size() == 2
			tagAssets[0].tag == tag1
			tagAssets[1].tag == tag2
	}

	void 'Test list with asset from another project'() {
		when: 'trying to get a list of tagAssets from an asset, that belongs to another project'
			tagAssetService.list(device3)

		then: 'An exception is thrown'
			thrown IllegalArgumentException
	}

	void 'Test getTags'() {
		when: 'getting tags based on an asset'
			List<TagAsset> tagAssets = tagAssetService.getTags(device2)

		then: 'a list of tags is returned'
			tagAssets.size() == 2
			tagAssets[0] == tag1
			tagAssets[1] == tag2
	}

	void 'Test getTags with asset from another project'() {
		when: 'trying to get a list of tags, from an asset, that belongs to another project'
			tagAssetService.getTags(device3)

		then: 'an exception is thrown'
			thrown IllegalArgumentException
	}

	void 'Test add tags'() {
		when: 'adding a tag(s) to an asset'
			List<TagAsset> tagAssets = tagAssetService.applyTags([tag2.id], device)

		then: 'a list of tagAssets is returned'
			tagAssets.size() == 1
			tagAssets[0].tag == tag2
	}

	void 'Test add tag from another project'() {
		when: 'trying to add tags from another project to an asset'
			tagAssetService.applyTags([tag3.id], device)
		then: 'an exception is thrown'
			thrown IllegalArgumentException
	}

	void 'Test add tag to an asset from another project'() {
		when: 'trying to add tags to an asset from another project'
			tagAssetService.applyTags([tag1.id], device3)
		then: 'an exception is thrown'
			thrown IllegalArgumentException
	}

	void 'Test remove tags'() {
		when: 'removing a tagAsset, from an asset, and checking the list of tagAssets'
			tagAssetService.removeTags([tagAsset3.id])
			List<TagAsset> tagAssets = tagAssetService.list(device2)
		then: 'The list of tagAssets will not contain the delete tagAsset'
			tagAssets.size() == 1
			tagAssets[0].tag == tag1
	}

	void 'Test remove tags from another project'() {
		when: 'trying to remove a tagAsset from an asset that belongs to another project'
			tagAssetService.removeTags([tagAsset4.id])
			tagAssetService.list(device2)
		then: 'an exception it thrown'
			thrown IllegalArgumentException
	}

	void 'Test asset cascade delete'() {
		when: 'deleting an asset'
			device.delete(flush: true)
			List<TagAsset> tagAssets = TagAsset.list()
		then: 'all related tagAssets are deleted'
			tagAssets.size() == 3
	}

	void 'Test tag cascade delete'() {
		when: 'deleting a tag'
			tag1.delete(flush: true)
			List<TagAsset> tagAssets = TagAsset.list()
		then: 'all related tagAssets are deleted'
			tagAssets.size() == 2
	}

	void 'Test tagMerge'() {
		when: 'merging one tag into another'
			long tagId = tag1.id
			List<TagAsset> tagAssets = tagAssetService.merge(tag2, tag1)
			Tag tag = Tag.get(tagId)

		then: 'the tag is deleted and all the tagAssets are updated with that tag'
			!tag
			tagAssets[0].tag == tag2
			tagAssets[0].asset == device
	}

	void 'Test tagMerge primary tag from another project'() {
		when: 'trying to merge a tag from another project into a tag'
			long tagId = tag1.id
			tagAssetService.merge(tag3, tag1)
		then: 'an exception is thrown'
			thrown IllegalArgumentException
	}

	void 'Test tagMerge secondary tag from another project'() {
		when: 'trying to merge a tag from another project into a tag'
			long tagId = tag1.id
			tagAssetService.merge(tag1, tag3)
		then: 'an exception is thrown'
			thrown IllegalArgumentException
	}
}